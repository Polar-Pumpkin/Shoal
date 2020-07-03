package net.shoal.sir.voteup.data.inventory;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.data.Vote;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ListInventoryHolder<T> implements net.shoal.sir.voteup.data.ListInventoryHolder {
    public final static GuiManager.GuiKey GUI_KEY = GuiManager.GuiKey.VOTE_LIST;
    private final PPlugin plugin;
    private final Map<Integer, KeyWord> slotItemMap = new HashMap<>();
    private final Map<Integer, Vote> voteMap = new HashMap<>();
    protected T data;
    protected Inventory inventory;
    protected Player viewer;
    protected GuiManager.GuiKey lastGui;

    public ListInventoryHolder(T data, @NonNull Player player, GuiManager.GuiKey lastGui) {
        this.plugin = VoteUp.getInstance();
        this.data = data;
        this.viewer = player;
        this.lastGui = lastGui;
        this.inventory = construct();
    }

    @Override
    public FileConfiguration getFile() {
        return VoteUpAPI.GUI_MANAGER.get(GUI_KEY.filename);
    }

    @Override
    public Inventory construct() {
        List<Vote> vote = VoteUpAPI.VOTE_MANAGER.list((String) data);
        FileConfiguration file = getFile();
        Inventory inv = basicConstruct();

        ConfigurationSection itemSection = file.getConfigurationSection("Items");
        if (itemSection == null) return inv;
        for (String key : itemSection.getKeys(false)) {
            KeyWord keyWord = EnumUtil.valueOf(KeyWord.class, key);
            ConfigurationSection targetItemSection = itemSection.getConfigurationSection(key);

            if (targetItemSection == null) continue;
            ItemStack item = ItemUtil.build(plugin, targetItemSection);

            if (keyWord == KeyWord.BACK)
                ItemUtil.replace(item, "%BACK%", lastGui != null ? lastGui.guiname : "无");

            ConfigurationSection targetSlotSection = targetItemSection.getConfigurationSection("Position");
            if (targetSlotSection == null) continue;

            if (keyWord == KeyWord.VOTE && vote.isEmpty()) {
                ItemStack noResult = new ItemStack(Material.BARRIER);
                ConfigurationSection nothing = file.getConfigurationSection("Settings.Nothing");
                if (nothing != null) noResult = ItemUtil.build(plugin, nothing);

                String nothingX = targetSlotSection.getString("X", "5");
                String nothingY = targetSlotSection.getString("Y", "4");

                if (nothingX == null || nothingX.length() == 0 || nothingY == null || nothingY.length() == 0) break;
                for (Integer nothingSlot : Position.getPositionList(nothingX, nothingY))
                    inv.setItem(nothingSlot, noResult);
                continue;
            }

            Iterator<Vote> iterator = vote.iterator();

            String x = targetSlotSection.getString("X");
            String y = targetSlotSection.getString("Y");

            if (x == null || x.length() == 0 || y == null || y.length() == 0) continue;
            for (Integer slot : Position.getPositionList(x, y)) {
                if (keyWord == KeyWord.VOTE) {
                    if (!iterator.hasNext()) break;
                    ItemStack resultItem = item.clone();
                    Vote target = iterator.next();
                    inv.setItem(slot, VoteUpPlaceholder.applyPlaceholder(resultItem, target));
                    voteMap.put(slot, target);
                } else inv.setItem(slot, item);
                slotItemMap.put(slot, keyWord);
            }
        }
        return inv;
    }

    @Override
    public void execute(InventoryClickEvent event) {
        event.setCancelled(true);
        KeyWord keyWord = slotItemMap.get(event.getSlot());
        if (keyWord == null) return;

        Player user = (Player) event.getWhoClicked();
        Vote vote = voteMap.get(event.getSlot());
        Inventory inv = event.getInventory();

        switch (keyWord) {
            case BACK:
                // TODO 完善导航链条。
                break;
            case VOTE:
                BasicUtil.openInventory(plugin, user, new DetailsInventoryHolder<>(vote, user, GUI_KEY).getInventory());
                break;
            default:
                break;
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public enum KeyWord {
        BACK,
        VOTE
    }
}
