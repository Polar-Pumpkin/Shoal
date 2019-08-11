package net.shoal.sir.voteup;

import lombok.Getter;
import net.shoal.sir.voteup.command.CommandHandler;
import net.shoal.sir.voteup.config.ExecutorManager;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.listener.InventoryClickListener;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class VoteUp extends JavaPlugin {

    @Getter private static VoteUp instance;
    @Getter private LocaleUtil locale;
    public static String LOCALE;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        if(!new File(getDataFolder() + File.separator + "config.yml").exists()) {
            saveDefaultConfig();
        }
        init();

        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
        Bukkit.getPluginCommand("voteup").setExecutor(new CommandHandler());
    }

    public void init() {
        reloadConfig();
        locale = new LocaleUtil(this);
        LOCALE = getConfig().getString("Language");
        SoundManager.getInstance().init();
        ExecutorManager.getInstance().load();
        GuiManager.getInstance().load();
        VoteManager.getInstance().load();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
