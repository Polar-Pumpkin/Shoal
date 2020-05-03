package net.shoal.sir.voteup;

import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.command.VoteUpCmd;
import net.shoal.sir.voteup.config.ConfigManager;
import net.shoal.sir.voteup.listener.InventoryClickListener;
import net.shoal.sir.voteup.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.serverct.parrot.parrotx.PPlugin;

public final class VoteUp extends PPlugin {
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

        super.registerCommand(new VoteUpCmd());
    }
}
