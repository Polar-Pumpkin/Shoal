package net.shoal.sir.voteup.listener;

import net.shoal.sir.voteup.config.CacheManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player user = event.getPlayer();
        if(user.hasPermission("VoteUp.admin")) {
            CacheManager.getInstance().report(user);
        }
    }

}
