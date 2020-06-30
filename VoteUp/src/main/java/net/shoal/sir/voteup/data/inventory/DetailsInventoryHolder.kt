package net.shoal.sir.voteup.data.inventory

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.api.VoteUpPlaceholder
import net.shoal.sir.voteup.config.ConfPath
import net.shoal.sir.voteup.config.GuiManager.GuiKey
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.data.Vote.UserStatus
import net.shoal.sir.voteup.data.prompts.CollectReasonPrompt
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.data.InventoryExecutor
import org.serverct.parrot.parrotx.enums.Position
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.ConversationUtil
import org.serverct.parrot.parrotx.utils.EnumUtil
import org.serverct.parrot.parrotx.utils.ItemUtil
import java.util.*

class DetailsInventoryHolder<T>(data: T, @NonNull player: Player, lastGui: GuiKey?) : InventoryExecutor {
    private val plugin: PPlugin
    private val slotItemMap: MutableMap<Int, KeyWord> = HashMap()
    protected var data: T
    protected var inventory: Inventory
    protected var viewer: Player
    protected var lastGui: GuiKey?
    override fun construct(): Inventory {
        val vote = data as Vote
        val file = VoteUpAPI.GUI_MANAGER!![GuiKey.VOTE_DETAILS.filename]
        var title: String? = "未初始化菜单"
        if (file == null) return Bukkit.createInventory(this, 0, title)
        title = VoteUpPlaceholder.parse(vote, file.getString("Settings.Title", Msg.ERROR_GUI_TITLE.msg))
        val inv: Inventory = Bukkit.createInventory(this, file.getInt("Settings.Row", 0) * 9, title)
        val itemSection = file.getConfigurationSection("Items") ?: return inv
        for (key in itemSection.getKeys(false)) {
            val keyWord = EnumUtil.valueOf(KeyWord::class.java, key)
            val targetItemSection = itemSection.getConfigurationSection(key!!) ?: continue
            val item = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, targetItemSection), vote)
            if (item!!.type == Material.PLAYER_HEAD) {
                val skull = item.itemMeta as SkullMeta?
                if (skull != null) {
                    skull.owningPlayer = Bukkit.getOfflinePlayer(vote.owner!!)
                    item.itemMeta = skull
                }
            }
            if (keyWord == KeyWord.BACK) ItemUtil.replace(item, "%BACK%", if (lastGui != null) lastGui!!.guiname else "无") else if (keyWord == KeyWord.AUTOCAST && !(plugin.pConfig.config.getBoolean(ConfPath.Path.AUTOCAST_ENABLE.path, true) || vote.autocast!!.isEmpty())) continue else if ((keyWord == KeyWord.EDIT || keyWord == KeyWord.CANCEL) && !(VoteUpPerm.ADMIN.hasPermission(viewer) || vote.isOwner(viewer.uniqueId))) continue else if ((keyWord == KeyWord.VOTE_ACCEPT || keyWord == KeyWord.VOTE_NEUTRAL || keyWord == KeyWord.VOTE_REFUSE) && !VoteUpPerm.VOTE.hasPermission(viewer, EnumUtil.valueOf(Vote.Choice::class.java, keyWord.name.split("[_]").toTypedArray()[1]))) continue else if (keyWord == KeyWord.PARTICIPANT && !vote.isPublic) continue
            val targetSlotSection = targetItemSection.getConfigurationSection("Position") ?: continue
            val x = targetSlotSection.getString("X")
            val y = targetSlotSection.getString("Y")
            if (x == null || x.length == 0 || y == null || y.length == 0) continue
            for (slot in Position.getPositionList(x, y)) {
                inv.setItem(slot, item)
                slotItemMap[slot] = keyWord
            }
        }
        val status = vote.getUserStatus(viewer.uniqueId)
        var acceptItem: ItemStack? = ItemStack(Material.LIME_STAINED_GLASS_PANE)
        var refuseItem: ItemStack? = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val statusSection = file.getConfigurationSection("Status." + status!!.name) ?: return inv
        for (key in statusSection.getKeys(false)) {
            val keyWord = EnumUtil.valueOf(KeyWord::class.java, key)
            val statusItemSection = statusSection.getConfigurationSection(key!!) ?: continue
            val item = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, statusItemSection), vote)
            if (item!!.type == Material.PLAYER_HEAD) {
                val skull = item.itemMeta as SkullMeta?
                if (skull != null) {
                    skull.owningPlayer = Bukkit.getOfflinePlayer(vote.owner!!)
                    item.itemMeta = skull
                }
            }
            if (status != UserStatus.DONE) {
                val statusSlotSection = statusItemSection.getConfigurationSection("Position") ?: continue
                val x = statusSlotSection.getString("X")
                val y = statusSlotSection.getString("Y")
                if (x == null || x.length == 0 || y == null || y.length == 0) continue
                for (slot in Position.getPositionList(x, y)) {
                    inv.setItem(slot, item)
                    slotItemMap[slot] = keyWord
                }
            } else {
                when (keyWord) {
                    KeyWord.PROCESS_DONE -> acceptItem = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, statusItemSection), vote)
                    KeyWord.PROCESS_NOT -> refuseItem = VoteUpPlaceholder.applyPlaceholder(ItemUtil.build(plugin, statusItemSection), vote)
                }
            }
        }
        if (status == UserStatus.DONE) {
            val acceptAmount = Math.min(Math.floor(9 * (vote.process / 100.0)).toInt(), 9)
            val acceptX = "1-$acceptAmount"
            for (slot in Position.getPositionList(acceptX, "3")) inv.setItem(slot!!, acceptItem)
            if (acceptAmount < 9) {
                val refuseX = (acceptAmount + 1).toString() + "-9"
                for (slot in Position.getPositionList(refuseX, "3")) inv.setItem(slot!!, refuseItem)
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
        val anonymous = vote.allowAnonymous && event.click == ClickType.DROP
        when (keyWord) {
            KeyWord.VOTE_ACCEPT -> {
                if (event.isRightClick || anonymous && event.isShiftClick) ConversationUtil.start(plugin, user, CollectReasonPrompt(user, vote, Vote.Choice.ACCEPT, anonymous), 300) else voteWithoutReason(user, Vote.Choice.ACCEPT, anonymous)
                refresh(inv)
            }
            KeyWord.VOTE_NEUTRAL -> {
                if (event.isRightClick || anonymous && event.isShiftClick) ConversationUtil.start(plugin, user, CollectReasonPrompt(user, vote, Vote.Choice.NEUTRAL, anonymous), 300) else voteWithoutReason(user, Vote.Choice.NEUTRAL, anonymous)
                refresh(inv)
            }
            KeyWord.VOTE_REFUSE -> {
                if (event.isRightClick || anonymous && event.isShiftClick) ConversationUtil.start(plugin, user, CollectReasonPrompt(user, vote, Vote.Choice.REFUSE, anonymous), 300) else voteWithoutReason(user, Vote.Choice.REFUSE, anonymous)
                refresh(inv)
            }
            KeyWord.VOTE_REASON -> {
                val participant = vote.getParticipant(user.uniqueId) ?: break
                ConversationUtil.start(plugin, user, CollectReasonPrompt(user, vote, participant.choice, participant.anonymous), 300)
            }
            KeyWord.BACK -> if (lastGui != null) {
                when (lastGui) {
                    GuiKey.MAIN_MENU -> {
                    }
                    GuiKey.VOTE_LIST -> {
                    }
                    GuiKey.VOTE_DETAILS, GuiKey.VOTE_CREATE, GuiKey.VOTE_PARTICIPANTS -> {
                    }
                }
            } else BasicUtil.closeInventory(plugin, user)
            KeyWord.PARTICIPANT -> BasicUtil.openInventory(plugin, user, ParticipantInventoryHolder(vote, user, GUI_KEY).getInventory())
            KeyWord.CANCEL -> {
            }
            KeyWord.EDIT -> {
            }
            KeyWord.OWNER, KeyWord.DESCRIPTION, KeyWord.AUTOCAST, KeyWord.PROCESS_DONE, KeyWord.PROCESS_NOT -> {
            }
            else -> {
            }
        }
    }

    private fun voteWithoutReason(@NonNull user: Player, choice: Vote.Choice, anonymous: Boolean) {
        val vote = data as Vote
        VoteUpAPI.VOTE_MANAGER!!.vote(vote.voteID, user, choice, anonymous, Msg.REASON_NOT_YET.msg)
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    enum class KeyWord {
        OWNER, DESCRIPTION, AUTOCAST, VOTE_ACCEPT, VOTE_NEUTRAL, VOTE_REFUSE, VOTE_REASON, PARTICIPANT, EDIT, CANCEL, BACK, PROCESS_DONE, PROCESS_NOT
    }

    companion object {
        val GUI_KEY = GuiKey.VOTE_DETAILS
    }

    init {
        plugin = PPlugin.getInstance()
        this.data = data
        viewer = player
        this.lastGui = lastGui
        inventory = construct()
    }
}