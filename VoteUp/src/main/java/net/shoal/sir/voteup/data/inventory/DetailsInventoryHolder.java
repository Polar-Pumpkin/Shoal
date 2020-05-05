package net.shoal.sir.voteup.data.inventory;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.config.ConfigManager;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.prompts.CollectReasonPrompt;
import net.shoal.sir.voteup.enums.BuiltinMsg;
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
import org.serverct.parrot.parrotx.data.InventoryExecutor;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.ConversationUtil;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;

import java.util.HashMap;
import java.util.Map;

public class DetailsInventoryHolder<T> implements InventoryExecutor {

    private final PPlugin plugin;
    private final Map<Integer, KeyWord> slotItemMap = new HashMap<>();
    protected T data;
    protected Inventory inventory;
    protected Player viewer;
    protected GuiManager.GuiKey lastGui;

    public DetailsInventoryHolder(T data, @NonNull Player player, GuiManager.GuiKey lastGui) {
        this.plugin = VoteUp.getInstance();
        this.data = data;
        this.viewer = player;
        this.lastGui = lastGui;
        this.inventory = construct();
    }

    @Override
    public Inventory construct() {
        Vote vote = (Vote) data;
        FileConfiguration file = VoteUpAPI.GUI_MANAGER.get(GuiManager.GuiKey.VOTE_DETAILS.filename);
        String title = "未初始化菜单";
        if (file == null) return Bukkit.createInventory(this, 0, title);
        title = VoteUpPlaceholder.parse(vote, file.getString("Settings.Title", BuiltinMsg.ERROR_GUI_TITLE.msg));
        Inventory inv = Bukkit.createInventory(this, file.getInt("Settings.Row", 0) * 9, title);

        ConfigurationSection itemSection = file.getConfigurationSection("Items");
        if (itemSection == null) return inv;
        for (String key : itemSection.getKeys(false)) {
            KeyWord keyWord = EnumUtil.valueOf(KeyWord.class, key);
            ConfigurationSection targetItemSection = itemSection.getConfigurationSection(key);

            if (targetItemSection == null) continue;
            ItemStack item = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, targetItemSection), vote);
            if (item.getType() == Material.PLAYER_HEAD) {
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                if (skull != null) {
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(vote.owner));
                    item.setItemMeta(skull);
                }
            }

            if (keyWord == KeyWord.BACK) ItemUtil.replace(item, "%BACK%", lastGui != null ? lastGui.guiname : "无");
            else if (keyWord == KeyWord.AUTOCAST && (plugin.pConfig.getConfig().getBoolean(ConfigManager.Path.AUTOCAST_ENABLE.path, true) || vote.autocast.isEmpty()))
                continue;
            else if ((keyWord == KeyWord.EDIT || keyWord == KeyWord.CANCEL) && !(VoteUpPerm.ADMIN.hasPermission(viewer) || vote.isOwner(viewer.getUniqueId())))
                continue;
            else if ((keyWord == KeyWord.VOTE_ACCEPT || keyWord == KeyWord.VOTE_NEUTRAL || keyWord == KeyWord.VOTE_REFUSE) && !VoteUpPerm.VOTE.hasPermission(viewer, EnumUtil.valueOf(Vote.Choice.class, keyWord.name().split("[_]")[1])))
                continue;

            ConfigurationSection targetSlotSection = targetItemSection.getConfigurationSection("Position");
            if (targetSlotSection == null) continue;

            String x = targetSlotSection.getString("X");
            String y = targetSlotSection.getString("Y");

            if (x == null || x.length() == 0 || y == null || y.length() == 0) continue;
            for (Integer slot : Position.getPositionList(x, y)) {
                inv.setItem(slot, item);
                slotItemMap.put(slot, keyWord);
            }
        }

        Vote.UserStatus status = vote.getUserStatus(viewer.getUniqueId());
        ItemStack acceptItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemStack refuseItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ConfigurationSection statusSection = file.getConfigurationSection("Status." + status.name());
        if (statusSection == null) return inv;
        for (String key : statusSection.getKeys(false)) {
            KeyWord keyWord = EnumUtil.valueOf(KeyWord.class, key);
            ConfigurationSection statusItemSection = statusSection.getConfigurationSection(key);

            if (statusItemSection == null) continue;
            ItemStack item = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, statusItemSection), vote);
            if (item.getType() == Material.PLAYER_HEAD) {
                SkullMeta skull = (SkullMeta) item.getItemMeta();
                if (skull != null) {
                    skull.setOwningPlayer(Bukkit.getOfflinePlayer(vote.owner));
                    item.setItemMeta(skull);
                }
            }

            if (status != Vote.UserStatus.DONE) {
                ConfigurationSection statusSlotSection = statusItemSection.getConfigurationSection("Position");
                if (statusSlotSection == null) continue;

                String x = statusSlotSection.getString("X");
                String y = statusSlotSection.getString("Y");

                if (x == null || x.length() == 0 || y == null || y.length() == 0) continue;
                for (Integer slot : Position.getPositionList(x, y)) {
                    inv.setItem(slot, item);
                    slotItemMap.put(slot, keyWord);
                }
            } else {
                switch (keyWord) {
                    case PROCESS_DONE:
                        acceptItem = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, statusItemSection), vote);
                        break;
                    case PROCESS_NOT:
                        refuseItem = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, statusItemSection), vote);
                        break;
                }
            }
        }

        if (status == Vote.UserStatus.DONE) {
            int acceptAmount = Math.min((int) Math.floor(9 * (vote.getProcess() / 100D)), 9);
            String acceptX = "1-" + acceptAmount;
            for (Integer slot : Position.getPositionList(acceptX, "3")) inv.setItem(slot, acceptItem);

            if (acceptAmount < 9) {
                String refuseX = (acceptAmount + 1) + "-9";
                for (Integer slot : Position.getPositionList(refuseX, "3")) inv.setItem(slot, refuseItem);
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
            case VOTE_ACCEPT:
                if (event.isRightClick())
                    ConversationUtil.start(plugin, user, new CollectReasonPrompt(user, vote, Vote.Choice.ACCEPT), 300);
                else voteWithoutReason(user, Vote.Choice.ACCEPT);
                refresh(inv);
                break;
            case VOTE_NEUTRAL:
                if (event.isRightClick())
                    ConversationUtil.start(plugin, user, new CollectReasonPrompt(user, vote, Vote.Choice.NEUTRAL), 300);
                else voteWithoutReason(user, Vote.Choice.NEUTRAL);
                refresh(inv);
                break;
            case VOTE_REFUSE:
                if (event.isRightClick())
                    ConversationUtil.start(plugin, user, new CollectReasonPrompt(user, vote, Vote.Choice.REFUSE), 300);
                else voteWithoutReason(user, Vote.Choice.REFUSE);
                refresh(inv);
                break;
            case VOTE_REASON:
                ConversationUtil.start(plugin, user, new CollectReasonPrompt(user, vote, vote.getChoice(user.getUniqueId())), 300);
                break;
            case BACK:
                if (lastGui != null) ; // TODO 返回的逻辑
                else BasicUtil.closeInventory(plugin, user);
                break;
            case PARTICIPANT:
                // TODO 投票参与者界面
                break;
            case CANCEL:
                // TODO 取消投票的实现
                break;
            case EDIT:
                // TODO 二次编辑投票的实现
                break;
            case OWNER:
            case DESCRIPTION:
            case AUTOCAST:
            case PROCESS_DONE:
            case PROCESS_NOT:
            default:
                break;
        }
    }

    private void voteWithoutReason(@NonNull Player user, Vote.Choice choice) {
        Vote vote = (Vote) data;
        VoteUpAPI.VOTE_MANAGER.vote(vote.voteID, user, choice, BuiltinMsg.REASON_NOT_YET.msg);
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
        PROCESS_DONE,
        PROCESS_NOT
    }
}
