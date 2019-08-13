package net.shoal.sir.voteup.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import org.bukkit.inventory.ItemStack;

public @Data @AllArgsConstructor class MenuItem implements Cloneable{

    private String id;
    private ItemStack item;
    private MenuItemExecutor executor;
    private String xPosition;
    private String yPosition;
    private String permission;

    @Override
    public MenuItem clone() {
        MenuItem clone = null;
        try {
            clone = (MenuItem) super.clone();
            clone.item = this.item.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }
}
