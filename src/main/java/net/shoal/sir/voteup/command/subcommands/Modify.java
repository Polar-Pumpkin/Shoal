package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Modify implements Subcommand {

    public static String ADD = "add";
    public static String SET = "set";
    public static String DEL = "del";

    private LocaleUtil locale;

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        locale = VoteUp.getInstance().getLocale();
        if(sender.hasPermission("VoteUp.admin")) {
            if(sender instanceof Player) {
                Player user = (Player) sender;
                Vote creating = VoteManager.getInstance().getCreatingVote(user.getName());
                if(creating != null) {
                    if(args.length == 2) {
                        if(ADD.equalsIgnoreCase(args[1])) {

                        } else if(DEL.equalsIgnoreCase(args[1])) {

                        } else {
                            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                        }
                    } else if(args.length == 3) {
                        if(ADD.equalsIgnoreCase(args[1])) {

                        } else if(SET.equalsIgnoreCase(args[1])) {

                        } else if(DEL.equalsIgnoreCase(args[1])) {

                        } else {
                            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                        }
                    } else {
                        user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                    }
                }
            }
        }
        return true;
    }
}
