package net.shoal.sir.voteup.data.inventory;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.api.VoteUpSound;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.VoteInventoryExecutor;
import net.shoal.sir.voteup.data.prompts.*;
import net.shoal.sir.voteup.enums.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.enums.Position;
import org.serverct.parrot.parrotx.utils.*;

import java.util.HashMap;
import java.util.Map;

public class CreateInventoryHolder<T> implements VoteInventoryExecutor {
    public final static GuiManager.GuiKey GUI_KEY = GuiManager.GuiKey.VOTE_CREATE;
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
    public FileConfiguration getFile() {
        return VoteUpAPI.GUI_MANAGER.get(GUI_KEY.filename);
    }

    @Override
    public Vote getVote() {
        return (Vote) data;
    }

    @Override
    public Inventory construct() {
        FileConfiguration file = getFile();
        Vote vote = getVote();
        Inventory inv = basicConstruct();

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

            if (keyWord != null && keyWord.target != null) {
                switch (keyWord) {
                    case ALLOW_EDIT:
                    case PUBLIC_MODE:
                    case ALLOW_ANONYMOUS:
                        boolean unlock;
                        boolean enable;
                        switch (keyWord) {
                            case ALLOW_ANONYMOUS:
                                unlock = VoteUpAPI.CONFIG.allow_anonymous;
                                enable = vote.allowAnonymous;
                                break;
                            case PUBLIC_MODE:
                                unlock = VoteUpAPI.CONFIG.allow_public;
                                enable = vote.isPublic;
                                break;
                            case ALLOW_EDIT:
                                unlock = VoteUpAPI.CONFIG.allow_edit_participant;
                                enable = vote.allowEdit;
                                break;
                            default:
                                unlock = false;
                                enable = false;
                                break;
                        }

                        ItemUtil.replace(item, "%feature%", unlock ? Msg.VOTE_FEATURE_SWITCH.msg : Msg.VOTE_FEATURE_LOCK.msg);

                        if (!unlock) {
                            item.setType(Material.BARRIER);
                            break;
                        }

                        if (item.getType().name().endsWith("DYE")) if (!enable) item.setType(Material.GRAY_DYE);
                        if (enable) {
                            ItemMeta meta = item.getItemMeta();
                            if (meta != null) {
                                meta.addEnchant(Enchantment.LUCK, 10, true);
                                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                                item.setItemMeta(meta);
                            }
                        }
                        break;
                }

                if (!VoteUpPerm.EDIT.hasPermission(viewer, keyWord.target)) {
                    ConfigurationSection noPerm = file.getConfigurationSection("Settings.NoPerm");
                    if (noPerm != null) item = ItemUtil.build(plugin, noPerm);
                    else item = new ItemStack(Material.BARRIER);
                }
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
        KeyWord keyWord = slotItemMap.get(event.getSlot());
        if (keyWord == null) return;

        Player user = (Player) event.getWhoClicked();
        Vote vote = getVote();
        Inventory inv = event.getInventory();

        switch (keyWord) {
            case RESET:
                vote.init();
                refresh(inv);
                break;
            case SET_TITLE:
                if (validate(user, event.isLeftClick() ? Vote.Data.TITLE : Vote.Data.ID)) break;
                ConversationUtil.start(plugin, user, event.isLeftClick() ? new SetTitlePrompt(user, vote) : new SetIDPrompt(user, vote), 300);
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
                        VoteUpPlaceholder.parse(vote, String.format(Msg.VOTE_VALUE_DESCRIPTION.msg, vote.description.size())),
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
            case ALLOW_ANONYMOUS:
                if (validate(user, Vote.Data.ANONYMOUS) || !VoteUpAPI.CONFIG.allow_anonymous)
                    break;
                vote.allowAnonymous = !vote.allowAnonymous;
                refresh(inv);
                break;
            case PUBLIC_MODE:
                if (validate(user, Vote.Data.PUBLIC) || !VoteUpAPI.CONFIG.allow_public)
                    break;
                vote.isPublic = !vote.isPublic;
                refresh(inv);
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
                        VoteUpPlaceholder.parse(vote, String.format(Msg.VOTE_VALUE_AUTOCAST.msg, vote.autocast.size())),
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
            case ALLOW_EDIT:
                if (validate(user, Vote.Data.EDITABLE) || !VoteUpAPI.CONFIG.allow_edit_participant)
                    break;
                vote.allowEdit = !vote.allowEdit;
                refresh(inv);
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
        VoteUpSound.ding(user);
    }

    private boolean validate(@NonNull Player user, Vote.Data type) {
        if (!VoteUpPerm.EDIT.hasPermission(user, type)) {
            VoteUpSound.fail(user);
            I18n.send(user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg));
            return true;
        }
        return false;
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
        ALLOW_ANONYMOUS(Vote.Data.ANONYMOUS),
        PUBLIC_MODE(Vote.Data.PUBLIC),
        SET_DURATION(Vote.Data.DURATION),
        SET_CHOICE(Vote.Data.CHOICE),
        MODIFY_AUTOCAST(Vote.Data.AUTOCAST),
        SET_RESULT(Vote.Data.RESULT),
        ALLOW_EDIT(Vote.Data.EDITABLE),
        VOTE_START(null),
        DRAFT_DELETE(null);

        public Vote.Data target;

        KeyWord(Vote.Data target) {
            this.target = target;
        }
    }
}
