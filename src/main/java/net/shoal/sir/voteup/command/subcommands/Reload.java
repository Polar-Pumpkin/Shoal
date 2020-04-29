package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;

public class Reload implements Subcommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        VoteUp.getInstance().init();
        sender.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7重载插件配置成功."));
        return true;
    }
}
