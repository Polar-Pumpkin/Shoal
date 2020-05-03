package net.shoal.sir.voteup.data.inventory;

import net.shoal.sir.voteup.VoteUp;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.InventoryExecutor;

import java.util.HashMap;
import java.util.Map;

public class DetailsInventoryHolder<T> implements InventoryExecutor {

    private final PPlugin plugin;
    private final Map<Integer, CreateInventoryHolder.KeyWord> slotItemMap = new HashMap<>();
    protected T data;
    protected Inventory inventory;

    public DetailsInventoryHolder(T data) {
        this.plugin = VoteUp.getInstance();
        this.data = data;
        this.inventory = construct();
    }

    @Override
    public Inventory construct() {
        return null;
    }

    @Override
    public void execute(InventoryClickEvent event) {

    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public enum KeyWord {

    }
}
