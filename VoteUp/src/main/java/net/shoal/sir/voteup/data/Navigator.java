package net.shoal.sir.voteup.data;

import lombok.NonNull;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.data.inventory.DetailsInventoryHolder;
import net.shoal.sir.voteup.data.inventory.ListInventoryHolder;
import net.shoal.sir.voteup.data.inventory.ParticipantInventoryHolder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class Navigator {

    private final Player user;

    public Navigator(@NonNull Player user) {
        this.user = user;
    }

    public final Map<Integer, GuiManager.GuiKey> navigator = new HashMap<>();
    public final Map<GuiManager.GuiKey, Object> dataMap = new HashMap<>();

    public void chain(GuiManager.GuiKey key, Object data) {
        navigator.put(navigator.size() + 1, key);
        dataMap.put(key, data);
        VoteUpAPI.GUI_MANAGER.navigatorMap.put(user.getUniqueId(), this);
    }

    public Inventory back() {
        if (navigator.isEmpty()) return null;
        GuiManager.GuiKey key = navigator.get(navigator.size());
        Object data = dataMap.get(key);
        Inventory inventory;

        switch (key) {
            case VOTE_PARTICIPANTS:
                inventory = new ParticipantInventoryHolder<>(data, user).getInventory();
                break;
            case VOTE_LIST:
                inventory = new ListInventoryHolder<>(data, user).getInventory();
                break;
            case VOTE_DETAILS:
                inventory = new DetailsInventoryHolder<>(data, user).getInventory();
                break;
            case VOTE_CREATE:
            case MAIN_MENU:
            default:
                inventory = null;
                break;
        }
        navigator.remove(navigator.size());
        dataMap.remove(key);
        VoteUpAPI.GUI_MANAGER.navigatorMap.put(user.getUniqueId(), this);
        return inventory;
    }

    public GuiManager.GuiKey last() {
        return navigator.get(navigator.size());
    }

    public void end() {
        VoteUpAPI.GUI_MANAGER.navigatorMap.remove(user.getUniqueId());
    }
}
