package net.shoal.sir.voteup.data.inventory;

import net.shoal.sir.voteup.VoteUp;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.InventoryExecutor;

public class CreateVoteInventory<T> implements InventoryExecutor {

    protected T data;
    protected Inventory inventory;
    private final PPlugin plugin;

    public CreateVoteInventory(T data) {
        this.plugin = VoteUp.getInstance();
        this.data = data;
        this.inventory = construct();
    }

    @Override
    public Inventory construct() {
        return null;
    }

    @Override
    public void execute(InventoryClickEvent inventoryClickEvent) {

    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public enum KeyWord {
        RESET,
        SET_ID,
        SET_TITLE,
        SWITCH_TYPE,
        SET_GOAL,
        MODIFY_DESCRIPTION,
        SET_DURATION,
        SET_CHOICE,
        MODIFY_AUTOCAST,
        SET_RESULT,
        VOTE_START
    }
}
