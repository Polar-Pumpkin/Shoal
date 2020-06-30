package net.shoal.sir.voteup.enumeration.menu

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class CreationEnum(val path: String) {
    TITLE("Settings.Title"),
    ROW("Settings.Row"),
    ITEM("Items");

    companion object {
        fun getItemStack(name: String): ItemStack {
            val material = Material.getMaterial("Items.$name.ItemStack.Material")
            val item = Material.getMaterial("")?.let { ItemStack(it) }
            return item ?: ItemStack(Material.STONE)
        }

        fun getPosition(name: String, axis: Int): String {
            val path = "Items.$name.Position."
            return when (axis) {
                1 -> "${path}X"
                2 -> "${path}Y"
                else -> "Empty Path"
            }
        }

        fun getDisplay(name: String): String {
            return "Items.$name.ItemStack.Display"
        }

        fun getLore(name: String) : String {
            return "ITEM.$name.ItemStack.Lore"
        }
    }

}