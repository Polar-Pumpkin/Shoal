package net.shoal.sir.voteup.gui.menu

import net.shoal.sir.voteup.enumeration.menu.CreationEnum
import net.shoal.sir.voteup.files.CreationFile
import net.shoal.sir.voteup.listener.holder.CustomInventoryHolder
import org.bukkit.Bukkit
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryEvent
import org.bukkit.inventory.Inventory

class CreationMenu : CustomInventoryHolder {
    var inv: Inventory

    init {
        val title = CreationFile.getString(CreationEnum.TITLE.path)
        inv = Bukkit.createInventory(this, 9 * CreationFile.getInt(CreationEnum.ROW.path), title)
        val items = CreationFile.config.getConfigurationSection("Items")!!.getKeys(false)
        items.forEach {
            val itemStack = CreationEnum.getItemStack(it)
            val x = CreationFile.getInt(CreationEnum.getPosition(it, 1))
            val y = CreationFile.getInt(CreationEnum.getPosition(it, 2))
            itemStack.itemMeta = itemStack.itemMeta.apply {
                this?.lore = CreationFile.getStringList(CreationEnum.getLore(it))
            }
            inv.setItem(x, itemStack)
        }
    }

    override fun run(event: InventoryEvent) {
        event as InventoryClickEvent
        event.isCancelled = true

    }

    override fun getInventory(): Inventory {
        return inv
    }
}