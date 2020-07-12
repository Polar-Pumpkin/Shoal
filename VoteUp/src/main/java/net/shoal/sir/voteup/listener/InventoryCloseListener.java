package net.shoal.sir.voteup.listener;

import net.shoal.sir.voteup.api.VoteUpAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        VoteUpAPI.GUI_MANAGER.getNavigator((Player) event.getPlayer()).end();
    }

}
