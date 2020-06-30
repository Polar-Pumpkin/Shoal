package net.shoal.sir.voteup.data.inventory

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.api.VoteUpPlaceholder
import net.shoal.sir.voteup.config.ConfPath
import net.shoal.sir.voteup.config.GuiManager.GuiKey
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.data.prompts.*
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.data.InventoryExecutor
import org.serverct.parrot.parrotx.enums.Position
import org.serverct.parrot.parrotx.utils.*
import java.util.*

class CreateInventoryHolder<T>(data: T, @NonNull player: Player) : InventoryExecutor {
    private val plugin: PPlugin
    private val slotItemMap: MutableMap<Int, KeyWord?> = HashMap()
    protected var data: T
    protected var inventory: Inventory
    protected var viewer: Player
    override fun construct(): Inventory {
        val vote = data as Vote
        val file = VoteUpAPI.GUI_MANAGER!![GuiKey.VOTE_CREATE.filename]
        var title: String? = "未初始化菜单"
        if (file == null) return Bukkit.createInventory(this, 0, title)
        title = VoteUpPlaceholder.parse(vote, file.getString("Settings.Title", Msg.ERROR_GUI_TITLE.msg))
        val inv: Inventory = Bukkit.createInventory(this, file.getInt("Settings.Row", 0) * 9, title)
        val itemSection = file.getConfigurationSection("Items") ?: return inv
        for (key in itemSection.getKeys(false)) {
            val keyWord = EnumUtil.valueOf(KeyWord::class.java, key)
            val targetItemSection = itemSection.getConfigurationSection(key!!) ?: continue
            var item = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, targetItemSection), vote)
            if (item!!.type == Material.PLAYER_HEAD) {
                val skull = item.itemMeta as SkullMeta?
                if (skull != null) {
                    skull.owningPlayer = Bukkit.getOfflinePlayer(vote.owner!!)
                    item.itemMeta = skull
                }
            }
            if (keyWord != null && keyWord.target != null && !VoteUpPerm.EDIT.hasPermission(viewer, keyWord.target!!)) {
                val noPerm = file.getConfigurationSection("Settings.NoPerm")
                item = if (noPerm != null) ItemUtil.build(plugin, noPerm) else ItemStack(Material.BARRIER)
                when (keyWord) {
                    KeyWord.ALLOW_EDIT, KeyWord.PUBLIC_MODE, KeyWord.ALLOW_ANONYMOUS -> {
                        var unlock: Boolean
                        var enable: Boolean
                        when (keyWord) {
                            KeyWord.ALLOW_ANONYMOUS -> {
                                unlock = plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_ALLOW_ANONYMOUS.path, true)
                                enable = vote.allowAnonymous
                            }
                            KeyWord.PUBLIC_MODE -> {
                                unlock = plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_ALLOW_PUBLIC.path, true)
                                enable = vote.isPublic
                            }
                            KeyWord.ALLOW_EDIT -> {
                                unlock = plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_ALLOW_EDIT_PARTICIPANT.path, true)
                                enable = vote.allowEdit
                            }
                            else -> {
                                unlock = false
                                enable = false
                            }
                        }
                        ItemUtil.replace(item, "%feature%", if (unlock) Msg.VOTE_FEATURE_SWITCH.msg else Msg.VOTE_FEATURE_LOCK.msg)
                        if (!unlock) {
                            item.type = Material.BARRIER
                            break
                        }
                        if (item.type.name.endsWith("DYE")) if (!enable) item.type = Material.LIGHT_GRAY_DYE
                        if (enable) {
                            val meta = item.itemMeta
                            if (meta != null) {
                                meta.addEnchant(Enchantment.LUCK, 10, true)
                                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
                                item.itemMeta = meta
                            }
                        }
                    }
                }
            }
            val targetSlotSection = targetItemSection.getConfigurationSection("Position") ?: continue
            val x = targetSlotSection.getString("X")
            val y = targetSlotSection.getString("Y")
            if (x == null || x.length == 0 || y == null || y.length == 0) continue
            for (slot in Position.getPositionList(x, y)) {
                inv.setItem(slot, item)
                slotItemMap[slot] = keyWord
            }
        }
        return inv
    }

    override fun execute(event: InventoryClickEvent) {
        event.isCancelled = true
        val keyWord = slotItemMap.getOrDefault(event.slot, null) ?: return
        val user = event.whoClicked as Player
        val vote = data as Vote
        val inv = event.inventory
        when (keyWord) {
            KeyWord.RESET -> {
                vote.init()
                refresh(inv)
            }
            KeyWord.SET_TITLE -> {
                if (validate(user, if (event.isLeftClick) Vote.Data.TITLE else Vote.Data.ID)) break
                ConversationUtil.start(plugin, user, if (event.isLeftClick) SetTitlePrompt(user, vote) else SetIDPrompt(user, vote), 300)
            }
            KeyWord.SWITCH_TYPE -> {
                if (validate(user, Vote.Data.TYPE)) break
                vote.type = Vote.Type.Companion.mode(vote.type!!.mode + if (event.isLeftClick) 1 else -1)
                refresh(inv)
            }
            KeyWord.SET_GOAL -> {
                if (validate(user, Vote.Data.GOAL)) break
                vote.goal += if (event.isLeftClick) if (event.isShiftClick) 5 else 1 else if (event.isShiftClick) -5 else -1
                refresh(inv)
            }
            KeyWord.MODIFY_DESCRIPTION -> {
                if (validate(user, Vote.Data.DESCRIPTION)) break
                BasicUtil.closeInventory(plugin, user)
                JsonChatUtil.sendEditableList(
                        user,
                        vote.description,
                        VoteUpPlaceholder.parse(vote, Msg.VOTE_VALUE_DESCRIPTION.msg),
                        "&a&l[插入] ",
                        "/vote modify desc add ",
                        "&e&l[编辑] ",
                        "/vote modify desc set ",
                        "&c&l[删除] ",
                        "/vote modify desc del ",
                        "&7[&a&l>>> &7返回菜单]",
                        "/vote create back"
                )
            }
            KeyWord.ALLOW_ANONYMOUS -> {
                if (validate(user, Vote.Data.ANONYMOUS) || !plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_ALLOW_ANONYMOUS.path, true)) break
                vote.allowEdit = !vote.allowAnonymous
                refresh(inv)
            }
            KeyWord.PUBLIC_MODE -> {
                if (validate(user, Vote.Data.PUBLIC) || !plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_ALLOW_PUBLIC.path, true)) break
                vote.isPublic = !vote.isPublic
                refresh(inv)
            }
            KeyWord.SET_DURATION -> {
                if (validate(user, Vote.Data.DURATION)) break
                ConversationUtil.start(plugin, user, SetDurationPrompt(user, vote), 300)
            }
            KeyWord.SET_CHOICE -> {
                if (validate(user, Vote.Data.CHOICE)) break
                ConversationUtil.start(plugin, user, SetChoicePrompt(user, vote, Vote.Choice.ACCEPT), 300)
            }
            KeyWord.MODIFY_AUTOCAST -> {
                if (validate(user, Vote.Data.AUTOCAST)) break
                BasicUtil.closeInventory(plugin, user)
                JsonChatUtil.sendEditableList(
                        user,
                        vote.autocast,
                        VoteUpPlaceholder.parse(vote, Msg.VOTE_VALUE_AUTOCAST.msg),
                        "&a&l[插入] ",
                        "/vote modify autocast add ",
                        "&e&l[编辑] ",
                        "/vote modify autocast set ",
                        "&c&l[删除] ",
                        "/vote modify autocast del ",
                        "&7[&a&l>>> &7返回菜单]",
                        "/vote create back"
                )
            }
            KeyWord.SET_RESULT -> {
                if (validate(user, Vote.Data.RESULT)) break
                ConversationUtil.start(plugin, user, SetResultPrompt(user, vote, Vote.Result.PASS), 300)
            }
            KeyWord.ALLOW_EDIT -> {
                if (validate(user, Vote.Data.EDITABLE) || !plugin.pConfig.config.getBoolean(ConfPath.Path.SETTINGS_ALLOW_EDIT_PARTICIPANT.path, true)) break
                vote.allowEdit = !vote.allowEdit
                refresh(inv)
            }
            KeyWord.VOTE_START -> {
                BasicUtil.closeInventory(plugin, user)
                VoteUpAPI.VOTE_MANAGER!!.start(user)
            }
            KeyWord.DRAFT_DELETE -> {
                BasicUtil.closeInventory(plugin, user)
                VoteUpAPI.VOTE_MANAGER!!.delete(vote.voteID!!)
            }
        }
        VoteUpAPI.SOUND!!.ding(user)
    }

    private fun validate(@NonNull user: Player, type: Vote.Data): Boolean {
        if (!VoteUpPerm.EDIT.hasPermission(user, type)) {
            VoteUpAPI.SOUND!!.fail(user)
            I18n.send(user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg))
            return true
        }
        return false
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    enum class KeyWord(var target: Vote.Data?) {
        RESET(null), SET_TITLE(Vote.Data.TITLE), SWITCH_TYPE(Vote.Data.TYPE), SET_GOAL(Vote.Data.GOAL), MODIFY_DESCRIPTION(Vote.Data.DESCRIPTION), ALLOW_ANONYMOUS(Vote.Data.ANONYMOUS), PUBLIC_MODE(Vote.Data.PUBLIC), SET_DURATION(Vote.Data.DURATION), SET_CHOICE(Vote.Data.CHOICE), MODIFY_AUTOCAST(Vote.Data.AUTOCAST), SET_RESULT(Vote.Data.RESULT), ALLOW_EDIT(Vote.Data.EDITABLE), VOTE_START(null), DRAFT_DELETE(null);

    }

    init {
        plugin = PPlugin.getInstance()
        this.data = data
        viewer = player
        inventory = construct()
    }
}