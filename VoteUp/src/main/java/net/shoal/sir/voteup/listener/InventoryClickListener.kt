package net.shoal.sir.voteup.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.serverct.parrot.parrotx.data.InventoryExecutor;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder != null) if (holder instanceof InventoryExecutor) ((InventoryExecutor) holder).execute(event);
    }
}
