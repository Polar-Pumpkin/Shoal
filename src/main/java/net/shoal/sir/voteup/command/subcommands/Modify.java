package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.conversation.prompts.ModifyContentPrompt;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;

import java.util.List;

public class Modify implements Subcommand {

    public static String ADD = "add";
    public static String SET = "set";
    public static String DEL = "del";

    private LocaleUtil locale;

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // /vote modify desc/autocast add/set/del line
        locale = VoteUp.getInstance().getLocale();
        if(sender instanceof Player) {
            Player user = (Player) sender;
            Vote creating = VoteManager.getInstance().getCreatingVote(user.getName());
            if(creating != null) {
                if(args.length == 3 || args.length == 4) {
                    switch(args[1]) {
                        case "desc":
                            modify(user, creating, args, true);
                            break;
                        case "autocast":
                            modify(user, creating, args, false);
                            break;
                        default:
                            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                            break;
                    }
                } else {
                    user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
                }
            }
        }
        return true;
    }

    private void modify(Player user, Vote creating, String[] args, boolean isDesc) {
        if(user.hasPermission(isDesc ? VoteUpPerm.CREATE_CUSTOM_DESCRIPTION.perm() : VoteUpPerm.CREATE_CUSTOM_AUTOCAST.perm())) {
            List<String> list = isDesc ? creating.getDescription() : creating.getAutoCast();
            if(ADD.equalsIgnoreCase(args[2])) {
                Conversation conversation = new ConversationFactory(VoteUp.getInstance())
                        .withFirstPrompt(new ModifyContentPrompt(
                                user,
                                (isDesc ? VoteDataType.DESCRIPTION : VoteDataType.AUTOCAST),
                                (args.length == 4 ? Integer.parseInt(args[3]) : list.size() + 1),
                                ADD
                        ))
                        .buildConversation(user);
                conversation.begin();
            } else if(SET.equalsIgnoreCase(args[2])) {
                Conversation conversation = new ConversationFactory(VoteUp.getInstance())
                        .withFirstPrompt(new ModifyContentPrompt(
                                user,
                                (isDesc ? VoteDataType.DESCRIPTION : VoteDataType.AUTOCAST),
                                Integer.parseInt(args[3]),
                                SET
                        ))
                        .buildConversation(user);
                conversation.begin();
            } else if(DEL.equalsIgnoreCase(args[2])) {
                list.remove((args.length == 4 ? Integer.parseInt(args[3]) : list.size() - 1));
                VoteManager.getInstance().setCreatingVoteData(user.getName(), (isDesc ? VoteDataType.DESCRIPTION : VoteDataType.AUTOCAST), list);
            } else {
                user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未知命令, 使用 &d/vote help &7查看帮助."));
            }
        } else {
            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7您没有权限这么做."));
        }
    }
}
