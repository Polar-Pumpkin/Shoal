package net.shoal.sir.voteup.listener;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.config.ConfigManager;
import net.shoal.sir.voteup.data.Notice;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.serverct.parrot.parrotx.PPlugin;

import java.util.List;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PPlugin plugin = VoteUp.getInstance();
        Player user = event.getPlayer();
        List<String> admins = plugin.pConfig.getConfig().getStringList(ConfigManager.Path.ADMIN.path);

        String uuid = user.getUniqueId().toString();
        boolean inList = admins.contains(uuid);
        boolean hasPerm = VoteUpPerm.NOTICE.hasPermission(user);

        if (inList && !hasPerm) admins.remove(uuid);
        else if (!inList && hasPerm) admins.add(uuid);

        plugin.pConfig.getConfig().set(ConfigManager.Path.ADMIN.path, admins);
        plugin.pConfig.save();

        VoteUpAPI.CACHE_MANAGER.report(Notice.Type.VOTE_END, user);
        VoteUpAPI.CACHE_MANAGER.report(Notice.Type.VOTE, user);
    }

}
