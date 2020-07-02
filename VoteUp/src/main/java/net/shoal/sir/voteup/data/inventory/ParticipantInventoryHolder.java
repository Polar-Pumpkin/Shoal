package net.shoal.sir.voteup.data.inventory;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.VoteInventoryExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;
import org.serverct.parrot.parrotx.utils.TimeUtil;

import java.util.*;

public class ParticipantInventoryHolder<T> implements VoteInventoryExecutor {
    public final static GuiManager.GuiKey GUI_KEY = GuiManager.GuiKey.VOTE_PARTICIPANTS;
    private final PPlugin plugin;
    private final Map<Integer, KeyWord> slotItemMap = new HashMap<>();
    private final Map<Integer, UUID> participantMap = new HashMap<>();
    private final Map<String, Material> anonymousItem = new HashMap<String, Material>() {
        {
            put("小怕", Material.CREEPER_HEAD);
            put("苦力怕", Material.CREEPER_HEAD);
            put("社会你怕哥", Material.CREEPER_HEAD);
            put("怕怕", Material.CREEPER_HEAD);
            put("爬", Material.CREEPER_HEAD);

            put("小僵", Material.ZOMBIE_HEAD);
            put("僵尸", Material.ZOMBIE_HEAD);
            put("丧尸", Material.ZOMBIE_HEAD);
            put("僵僵", Material.ZOMBIE_HEAD);
            put("尸", Material.ZOMBIE_HEAD);

            put("小白", Material.SKELETON_SKULL);
            put("骷髅", Material.SKELETON_SKULL);
            put("Sans", Material.SKELETON_SKULL);
            put("白白", Material.SKELETON_SKULL);
            put("嘎吱嘎吱", Material.SKELETON_SKULL);

            put("小黑", Material.WITHER_SKELETON_SKULL);
            put("凋零骷髅", Material.WITHER_SKELETON_SKULL);
            put("破碎の心 :(", Material.WITHER_SKELETON_SKULL);
            put("黑黑", Material.WITHER_SKELETON_SKULL);
            put("神必剑客", Material.WITHER_SKELETON_SKULL);

            put("草", Material.GRASS);
            put("花", Material.SUNFLOWER);
        }
    };
    protected T data;
    protected Inventory inventory;
    protected Player viewer;
    protected GuiManager.GuiKey lastGui;

    public ParticipantInventoryHolder(T data, @NonNull Player player, GuiManager.GuiKey lastGui) {
        this.plugin = VoteUp.getInstance();
        this.data = data;
        this.viewer = player;
        this.lastGui = lastGui;
        this.inventory = construct();
    }

    @Override
    public FileConfiguration getFile() {
        return VoteUpAPI.GUI_MANAGER.get(GuiManager.GuiKey.VOTE_PARTICIPANTS.filename);
    }

    @Override
    public Vote getVote() {
        return (Vote) data;
    }

    @Override
    public Inventory construct() {
        Vote vote = getVote();
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

            List<Vote.Participant> participantList = vote.participants;
            Iterator<Vote.Participant> iterator = participantList.iterator();

            ConfigurationSection targetSlotSection = targetItemSection.getConfigurationSection("Position");
            if (targetSlotSection == null) continue;

            String x = targetSlotSection.getString("X");
            String y = targetSlotSection.getString("Y");

            if (x == null || x.length() == 0 || y == null || y.length() == 0) continue;
            for (Integer slot : Position.getPositionList(x, y)) {
                if (keyWord == KeyWord.PARTICIPANT) {
                    if (!iterator.hasNext()) break;
                    ItemStack resultItem = item.clone();
                    Vote.Participant user = iterator.next();
                    Map<String, String> variableMap = new HashMap<String, String>() {
                        {
                            put("participant", Bukkit.getOfflinePlayer(user.uuid).getName());
                            put("time", TimeUtil.getDescriptionTimeFromTimestamp(user.timestamp) + " &8&o" + TimeUtil.getChineseDateFormat(new Date(user.timestamp)));
                            put("choice", vote.choices.get(user.choice));
                            put("reason", user.reason);
                        }
                    };

                    if (user.anonymous) {
                        String[] names = anonymousItem.keySet().toArray(new String[0]);
                        String name = names[new Random().nextInt(names.length)];
                        variableMap.put("participant", VoteUpPerm.ANONYMOUS.hasPermission(viewer) ? name + " &8&o(" + variableMap.get("participant") + " 已匿名)" : name);
                        resultItem.setType(anonymousItem.get(name));
                    } else if (resultItem.getType() == Material.PLAYER_HEAD) {
                        SkullMeta skull = (SkullMeta) resultItem.getItemMeta();
                        if (skull != null) {
                            skull.setOwningPlayer(Bukkit.getOfflinePlayer(user.uuid));
                            resultItem.setItemMeta(skull);
                        }
                    }

                    variableMap.forEach((k, v) -> ItemUtil.replace(resultItem, "%" + k + "%", v));
                    inv.setItem(slot, VoteUpPlaceholder.applyPlaceholder(resultItem, vote));
                    participantMap.put(slot, user.uuid);
                } else inv.setItem(slot, VoteUpPlaceholder.applyPlaceholder(item, vote));
                slotItemMap.put(slot, keyWord);
            }
        }

        return inv;
    }

    @Override
    public void execute(InventoryClickEvent event) {
        event.setCancelled(true);
        KeyWord keyWord = slotItemMap.getOrDefault(event.getSlot(), null);
        if (keyWord == null) return;

        Player user = (Player) event.getWhoClicked();
        Vote vote = (Vote) data;
        Inventory inv = event.getInventory();

        switch (keyWord) {
            case BACK:
                if (lastGui != null) {
                    switch (lastGui) {
                        case VOTE_DETAILS:
                            BasicUtil.openInventory(plugin, user, new DetailsInventoryHolder<>(vote, user, GUI_KEY).getInventory());
                            break;
                        case MAIN_MENU:
                        case VOTE_CREATE:
                        case VOTE_LIST:
                        case VOTE_PARTICIPANTS:
                        default:
                            break;
                    }
                } else BasicUtil.closeInventory(plugin, user);
                break;
            case PARTICIPANT:
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
        PARTICIPANT
    }
}
