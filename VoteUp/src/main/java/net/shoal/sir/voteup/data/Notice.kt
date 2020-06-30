package net.shoal.sir.voteup.data

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPlaceholder
import net.shoal.sir.voteup.config.ConfPath
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.data.flags.Timestamp
import org.serverct.parrot.parrotx.utils.ConfigUtil
import org.serverct.parrot.parrotx.utils.EnumUtil
import org.serverct.parrot.parrotx.utils.TimeUtil
import java.util.*
import java.util.function.Consumer

class Notice : Timestamp {
    private val plugin = PPlugin.getInstance()
    var number: Int
    var type: Type
    var voteID: String?
    var announced: MutableList<UUID> = ArrayList()
    var params: MutableMap<String, Any> = HashMap()
    private var time: Long

    constructor(type: Type, voteID: String?, number: Int, params: Map<String, Any>?) {
        this.type = type
        this.voteID = voteID
        this.number = number
        this.params.putAll(params!!)
        time = System.currentTimeMillis()
    }

    /*
    voteID:
      '1':
        Type: type
        Timestamp: timestamp
        Announced:
          - uuid
          - uuid
        Params:
          Voter: Cat
          Choice: ACCEPT
          Reason: string
     */
    constructor(voteID: String?, @NonNull section: ConfigurationSection) {
        this.voteID = voteID
        number = section.name.toInt()
        type = EnumUtil.valueOf(Type::class.java, section.getString("Type")!!.toUpperCase())
        time = section.getLong("Timestamp")
        section.getStringList("Announced").forEach(Consumer { uuid: String? -> announced.add(UUID.fromString(uuid)) })
        params.putAll(ConfigUtil.getMap(section, "Params"))
    }

    fun save(@NonNull section: ConfigurationSection) {
        section["Type"] = type.name
        section["Timestamp"] = time
        val strList: MutableList<String> = ArrayList()
        announced.forEach(Consumer { uuid: UUID -> strList.add(uuid.toString()) })
        section["Announced"] = strList
        val params = section.createSection("Params")
        this.params.forEach { (s: String?, o: Any?) -> params[s] = o }
    }

    fun announce(uuid: UUID): String? {
        val vote = VoteUpAPI.VOTE_MANAGER!!.getVote(voteID) ?: return null
        var result: String? = null
        when (type) {
            Type.VOTE_END, Type.VOTE -> {
                if (announced.contains(uuid)) return null
                announced.add(uuid)
                result = plugin.lang.getRaw(plugin.localeKey, "Vote", "Notice." + type.name + if (vote.isOwner(uuid)) ".Noticer" else ".Starter")
                for ((key, value) in params) result = result.replace("%" + key.toLowerCase() + "%", value as String)
                result = VoteUpPlaceholder.parse(vote, result)
                        .replace("%time%", TimeUtil.getDescriptionTimeFromTimestamp(time) + " &7[" + getTime() + "]")
            }
            Type.AUTOCAST_WAIT_EXECUTE -> {
                if (uuid !== vote.getOwner() || announced.contains(uuid)) return null
                announced.add(uuid)
                val owner = Bukkit.getPlayer(vote.getOwner()) ?: break
                vote.autocast!!.forEach(Consumer { s: String? -> owner.performCommand(s!!) })
            }
            else -> {
                if (announced.contains(uuid)) return null
                announced.add(uuid)
                result = plugin.lang.getRaw(plugin.localeKey, "Vote", "Notice." + type.name + if (vote.isOwner(uuid)) ".Noticer" else ".Starter")
                for ((key, value) in params) result = result.replace("%" + key.toLowerCase() + "%", value as String)
                result = VoteUpPlaceholder.parse(vote, result)
                        .replace("%time%", TimeUtil.getDescriptionTimeFromTimestamp(time) + " &7[" + getTime() + "]")
            }
        }
        return result
    }

    val isOver: Boolean
        get() {
            val vote = VoteUpAPI.VOTE_MANAGER!!.getVote(voteID) ?: return true
            return when (type) {
                Type.VOTE, Type.VOTE_END -> {
                    val admins = plugin.pConfig.config.getStringList(ConfPath.Path.ADMIN.path)
                    val announced: MutableList<String> = ArrayList()
                    this.announced.forEach(Consumer { uuid: UUID -> announced.add(uuid.toString()) })
                    announced == admins
                }
                Type.AUTOCAST_WAIT_EXECUTE -> announced.contains(vote.getOwner())
                else -> {
                    val admins = plugin.pConfig.config.getStringList(ConfPath.Path.ADMIN.path)
                    val announced: MutableList<String> = ArrayList()
                    this.announced.forEach(Consumer { uuid: UUID -> announced.add(uuid.toString()) })
                    announced == admins
                }
            }
        }

    override fun getTimestamp(): Long {
        return time
    }

    override fun setTime(l: Long) {
        time = l
    }

    enum class Type {
        VOTE_END, VOTE, AUTOCAST_WAIT_EXECUTE
    }
}