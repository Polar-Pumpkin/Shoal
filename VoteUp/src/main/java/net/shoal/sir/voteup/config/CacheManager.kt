package net.shoal.sir.voteup.config

import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.shoal.sir.voteup.data.Notice
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.config.PConfig
import org.serverct.parrot.parrotx.utils.I18n
import org.serverct.parrot.parrotx.utils.JsonChatUtil
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer

class CacheManager : PConfig(PPlugin.getInstance(), "cache", "缓存日志文件") {
    private val notices: MutableMap<String?, Map<Int, Notice?>> = HashMap()
    override fun load(@NonNull file: File) {
        val log = config.getConfigurationSection("Logs") ?: return
        log.getKeys(false).forEach(
                Consumer { voteID: String? ->
                    val section = log.getConfigurationSection(voteID!!)
                    section?.getKeys(false)?.forEach(
                            Consumer { number: String? ->
                                val numberSection = section.getConfigurationSection(number!!)
                                if (numberSection != null) {
                                    val map: MutableMap<Int, Notice?> = notices.getOrDefault(voteID, HashMap<Int, Notice>())
                                    val notice = Notice(voteID, numberSection)
                                    map[notice.number] = notice
                                    notices[voteID] = map
                                }
                            }
                    )
                }
        )
    }

    override fun saveDefault() {
        try {
            file.createNewFile()
        } catch (e: IOException) {
            plugin.lang.logError(I18n.LOAD, typeName, e, null)
        }
    }

    override fun save() {
        val log = config.createSection("Logs")
        notices.forEach { (voteID: String?, map: Map<Int, Notice?>) ->
            val voteIDSection = log.createSection(voteID!!)
            map.forEach { (number: Int, notice: Notice?) -> notice!!.save(voteIDSection.createSection(number.toString())) }
        }
        super.save()
    }

    fun log(type: Notice.Type, voteID: String?, params: Map<String, Any>?): Notice {
        val noticeMap: MutableMap<Int, Notice?> = notices.getOrDefault(voteID, HashMap<Int, Notice>())
        var number = noticeMap.size + 1
        while (noticeMap.containsKey(number)) number++
        val notice = Notice(type, voteID, number, params)
        noticeMap[number] = notice
        notices[voteID] = noticeMap
        return notice
    }

    fun report(type: Notice.Type, @NonNull user: Player) {
        val content: MutableList<String> = ArrayList()
        val hover = StringBuilder()
        notices.forEach { (voteID: String?, map: Map<Int, Notice?>) ->
            map.forEach { (number: Int?, notice: Notice?) ->
                val append = notice!!.announce(user.uniqueId)
                if (append != null) content.add(append)
                if (notice.isOver) map.remove(number)
            }
        }
        if (content.isEmpty()) return
        val text: TextComponent = JsonChatUtil.getFromLegacy(
                plugin.lang[plugin.localeKey, I18n.Type.INFO, "Vote", "Notice." + type.name + ".Head"]
                        .replace("%amount%", content.size.toString())
        )
        val iterator: Iterator<String> = content.iterator()
        while (iterator.hasNext()) {
            hover.append(iterator.next())
            if (iterator.hasNext()) hover.append("\n")
        }
        text.setHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(hover.toString()))))
        user.spigot().sendMessage(text)
    }
}