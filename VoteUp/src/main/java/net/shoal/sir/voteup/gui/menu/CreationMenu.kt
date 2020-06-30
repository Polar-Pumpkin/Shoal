package net.shoal.sir.voteup.gui.menu

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class VoteUpMenu {


    object Main {
        private val glassSlot = intArrayOf(0, 1, 2, 3, 5, 6, 7, 8, 45, 46, 47, 49, 51, 52, 53)

        fun createMenu(): Inventory {
            val inventory = Bukkit.createInventory(null, 6 * 3, "")
            return inventory
        }

        fun init(inventory: Inventory) {
            glassSlot.forEach { i -> inventory.setItem(i, Material.GRAY_STAINED_GLASS_PANE) }
            inventory.setItem(4, Material.BARRIER)
        }
    }
}

fun Inventory.setItem(i: Int, material: Material) {
    setItem(i, ItemStack(material))
}