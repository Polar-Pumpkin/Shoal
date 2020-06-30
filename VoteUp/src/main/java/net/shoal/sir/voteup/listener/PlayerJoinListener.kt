package net.shoal.sir.voteup.listener

import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.api.VoteUpPlaceholder
import net.shoal.sir.voteup.config.ConfPath
import net.shoal.sir.voteup.data.Notice
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.I18n
import org.serverct.parrot.parrotx.utils.JsonChatUtil

class PlayerJoinListener : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val plugin = PPlugin.getInstance()
        val user = event.player
        val newest = VoteUpAPI.VOTE_MANAGER.newest
        if (newest != null) {
            if (!newest.isVoted(user.uniqueId)) {
                VoteUpAPI.SOUND!!.ding(user)
                BasicUtil.broadcastTitle(
                        "",
                        VoteUpPlaceholder.parse(newest, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.Join.Subtitle")),
                        plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                        plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                        plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
                )
                user.spigot().sendMessage(JsonChatUtil.buildClickText(
                        VoteUpPlaceholder.parse(newest, plugin.lang[plugin.localeKey, I18n.Type.INFO, "Vote", "Event.Join.Broadcast"]),
                        ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + newest.voteID),
                        HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(Msg.VOTE_CLICK.msg)))
                ))
            }
        }
        val admins = plugin.pConfig.config.getStringList(ConfPath.Path.ADMIN.path)
        val uuid = user.uniqueId.toString()
        val inList = admins.contains(uuid)
        val hasPerm = VoteUpPerm.NOTICE.hasPermission(user)
        if (inList && !hasPerm) admins.remove(uuid) else if (!inList && hasPerm) admins.add(uuid)
        plugin.pConfig.config[ConfPath.Path.ADMIN.path] = admins
        plugin.pConfig.save()
        for (type in Notice.Type.values()) VoteUpAPI.CACHE_MANAGER!!.report(type, user)
    }
}