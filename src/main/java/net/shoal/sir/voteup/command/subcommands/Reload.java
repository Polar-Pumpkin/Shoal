package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;

public class Reload implements Subcommand {

    private LocaleUtil locale;

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        locale = VoteUp.getInstance().getLocale();
        VoteUp.getInstance().init();
        sender.sendMessage(locale.buildMessage(VoteUp.getInstance().getLocaleKey(), MessageType.INFO, "&7重载插件配置成功."));
        return true;
    }
}
