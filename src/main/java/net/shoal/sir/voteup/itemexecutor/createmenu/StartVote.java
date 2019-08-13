package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StartVote implements MenuItemExecutor {

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        Player user = (Player) event.getWhoClicked();
        CommonUtil.closeInventory(user);
        VoteManager.getInstance().finishVoteCreating(user.getName());
        return true;
    }
}
