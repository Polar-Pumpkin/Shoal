package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.ChestMenu;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Create implements Subcommand {

    private Player user;
    private ChestMenu creatingMenu;

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if(sender instanceof Player) {
            user = (Player) sender;
            creatingMenu = InventoryUtil.parsePlaceholder(GuiManager.getInstance().getMenu("CreateMenu"), VoteManager.getInstance().startCreateVote(user));
            CommonUtil.openInventory(user, creatingMenu.getInventory());
        }
        return true;
    }
}
