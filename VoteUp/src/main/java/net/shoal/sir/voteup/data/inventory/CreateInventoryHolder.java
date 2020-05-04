package net.shoal.sir.voteup.data.inventory;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.prompts.SetChoicePrompt;
import net.shoal.sir.voteup.data.prompts.SetDurationPrompt;
import net.shoal.sir.voteup.data.prompts.SetResultPrompt;
import net.shoal.sir.voteup.data.prompts.SetTitlePrompt;
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
import org.serverct.parrot.parrotx.utils.*;

import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

public class CreateInventoryHolder<T> implements InventoryExecutor {

    private final PPlugin plugin;
    private final Map<Integer, KeyWord> slotItemMap = new HashMap<>();
    protected T data;
    protected Inventory inventory;
    protected Player viewer;

    public CreateInventoryHolder(T data, @NonNull Player player) {
        this.plugin = VoteUp.getInstance();
        this.data = data;
        this.viewer = player;
        this.inventory = construct();
    }

    @Override
    public Inventory construct() {
        Vote vote = (Vote) data;
        FileConfiguration file = VoteUpAPI.GUI_MANAGER.get(GuiManager.GuiKey.VOTE_CREATE.filename);
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

            if (keyWord != null && keyWord.target != null && !VoteUpPerm.EDIT.hasPermission(viewer, keyWord.target)) {
                ConfigurationSection noPerm = file.getConfigurationSection("Settings.NoPerm");
                if (noPerm != null) item = ItemUtil.build(plugin, noPerm);
                else item = new ItemStack(Material.BARRIER);
            }

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
            case RESET:
                vote.init();
                refresh(inv);
                break;
            case SET_TITLE:
                if (validate(user, (event.isLeftClick() ? Vote.Data.TITLE : Vote.Data.ID))) break;
                ConversationUtil.start(plugin, user, new SetTitlePrompt(user, vote), 300);
                break;
            case SWITCH_TYPE:
                if (validate(user, Vote.Data.TYPE)) break;
                vote.type = Vote.Type.mode(vote.type.mode + (event.isLeftClick() ? 1 : -1));
                refresh(inv);
                break;
            case SET_GOAL:
                if (validate(user, Vote.Data.GOAL)) break;
                vote.goal += event.isLeftClick() ? (event.isShiftClick() ? 5 : 1) : (event.isShiftClick() ? -5 : -1);
                refresh(inv);
                break;
            case MODIFY_DESCRIPTION:
                if (validate(user, Vote.Data.DESCRIPTION)) break;
                BasicUtil.closeInventory(plugin, user);
                JsonChatUtil.sendEditableList(
                        user,
                        vote.description,
                        VoteUpPlaceholder.parse(vote, BuiltinMsg.VOTE_VALUE_DESCRIPTION.msg),
                        "&a&l[插入] ",
                        "/vote modify desc add ",
                        "&e&l[编辑] ",
                        "/vote modify desc set ",
                        "&c&l[删除] ",
                        "/vote modify desc del ",
                        "&7[&a&l>>> &7返回菜单]",
                        "/vote create back"
                );
                break;
            case SET_DURATION:
                if (validate(user, Vote.Data.DURATION)) break;
                ConversationUtil.start(plugin, user, new SetDurationPrompt(user, vote), 300);
                break;
            case SET_CHOICE:
                if (validate(user, Vote.Data.CHOICE)) break;
                ConversationUtil.start(plugin, user, new SetChoicePrompt(user, vote, Vote.Choice.ACCEPT), 300);
                break;
            case MODIFY_AUTOCAST:
                if (validate(user, Vote.Data.AUTOCAST)) break;
                BasicUtil.closeInventory(plugin, user);
                JsonChatUtil.sendEditableList(
                        user,
                        vote.autocast,
                        VoteUpPlaceholder.parse(vote, BuiltinMsg.VOTE_VALUE_AUTOCAST.msg),
                        "&a&l[插入] ",
                        "/vote modify autocast add ",
                        "&e&l[编辑] ",
                        "/vote modify autocast set ",
                        "&c&l[删除] ",
                        "/vote modify autocast del ",
                        "&7[&a&l>>> &7返回菜单]",
                        "/vote create back"
                );
                break;
            case SET_RESULT:
                if (validate(user, Vote.Data.RESULT)) break;
                ConversationUtil.start(plugin, user, new SetResultPrompt(user, vote, Vote.Result.PASS), 300);
                break;
            case VOTE_START:
                BasicUtil.closeInventory(plugin, user);
                VoteUpAPI.VOTE_MANAGER.start(user);
                break;
            case DRAFT_DELETE:
                BasicUtil.closeInventory(plugin, user);
                VoteUpAPI.VOTE_MANAGER.delete(vote.voteID);
                break;
        }
        VoteUpAPI.SOUND.ding(user);
    }

    private boolean validate(@NonNull Player user, Vote.Data type) {
        if (!VoteUpPerm.EDIT.hasPermission(user, type)) {
            VoteUpAPI.SOUND.fail(user);
            I18n.send(user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, BuiltinMsg.ERROR_EDIT_NO_PERM.msg));
            return true;
        }
        return false;
    }

    private void refresh(@NonNull Inventory inventory) {
        Inventory inv = construct();
        ListIterator<ItemStack> iterator = inv.iterator();
        while (iterator.hasNext()) inventory.setItem(iterator.nextIndex(), iterator.next());
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public enum KeyWord {
        RESET(null),
        SET_TITLE(Vote.Data.TITLE),
        SWITCH_TYPE(Vote.Data.TYPE),
        SET_GOAL(Vote.Data.GOAL),
        MODIFY_DESCRIPTION(Vote.Data.DESCRIPTION),
        SET_DURATION(Vote.Data.DURATION),
        SET_CHOICE(Vote.Data.CHOICE),
        MODIFY_AUTOCAST(Vote.Data.AUTOCAST),
        SET_RESULT(Vote.Data.RESULT),
        VOTE_START(null),
        DRAFT_DELETE(null);

        public Vote.Data target;

        KeyWord(Vote.Data target) {
            this.target = target;
        }
    }
}
