package net.shoal.parrot.signitem;

import lombok.Getter;
import net.shoal.parrot.signitem.command.CommandHandler;
import net.shoal.parrot.signitem.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Map;

public final class SignItem extends PPlugin {

    public static NamespacedKey DESC;

    @Getter
    private static SignItem instance;

    @Override
    protected void preload() {
        instance = this;
        DESC = new NamespacedKey(this, "Description");
        pConfig = new ConfigManager();
        setTimeLog(lang.data.info("启动完成, 共花费 &c{0}ms"));
    }

    @Override
    protected void load() {
        registerCommand(new CommandHandler());

        try {
            final Map<String, PCommand> commands = getCmdHandler().getCommands();
            Bukkit.getPluginCommand("sign").setExecutor(commands.get("sign"));
            Bukkit.getPluginCommand("unsign").setExecutor(commands.get("unsign"));
        } catch (NullPointerException e) {
            lang.log.error(I18n.REGISTER, "快捷主命令(sign/unsign)", e, "shoal");
        }
    }
}
