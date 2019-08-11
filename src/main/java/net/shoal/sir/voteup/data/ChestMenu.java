package net.shoal.sir.voteup.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.shoal.sir.voteup.util.PlaceholderUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public @Data @AllArgsConstructor class ChestMenu implements Cloneable {

    private String id;
    private String title;
    private int row;
//    private Inventory inventory;
    private List<MenuItem> items;
    private Sound open;
    private Sound close;

    public ChestMenu(ChestMenu menu) {
        this.id = menu.getId();
        this.title = menu.getTitle();
        this.row = menu.getRow();
//        this.inventory = menu.getInventory();
        this.items = menu.getItems();
        this.open = menu.getOpen();
        this.close = menu.getClose();
    }

    public MenuItem getItem(ItemStack item, Vote data) {
        for(MenuItem menuItem : items) {
            if(item != null && item.getType() != Material.AIR) {
                if(item.equals(PlaceholderUtil.applyPlaceholder(menuItem.getItem().clone(), data))) {
                    return menuItem;
                }
            }
        }
        return null;
    }

    @Override
    public ChestMenu clone() {
        try {
            return (ChestMenu) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
