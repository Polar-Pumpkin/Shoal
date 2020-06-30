package net.shoal.sir.voteup.data.inventory

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.api.VoteUpPlaceholder
import net.shoal.sir.voteup.config.GuiManager.GuiKey
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.meta.SkullMeta
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.data.InventoryExecutor
import org.serverct.parrot.parrotx.enums.Position
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.EnumUtil
import org.serverct.parrot.parrotx.utils.ItemUtil
import org.serverct.parrot.parrotx.utils.TimeUtil
import java.util.*

class ParticipantInventoryHolder<T>(data: T, @NonNull player: Player, lastGui: GuiKey?) : InventoryExecutor {
    private val plugin: PPlugin
    private val slotItemMap: MutableMap<Int, KeyWord> = HashMap()
    private val participantMap: MutableMap<Int, UUID?> = HashMap()
    private val anonymousItem: Map<String, Material> = object : HashMap<String?, Material?>() {
        init {
            put("小怕", Material.CREEPER_HEAD)
            put("苦力怕", Material.CREEPER_HEAD)
            put("社会你怕哥", Material.CREEPER_HEAD)
            put("怕怕", Material.CREEPER_HEAD)
            put("爬", Material.CREEPER_HEAD)
            put("小僵", Material.ZOMBIE_HEAD)
            put("僵尸", Material.ZOMBIE_HEAD)
            put("丧尸", Material.ZOMBIE_HEAD)
            put("僵僵", Material.ZOMBIE_HEAD)
            put("尸", Material.ZOMBIE_HEAD)
            put("小白", Material.SKELETON_SKULL)
            put("骷髅", Material.SKELETON_SKULL)
            put("Sans", Material.SKELETON_SKULL)
            put("白白", Material.SKELETON_SKULL)
            put("嘎吱嘎吱", Material.SKELETON_SKULL)
            put("小黑", Material.WITHER_SKELETON_SKULL)
            put("凋零骷髅", Material.WITHER_SKELETON_SKULL)
            put("破碎の心 :(", Material.WITHER_SKELETON_SKULL)
            put("黑黑", Material.WITHER_SKELETON_SKULL)
            put("神必剑客", Material.WITHER_SKELETON_SKULL)
            put("草", Material.GRASS)
            put("花", Material.SUNFLOWER)
        }
    }
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
            val item = ItemUtil.build(plugin, targetItemSection)
            if (keyWord == KeyWord.BACK) ItemUtil.replace(item, "%BACK%", if (lastGui != null) lastGui!!.guiname else "无")
            val participantList = vote.participants
            val iterator: Iterator<Vote.Participant?> = participantList!!.iterator()
            val targetSlotSection = targetItemSection.getConfigurationSection("Position") ?: continue
            val x = targetSlotSection.getString("X")
            val y = targetSlotSection.getString("Y")
            if (x == null || x.length == 0 || y == null || y.length == 0) continue
            for (slot in Position.getPositionList(x, y)) {
                if (keyWord == KeyWord.PARTICIPANT) {
                    if (!iterator.hasNext()) break
                    val resultItem = item.clone()
                    val user = iterator.next()
                    val variableMap: MutableMap<String, String> = object : HashMap<String?, String?>() {
                        init {
                            put("participant", Bukkit.getOfflinePlayer(user!!.uuid).name)
                            put("time", TimeUtil.getDescriptionTimeFromTimestamp(user.timestamp) + " &8&o" + TimeUtil.getChineseDateFormat(Date(user.timestamp)))
                            put("choice", user.choice!!.name + " &8&o" + vote.choices!![user.choice])
                            put("reason", user.reason)
                        }
                    }
                    if (user!!.anonymous) {
                        val names = anonymousItem.keys.toTypedArray()
                        val name = names[Random().nextInt(names.size)]
                        variableMap["participant"] = if (VoteUpPerm.ANONYMOUS.hasPermission(viewer)) name + " &8&o(" + variableMap["participant"] + ")" else name
                        resultItem.type = anonymousItem[name]!!
                    } else if (resultItem.type == Material.PLAYER_HEAD) {
                        val skull = resultItem.itemMeta as SkullMeta?
                        if (skull != null) {
                            skull.owningPlayer = Bukkit.getOfflinePlayer(user.uuid)
                            resultItem.itemMeta = skull
                        }
                    }
                    variableMap.forEach { (k: String, v: String?) -> ItemUtil.replace(resultItem, "%$k%", v) }
                    inv.setItem(slot, resultItem)
                    participantMap[slot] = user.uuid
                } else inv.setItem(slot, item)
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
            KeyWord.BACK -> if (lastGui != null) {
                when (lastGui) {
                    GuiKey.VOTE_DETAILS -> BasicUtil.openInventory(plugin, user, DetailsInventoryHolder(vote, user, GUI_KEY).getInventory())
                    GuiKey.MAIN_MENU, GuiKey.VOTE_CREATE, GuiKey.VOTE_LIST, GuiKey.VOTE_PARTICIPANTS -> {
                    }
                    else -> {
                    }
                }
            } else BasicUtil.closeInventory(plugin, user)
            KeyWord.PARTICIPANT -> {
            }
            else -> {
            }
        }
    }

    override fun getInventory(): Inventory {
        return inventory
    }

    enum class KeyWord {
        BACK, PARTICIPANT
    }

    companion object {
        val GUI_KEY = GuiKey.VOTE_PARTICIPANTS
    }

    init {
        plugin = PPlugin.getInstance()
        this.data = data
        viewer = player
        this.lastGui = lastGui
        inventory = construct()
    }
}