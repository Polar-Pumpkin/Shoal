package net.shoal.sir.voteup;

import net.shoal.sir.voteup.config.CacheManager;
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
        CacheManager.getInstance().load();
    }
}
