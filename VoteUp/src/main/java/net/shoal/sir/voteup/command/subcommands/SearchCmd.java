package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.config.GuiManager;
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
                    String data = null;

                    if ("me".equalsIgnoreCase(args[1]))
                        data = "owner:" + user.getUniqueId();
                    else if ("open".equalsIgnoreCase(args[1]))
                        data = "open:true";
                    else {
                        StringBuilder filter = new StringBuilder();
                        for (int index = args.length - 1; index >= 1; index--) {
                            filter.append(args[index]);
                            if (index != 1) filter.append(" ");
                        }
                        data = filter.toString();
                    }

                    BasicUtil.openInventory(plugin, user, new ListInventoryHolder<>(data, user).getInventory());
                    VoteUpAPI.GUI_MANAGER.getNavigator(user).chain(GuiManager.GuiKey.VOTE_LIST, data);
                } else I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"));
            }
        } else
            sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.ERROR, Msg.ERROR_COMMAND_NOT_PLAYER.msg));
        return true;
    }
}
