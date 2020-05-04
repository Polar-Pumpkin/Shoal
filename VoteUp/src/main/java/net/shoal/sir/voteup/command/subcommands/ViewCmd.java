package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.inventory.DetailsInventoryHolder;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

public class ViewCmd implements PCommand {
    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "查看投票的详细信息";
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        // /vote view voteID
        if (sender instanceof Player) {
            Player user = (Player) sender;
            if (VoteUpPerm.VIEW.hasPermission(user)) {
                if (args.length == 2) {
                    Vote vote = VoteUpAPI.VOTE_MANAGER.getVote(args[1]);
                    if (vote != null)
                        BasicUtil.openInventory(plugin, user, new DetailsInventoryHolder<>(vote, user).getInventory());
                    else
                        I18n.send(user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, BuiltinMsg.ERROR_GET_VOTE.msg));
                } else I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
            }
        } else
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.ERROR, BuiltinMsg.ERROR_COMMAND_NOT_PLAYER.msg));
        return true;
    }
}
