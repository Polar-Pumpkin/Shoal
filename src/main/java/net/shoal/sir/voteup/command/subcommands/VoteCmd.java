package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;

public class VoteCmd implements Subcommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        return true;
    }
}
