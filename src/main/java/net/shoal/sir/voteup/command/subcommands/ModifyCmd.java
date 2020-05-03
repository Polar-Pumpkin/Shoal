package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.prompts.ModifyContentPrompt;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.ConversationUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.List;

public class ModifyCmd implements PCommand {

    public static final String ADD = "add";
    public static final String SET = "set";
    public static final String DEL = "del";

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        // /vote modify desc/autocast add/set/del line
        if (sender instanceof Player) {
            Player user = (Player) sender;
            Vote creating = VoteUpAPI.VOTE_MANAGER.draftVote(user.getUniqueId());
            if (creating != null) {
                if (args.length == 3 || args.length == 4) {
                    switch (args[1]) {
                        case "desc":
                            modify(user, creating, args, true);
                            break;
                        case "autocast":
                            modify(user, creating, args, false);
                            break;
                        default:
                            user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
                            break;
                    }
                } else user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
            }
        }
        return true;
    }

    private void modify(Player user, Vote creating, String[] args, boolean isDesc) {
        Vote.Data dataType = isDesc ? Vote.Data.DESCRIPTION : Vote.Data.AUTOCAST;
        if (VoteUpPerm.EDIT.hasPermission(user, dataType)) {
            PPlugin plugin = VoteUp.getInstance();
            List<String> list = isDesc ? creating.description : creating.autocast;
            if (ADD.equalsIgnoreCase(args[2]))
                ConversationUtil.start(plugin, user, new ModifyContentPrompt(user, creating, (args.length == 4 ? Integer.parseInt(args[3]) : list.size() + 1), isDesc, ADD), 300);
            else if (SET.equalsIgnoreCase(args[2]))
                ConversationUtil.start(plugin, user, new ModifyContentPrompt(user, creating, Integer.parseInt(args[3]), isDesc, SET), 300);
            else if (DEL.equalsIgnoreCase(args[2])) {
                list.remove((args.length == 4 ? Integer.parseInt(args[3]) : list.size() - 1));
                VoteUpAPI.VOTE_MANAGER.setVoteData(creating.voteID, user, dataType, list);
            } else user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
        }
    }
}
