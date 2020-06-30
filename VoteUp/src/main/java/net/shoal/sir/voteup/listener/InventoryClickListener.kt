package net.shoal.sir.voteup.listener

import net.shoal.sir.voteup.listener.holder.CustomInventoryHolder
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent

class InventoryClickListener : Listener {
    @EventHandler
    fun onClick(e: InventoryClickEvent) {
        val holder = e.inventory.holder
        if (holder != null) {
            if (holder is CustomInventoryHolder) {
                holder.run(e)
            }
        }
    }
}