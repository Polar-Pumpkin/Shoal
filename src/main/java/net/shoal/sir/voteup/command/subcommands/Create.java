package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Create implements Subcommand {

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        if(sender instanceof Player) {
            Player user = (Player) sender;
            if(user.hasPermission(VoteUpPerm.CREATE.perm())) {
                Vote data = null;

                switch(args.length) {
                    case 1:
                        data = VoteManager.getInstance().startCreateVote(user.getName());
                        break;
                    case 2:
                        switch(args[1]) {
                            case "back":
                                data = VoteManager.getInstance().getCreatingVote(user.getName());
                                break;
                            default:
                                user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                                break;
                        }
                        break;
                    case 3:
                        switch (args[1]) {
                            case "player":
                                data = VoteManager.getInstance().startCreateVote(args[2]);
                                break;
                            default:
                                user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                                break;
                        }
                    default:
                        user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                        break;
                }

                if(data != null) {
                    CommonUtil.openInventory(
                            user,
                            InventoryUtil.parsePlaceholder(
                                    GuiManager.getInstance().getMenu(GuiManager.CREATE_MENU),
                                    data,
                                    user
                            )
                    );
                }
            } else {
                user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7您没有权限这么做."));
            }
        }
        return true;
    }
}
