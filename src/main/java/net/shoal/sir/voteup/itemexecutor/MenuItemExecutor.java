package net.shoal.sir.voteup.itemexecutor;

import org.bukkit.event.inventory.InventoryClickEvent;

public interface MenuItemExecutor {
    boolean execute(InventoryClickEvent event);
}
