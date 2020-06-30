package net.shoal.sir.voteup.listener.holder

import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

interface CustomInventoryHolder : InventoryHolder {
    fun run(event : InventoryEvent)
}