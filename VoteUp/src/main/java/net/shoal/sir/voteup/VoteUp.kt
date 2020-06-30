package net.shoal.sir.voteup

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.command.VoteUpCmd
import net.shoal.sir.voteup.config.ConfPath
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.enums.Msg
import net.shoal.sir.voteup.listener.InventoryClickListener
import net.shoal.sir.voteup.listener.PlayerJoinListener
import org.bstats.bukkit.Metrics
import org.bstats.bukkit.Metrics.SingleLineChart
import org.bukkit.Bukkit
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.I18n
import java.util.concurrent.Callable

class VoteUp : PPlugin() {
    override fun registerListener() {
        Bukkit.getPluginManager().registerEvents(PlayerJoinListener(), this)
        Bukkit.getPluginManager().registerEvents(InventoryClickListener(), this)
    }

    override fun preload() {
        pConfig = ConfPath()
        pConfig.init()
    }

    public override fun load() {
        VoteUpAPI.VOTE_MANAGER!!.init()
        VoteUpAPI.GUI_MANAGER!!.init()
        VoteUpAPI.CACHE_MANAGER!!.init()
        if (pConfig.config.getBoolean(ConfPath.Path.BSTATS.path, true)) {
            val metrics = Metrics(this, PLUGIN_ID)
            metrics.addCustomChart(SingleLineChart("totalVote", Callable { VoteUpAPI.VOTE_MANAGER!!.list { vote: Vote -> !vote.isDraft }.size }))
            metrics.addCustomChart(SingleLineChart("openVote", Callable { VoteUpAPI.VOTE_MANAGER!!.list { vote: Vote -> !vote.isDraft && vote.open }.size }))
            metrics.addCustomChart(SingleLineChart("closeVote", Callable { VoteUpAPI.VOTE_MANAGER!!.list { vote: Vote -> !vote.isDraft && !vote.open }.size }))
            lang.log(Msg.BSTATS_ENABLE.msg, I18n.Type.INFO, false)
        } else lang.log(Msg.BSTATS_DISABLE.msg, I18n.Type.WARN, false)
        super.registerCommand(VoteUpCmd())
    }

    override fun onDisable() {
        VoteUpAPI.VOTE_MANAGER!!.saveAll()
        VoteUpAPI.CACHE_MANAGER!!.save()
        super.onDisable()
    }

    companion object {
        const val PLUGIN_ID = 7972
    }
}