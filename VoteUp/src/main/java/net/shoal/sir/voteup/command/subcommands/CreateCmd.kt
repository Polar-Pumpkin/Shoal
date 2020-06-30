package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.inventory.CreateInventoryHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.UUID;

public class CreateCmd implements PCommand {

    @Override
    public String getPermission() {
        return VoteUpPerm.CREATE.node;
    }

    @Override
    public String getDescription() {
        return "创建新投票或返回投票草稿";
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        if (sender instanceof Player) {
            Player user = (Player) sender;
            Vote data;
            switch (args.length) {
                case 1:
                    data = VoteUpAPI.VOTE_MANAGER.create(user.getUniqueId());
                    break;
                case 2:
                    if ("back".equals(args[1])) data = VoteUpAPI.VOTE_MANAGER.draftVote(user.getUniqueId());
                    else {
                        data = null;
                        user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
                    }
                    break;
                case 3:
                    if ("player".equals(args[1])) {
                        UUID uuid;
                        Player target = Bukkit.getPlayerExact(args[2]);
                        if (target == null) uuid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                        else uuid = target.getUniqueId();
                        data = VoteUpAPI.VOTE_MANAGER.create(uuid);
                    } else {
                        data = null;
                        user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
                    }
                    break;
                default:
                    data = null;
                    user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
                    break;
            }

            if (data != null)
                BasicUtil.openInventory(plugin, user, new CreateInventoryHolder<>(data, user).getInventory());
        }
        return true;
    }
}
