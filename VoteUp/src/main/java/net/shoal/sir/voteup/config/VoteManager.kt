package net.shoal.sir.voteup.config

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.api.VoteUpPlaceholder
import net.shoal.sir.voteup.data.Notice
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.data.inventory.CreateInventoryHolder
import net.shoal.sir.voteup.enums.Msg
import net.shoal.sir.voteup.task.VoteEndTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.config.PFolder
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.I18n
import org.serverct.parrot.parrotx.utils.JsonChatUtil
import java.io.File
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate

class VoteManager : PFolder(PPlugin.getInstance(), "Votes", "投票数据文件夹") {
    private val voteMap: MutableMap<String?, Vote> = HashMap()
    private val endTaskMap: MutableMap<String?, Int?> = HashMap()
    override fun init() {
        super.init()
        voteMap.forEach { (s: String?, vote: Vote?) -> startCountdown(s) }
    }

    override fun load(@NonNull file: File) {
        voteMap[BasicUtil.getNoExFileName(file.name)] = Vote(file)
    }

    private fun startCountdown(voteID: String?) {
        val data = voteMap.getOrDefault(voteID, null) ?: return
        if (!data.open) return
        if (endTaskMap.containsKey(voteID)) return
        val timeRemain: Long = data.startTime + Vote.Companion.getDurationTimestamp(data.duration) - System.currentTimeMillis()
        if (timeRemain <= 0) {
            endVote(voteID)
            return
        }
        val endTask: BukkitRunnable = VoteEndTask(voteID)
        endTask.runTaskLater(plugin, timeRemain / 1000 * 20)
        endTaskMap[voteID] = endTask.taskId
    }

    fun getVote(id: String?): Vote {
        return voteMap.getOrDefault(id, null)
    }

    val newest: Vote?
        get() {
            val votes: MutableList<Vote> = ArrayList(voteMap.values)
            votes.removeIf { vote: Vote -> vote.isDraft || !vote.open }
            if (votes.isEmpty()) return null
            votes.sort(Comparator.comparing { obj: Vote -> obj.timestamp }.reversed())
            return votes[0]
        }

    fun list(filter: Predicate<Vote>): List<Vote> {
        val result: MutableList<Vote> = ArrayList()
        voteMap.values.forEach(Consumer { vote: Vote -> if (filter.test(vote)) result.add(vote) })
        return result
    }

    fun create(uuid: UUID?): Vote {
        val vote = Vote(plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_PARTICIPANT_LEAST.path, 3), uuid, "1d")
        voteMap[vote.voteID] = vote
        return vote
    }

    fun draftVote(uuid: UUID?): Vote {
        for ((_, vote) in voteMap) {
            if (vote.isOwner(uuid) && vote.isDraft) return vote
        }
        return create(uuid)
    }

    fun back(@NonNull user: Player) {
        BasicUtil.openInventory(plugin, user, CreateInventoryHolder(draftVote(user.uniqueId), user).inventory)
    }

    fun start(@NonNull user: Player) {
        val vote = draftVote(user.uniqueId) ?: return
        startCountdown(vote.voteID)
        vote.startTime = System.currentTimeMillis()
        vote.open = true
        vote.isDraft = false
        VoteUpAPI.SOUND!!.voteEvent(true)
        if (plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_BROADCAST_TITLE_VOTESTART.path, true)) BasicUtil.broadcastTitle(
                "",
                VoteUpPlaceholder.parse(vote, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.Start.Subtitle")),
                plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
        )
        Bukkit.getOnlinePlayers().forEach { player: Player ->
            player.spigot().sendMessage(JsonChatUtil.buildClickText(
                    VoteUpPlaceholder.parse(vote, plugin.lang[plugin.localeKey, I18n.Type.INFO, "Vote", "Event.Start.Broadcast"]),
                    ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + vote.voteID),
                    HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(Msg.VOTE_CLICK.msg)))
            ))
        }
    }

    fun vote(voteID: String?, @NonNull user: Player, choice: Vote.Choice?, anonymous: Boolean, reason: String?) {
        val uuid = user.uniqueId
        val vote = getVote(voteID) ?: return
        if (!vote.open) {
            I18n.send(user, plugin.lang[plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Closed"])
            return
        }
        if (!VoteUpPerm.VOTE.hasPermission(user, choice!!)) return
        if (vote.isVoted(uuid)) I18n.send(user, plugin.lang[plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Logged"])
        vote.participants!!.add(
                Vote.Participant(
                        uuid,
                        choice,
                        anonymous,
                        if (VoteUpPerm.REASON.hasPermission(user)) if (reason!!.length == 0) Msg.REASON_NOT_YET.msg else reason else Msg.REASON_NO_PERM.msg
                )
        )
        vote.save()
        I18n.send(user, plugin.lang[plugin.localeKey, I18n.Type.INFO, "Vote", "Vote." + choice.name])
        if (anonymous) return
        val starter = Bukkit.getPlayer(vote.owner!!)
        val notice = VoteUpAPI.CACHE_MANAGER!!.log(Notice.Type.VOTE, voteID, object : HashMap<String?, Any?>() {
            init {
                put("Voter", user.name)
                put("Choice", choice.name)
                put("Reason", reason)
            }
        })
        if (starter != null && starter.isOnline) {
            val announce = notice!!.announce(user.uniqueId)
            if (announce != null) I18n.send(starter, VoteUpPlaceholder.parse(vote, announce))
        }
        plugin.pConfig.config.getStringList(ConfPath.Path.ADMIN.path).forEach(
                Consumer { adminID: String? ->
                    val admin = Bukkit.getPlayer(UUID.fromString(adminID))
                    if (admin != null) {
                        val announce = notice!!.announce(admin.uniqueId)
                        if (announce != null) I18n.send(admin, VoteUpPlaceholder.parse(vote, announce))
                    }
                }
        )
    }

    fun endVote(voteID: String?) {
        val vote = getVote(voteID) ?: return
        if (vote.open) {
            vote.open = false
            vote.save()
            if (vote.isPassed) if (plugin.pConfig.config.getBoolean(ConfPath.Path.AUTOCAST_USERMODE.path, true)) {
                val owner = Bukkit.getPlayer(vote.getOwner())
                if (owner != null) vote.autocast!!.forEach(Consumer { s: String? -> owner.performCommand(s!!) }) else VoteUpAPI.CACHE_MANAGER!!.log(Notice.Type.AUTOCAST_WAIT_EXECUTE, voteID, HashMap())
            } else vote.autocast!!.forEach(Consumer { cmd: String? -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd!!) })
            VoteUpAPI.SOUND!!.voteEvent(false)
            if (plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_BROADCAST_TITLE_VOTEEND.path, false)) BasicUtil.broadcastTitle(
                    "",
                    VoteUpPlaceholder.parse(vote, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.End.Subtitle")),
                    plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                    plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                    plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
            )
            BasicUtil.broadcast(VoteUpPlaceholder.parse(vote, plugin.lang[plugin.localeKey, I18n.Type.INFO, "Vote", "Event.End.Broadcast"]))
            val notice = VoteUpAPI.CACHE_MANAGER!!.log(Notice.Type.VOTE_END, voteID, HashMap())
            plugin.pConfig.config.getStringList(ConfPath.Path.ADMIN.path).forEach(
                    Consumer { adminID: String? ->
                        val admin = Bukkit.getPlayer(UUID.fromString(adminID))
                        if (admin != null) {
                            val announce = notice!!.announce(admin.uniqueId)
                            if (announce != null) I18n.send(admin, VoteUpPlaceholder.parse(vote, announce))
                        }
                    }
            )
        }
    }

    override fun saveAll() {
        voteMap.forEach { (voteID: String?, vote: Vote) -> vote.save() }
    }

    override fun delete(id: String) {
        voteMap.remove(id)
    }
}