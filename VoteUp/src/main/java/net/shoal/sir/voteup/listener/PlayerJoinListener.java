package net.shoal.sir.voteup.listener;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.config.ConfigManager;
import net.shoal.sir.voteup.data.Notice;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;

import java.util.List;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PPlugin plugin = VoteUp.getInstance();
        Player user = event.getPlayer();

        Vote newest = VoteUpAPI.VOTE_MANAGER.getNewest();
        if (newest != null) {
            if (!newest.isVoted(user.getUniqueId())) {
                VoteUpAPI.SOUND.ding(user);
                BasicUtil.broadcastTitle(
                        "",
                        VoteUpPlaceholder.parse(newest, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.Join.Subtitle")),
                        plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                        plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                        plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
                );
                user.spigot().sendMessage(JsonChatUtil.buildClickText(
                        VoteUpPlaceholder.parse(newest, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Event.Join.Broadcast")),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + newest.voteID),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(BuiltinMsg.VOTE_CLICK.msg)))
                ));
            }
        }

        List<String> admins = plugin.pConfig.getConfig().getStringList(ConfigManager.Path.ADMIN.path);

        String uuid = user.getUniqueId().toString();
        boolean inList = admins.contains(uuid);
        boolean hasPerm = VoteUpPerm.NOTICE.hasPermission(user);

        if (inList && !hasPerm) admins.remove(uuid);
        else if (!inList && hasPerm) admins.add(uuid);

        plugin.pConfig.getConfig().set(ConfigManager.Path.ADMIN.path, admins);
        plugin.pConfig.save();

        for (Notice.Type type : Notice.Type.values()) VoteUpAPI.CACHE_MANAGER.report(type, user);
    }

}
