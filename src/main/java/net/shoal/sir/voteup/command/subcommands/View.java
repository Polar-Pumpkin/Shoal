package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.GuiConfiguration;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class View implements Subcommand {
    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // /vote view voteID
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        if(sender instanceof Player) {
            Player user = (Player) sender;
            if(user.hasPermission(VoteUpPerm.VIEW.perm())) {
                if(args.length == 2) {
                    Vote data = VoteManager.getInstance().getVote(args[1]);
                    if(data != null) {
                        GuiConfiguration targetMenu;

                        if(VoteManager.getInstance().isVoted(data.getId(), user.getName())) {
                            if(VoteManager.getInstance().isUploadReason(data.getId(), user.getName())) {
                                targetMenu = GuiConfiguration.VOTE_DETAILS_COMPLETE;
                            } else {
                                targetMenu = GuiConfiguration.VOTE_DETAILS_REASON;
                            }
                        } else {
                            targetMenu = GuiConfiguration.VOTE_DETAILS_COMMON;
                        }

                        CommonUtil.openInventory(
                                user,
                                InventoryUtil.parsePlaceholder(
                                        GuiManager.getInstance().getMenu(targetMenu.getName()),
                                        data,
                                        user
                                )
                        );
                    } else {
                        user.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7目标投票不存在."));
                    }
                } else {
                    user.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                }
            } else {
                user.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7您没有权限这么做."));
            }
        } else {
            sender.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.ERROR, "&7控制台无法执行此命令."));
        }
        return true;
    }
}
