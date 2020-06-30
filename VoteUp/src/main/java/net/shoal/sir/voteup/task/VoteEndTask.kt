package net.shoal.sir.voteup.task

import net.shoal.sir.voteup.api.VoteUpAPI
import org.bukkit.scheduler.BukkitRunnable

class VoteEndTask(private val voteID: String?) : BukkitRunnable() {
    override fun run() {
        VoteUpAPI.VOTE_MANAGER!!.endVote(voteID)
    }

}