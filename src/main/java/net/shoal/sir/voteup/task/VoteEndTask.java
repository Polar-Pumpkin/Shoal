package net.shoal.sir.voteup.task;

import net.shoal.sir.voteup.config.VoteManager;
import org.bukkit.scheduler.BukkitRunnable;

public class VoteEndTask extends BukkitRunnable {

    private String voteID;
    public VoteEndTask(String id) {
        this.voteID = id;
    }

    @Override
    public void run() {
        VoteManager.getInstance().voteEnd(voteID);
    }
}
