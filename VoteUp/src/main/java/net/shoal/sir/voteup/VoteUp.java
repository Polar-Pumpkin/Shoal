package net.shoal.sir.voteup;

import lombok.Getter;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.command.VoteUpCmd;
import net.shoal.sir.voteup.config.VoteUpConfig;
import net.shoal.sir.voteup.enums.Msg;
import net.shoal.sir.voteup.listener.InventoryCloseListener;
import net.shoal.sir.voteup.listener.PlayerJoinListener;
import org.bstats.bukkit.Metrics;
import org.serverct.parrot.parrotx.PPlugin;

public final class VoteUp extends PPlugin {
    @Getter private static VoteUp instance;
    public final static int PLUGIN_ID = 7972;

    @Override
    protected void preload() {
        this.pConfig = new VoteUpConfig();
        this.pConfig.init();
    }

    @Override
    public void load() {
        instance = this;

        VoteUpAPI.VOTE_MANAGER.init();
        VoteUpAPI.GUI_MANAGER.init();
        VoteUpAPI.CACHE_MANAGER.init();

        if (VoteUpAPI.CONFIG.bStats) {
            Metrics metrics = new Metrics(this, PLUGIN_ID);
            metrics.addCustomChart(new Metrics.SingleLineChart("totalVote", () -> VoteUpAPI.VOTE_MANAGER.list(vote -> !vote.isDraft).size()));
            metrics.addCustomChart(new Metrics.SingleLineChart("openVote", () -> VoteUpAPI.VOTE_MANAGER.list(vote -> !vote.isDraft && vote.open).size()));
            metrics.addCustomChart(new Metrics.SingleLineChart("closeVote", () -> VoteUpAPI.VOTE_MANAGER.list(vote -> !vote.isDraft && !vote.open).size()));

            this.lang.log.info(Msg.BSTATS_ENABLE.msg);
        } else this.lang.log.warn(Msg.BSTATS_DISABLE.msg);

        listen(manager -> {
            manager.registerEvents(new PlayerJoinListener(), this);
            manager.registerEvents(new InventoryCloseListener(), this);
        });

        super.registerCommand(new VoteUpCmd());
    }

    @Override
    public void onDisable() {
        VoteUpAPI.VOTE_MANAGER.saveAll();
        VoteUpAPI.CACHE_MANAGER.save();
        super.onDisable();
    }
}
