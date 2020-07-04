package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.data.inventory.ListInventoryHolder;
import net.shoal.sir.voteup.enums.Msg;
import net.shoal.sir.voteup.enums.VoteFilter;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SearchCmd implements PCommand {
    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public String getDescription() {
        return "检索符合条件的投票";
    }

    @Override
    public String[] getParams(int arg) {
        List<String> params = new ArrayList<>(Arrays.asList("me", "open"));
        for (VoteFilter filter : VoteFilter.values()) params.add(filter.name() + ":");
        return params.toArray(new String[0]);
    }

    @Override
    public boolean execute(PPlugin plugin, CommandSender sender, String[] args) {
        // /vote search filter
        if (sender instanceof Player) {
            Player user = (Player) sender;
            if (VoteUpPerm.SEARCH.hasPermission(user)) {
                if (args.length >= 2) {
                    if ("me".equalsIgnoreCase(args[1]))
                        BasicUtil.openInventory(plugin, user, new ListInventoryHolder<>("owner:" + user.getUniqueId(), user, null).getInventory());
                    else if ("open".equalsIgnoreCase(args[1]))
                        BasicUtil.openInventory(plugin, user, new ListInventoryHolder<>("open:true", user, null).getInventory());
                    else {
                        StringBuilder filter = new StringBuilder();
                        for (int index = args.length - 1; index >= 1; index--) {
                            filter.append(args[index]);
                            if (index != 1) filter.append(" ");
                        }
                        BasicUtil.openInventory(plugin, user, new ListInventoryHolder<>(filter.toString(), user, null).getInventory());
                    }
                } else I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
            }
        } else
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.ERROR, Msg.ERROR_COMMAND_NOT_PLAYER.msg));
        return true;
    }
}
