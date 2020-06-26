package net.shoal.sir.voteup;

import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.command.VoteUpCmd;
import net.shoal.sir.voteup.config.ConfigManager;
import net.shoal.sir.voteup.listener.InventoryClickListener;
import net.shoal.sir.voteup.listener.PlayerJoinListener;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.serverct.parrot.parrotx.PPlugin;

public final class VoteUp extends PPlugin {
    public final static int PLUGIN_ID = 7972;

    @Override
    protected void registerListener() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
    }

    @Override
    protected void preload() {
        this.pConfig = new ConfigManager();
        this.pConfig.init();
    }

    @Override
    public void load() {
        VoteUpAPI.VOTE_MANAGER.init();
        VoteUpAPI.GUI_MANAGER.init();
        VoteUpAPI.CACHE_MANAGER.init();

        if (pConfig.getConfig().getBoolean(ConfigManager.Path.BSTATS.path, true)) {
            Metrics metrics = new Metrics(this, PLUGIN_ID);
            metrics.addCustomChart(new Metrics.SingleLineChart("totalVote", () -> VoteUpAPI.VOTE_MANAGER.list(vote -> !vote.isDraft).size()));
            metrics.addCustomChart(new Metrics.SingleLineChart("openVote", () -> VoteUpAPI.VOTE_MANAGER.list(vote -> !vote.isDraft && vote.open).size()));
            metrics.addCustomChart(new Metrics.SingleLineChart("closeVote", () -> VoteUpAPI.VOTE_MANAGER.list(vote -> !vote.isDraft && !vote.open).size()));
        }

        super.registerCommand(new VoteUpCmd());
    }

    @Override
    public void onDisable() {
        VoteUpAPI.VOTE_MANAGER.saveAll();
        VoteUpAPI.CACHE_MANAGER.save();
        super.onDisable();
    }
}
