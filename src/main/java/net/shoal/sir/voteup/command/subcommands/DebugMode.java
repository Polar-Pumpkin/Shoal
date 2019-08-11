package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class DebugMode implements Subcommand {

    private LocaleUtil locale;

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        locale = VoteUp.getInstance().getLocale();
        FileConfiguration config = VoteUp.getInstance().getConfig();
        boolean result = !config.getBoolean("Debug");
        config.set("Debug", result);
        VoteUp.getInstance().saveConfig();
        sender.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7Debug模式已 " + (result ? "&a&l开启" : "&c&l关闭") + "&7."));
        return true;
    }
}
