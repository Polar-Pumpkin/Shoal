package net.shoal.sir.voteup.listener;

import net.shoal.sir.voteup.config.CacheManager;
import net.shoal.sir.voteup.enums.CacheLogType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player user = event.getPlayer();
        CacheManager.getInstance().report(CacheLogType.VOTE_END, user);
        CacheManager.getInstance().report(CacheLogType.VOTE_VOTED, user);
    }

}
