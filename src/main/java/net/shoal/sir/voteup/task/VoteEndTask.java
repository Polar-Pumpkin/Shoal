package net.shoal.sir.voteup.task;

import net.shoal.sir.voteup.api.VoteUpAPI;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteEndTask extends BukkitRunnable {

    private final String voteID;
    public VoteEndTask(String id) {
        this.voteID = id;
    }

    @Override
    public void run() {
        VoteUpAPI.VOTE_MANAGER.endVote(voteID);
    }
}
