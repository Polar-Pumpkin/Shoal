package net.shoal.sir.voteup.listener;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.api.VoteUpSound;
import net.shoal.sir.voteup.data.Notice;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.Msg;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.serverct.parrot.parrotx.PPlugin;
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
                VoteUpSound.ding(user);
                user.sendTitle(
                        "",
                        VoteUpPlaceholder.parse(newest, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.Join.Subtitle")),
                        VoteUpAPI.CONFIG.title_fadeIn,
                        VoteUpAPI.CONFIG.title_stay,
                        VoteUpAPI.CONFIG.title_fadeOut
                );
                user.spigot().sendMessage(JsonChatUtil.buildClickText(
                        VoteUpPlaceholder.parse(newest, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Event.Join.Broadcast")),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + newest.voteID),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(Msg.VOTE_CLICK.msg)))
                ));
            }
        }

        List<String> admins = VoteUpAPI.CONFIG.admins;

        String uuid = user.getUniqueId().toString();
        boolean inList = admins.contains(uuid);
        boolean hasPerm = VoteUpPerm.ADMIN.hasPermission(user);

        if (inList && !hasPerm) admins.remove(uuid);
        else if (!inList && hasPerm) admins.add(uuid);

        VoteUpAPI.CONFIG.admins = admins;

        for (Notice.Type type : Notice.Type.values()) VoteUpAPI.CACHE_MANAGER.report(type, user);
    }

}
