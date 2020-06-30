package net.shoal.sir.voteup.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.serverct.parrot.parrotx.data.InventoryExecutor

class InventoryClickListener : Listener {
    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder
        if (holder != null) if (holder is InventoryExecutor) holder.execute(event)
    }
}