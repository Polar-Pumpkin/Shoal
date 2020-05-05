package net.shoal.sir.voteup.listener;

import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.data.Notice;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player user = event.getPlayer();
        // TODO 不要 0 提醒还发通知
        VoteUpAPI.CACHE_MANAGER.report(Notice.Type.VOTE_END, user);
        VoteUpAPI.CACHE_MANAGER.report(Notice.Type.VOTE, user);
    }

}
