package net.shoal.sir.voteup.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import org.bukkit.inventory.ItemStack;

public @Data @AllArgsConstructor class MenuItem {

    private String id;
    private ItemStack item;
    private MenuItemExecutor executor;
    private int xPosition;
    private int yPosition;

}
