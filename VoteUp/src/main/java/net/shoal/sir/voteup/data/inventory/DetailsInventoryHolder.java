package net.shoal.sir.voteup.data.inventory;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import org.bukkit.entity.Player;
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
    protected Player owner;

    public DetailsInventoryHolder(T data, @NonNull Player player) {
        this.plugin = VoteUp.getInstance();
        this.data = data;
        this.owner = player;
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
        OWNER,
        DESCRIPTION,
        AUTOCAST,
        VOTE_ACCEPT,
        VOTE_NEUTRAL,
        VOTE_REFUSE,
        VOTE_REASON,
        PARTICIPANT,
        EDIT,
        CANCEL,
        BACK,
    }

    public enum Status {
        FIRST,
        NO_REASON,
        DONE
    }
}
