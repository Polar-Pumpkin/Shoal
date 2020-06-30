package net.shoal.sir.voteup.data

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.config.ConfPath
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.ChatColor
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.data.PData
import org.serverct.parrot.parrotx.data.PID
import org.serverct.parrot.parrotx.data.flags.Owned
import org.serverct.parrot.parrotx.data.flags.Timestamp
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.EnumUtil
import org.serverct.parrot.parrotx.utils.I18n
import java.io.File
import java.io.IOException
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.UnaryOperator

class Vote : PData, Owned, Timestamp {
    private val plugin: PPlugin
    var voteID: String? = null
    var open = false
    var cancelled = false
    var isDraft = false
    var allowAnonymous // TODO 匿名投票设置 = false
    var isPublic // TODO 结果公开 = false
    var allowEdit // TODO 允许投票后编辑 = false
    var type: Type? = null
    var goal = 0
    var owner: UUID? = null
    var startTime: Long = 0
    var duration: String? = null
    var title: String? = null
    var description: MutableList<String?>? = null
    var choices: MutableMap<Choice?, String?>? = null
    var autocast: List<String?>? = null
    var results: MutableMap<Result?, String?>? = null
    var participants: MutableList<Participant?>? = null
    private var id: PID
    private var file: File

    constructor(@NonNull file: File) {
        plugin = PPlugin.getInstance()
        this.file = file
        load(file)
        id = PID(plugin, "VOTE_$voteID")
    }

    constructor(goal: Int, owner: UUID?, duration: String?) {
        voteID = UUID.randomUUID().toString()
        plugin = PPlugin.getInstance()
        id = PID(plugin, "VOTE_$voteID")
        file = File(VoteUpAPI.VOTE_MANAGER!!.folder, "$voteID.yml")
        open = false
        cancelled = false
        isDraft = true
        allowAnonymous = false
        isPublic = false
        allowEdit = false
        type = Type.NORMAL
        this.goal = goal
        this.owner = owner
        startTime = System.currentTimeMillis()
        this.duration = duration
        init()
    }

    fun getParticipant(uuid: UUID): Participant? {
        for (participant in participants!!) if (participant!!.uuid === uuid) return participant
        return null
    }

    fun listParticipants(filter: Predicate<Participant?>): List<Participant?> {
        val result: MutableList<Participant?> = ArrayList()
        participants!!.forEach(
                Consumer { participant: Participant? -> if (filter.test(participant)) result.add(participant) }
        )
        return result
    }

    val isPassed: Boolean
        get() {
            val all = participants!!.size
            val accept = listParticipants(Predicate { user: Participant? -> user!!.choice == Choice.ACCEPT }).size
            val refuse = listParticipants(Predicate { user: Participant? -> user!!.choice == Choice.REFUSE }).size
            return if (all < plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_PARTICIPANT_LEAST.path, 5)) false else when (type) {
                Type.NORMAL -> accept > refuse
                Type.REACHAMOUNT -> accept >= goal
                Type.LEASTNOT -> refuse <= goal
                else -> false
            }
        }

    fun result(): Result {
        if (cancelled) return Result.CANCEL
        if (type == Type.NORMAL) if (listParticipants(Predicate { user: Participant? -> user!!.choice == Choice.ACCEPT }).size == listParticipants(Predicate { user: Participant? -> user!!.choice == Choice.REFUSE }).size) return Result.DRAW
        return if (isPassed) Result.PASS else Result.REJECT
    }

    fun isVoted(uuid: UUID): Boolean {
        return getParticipant(uuid) != null
    }

    fun hasReason(uuid: UUID): Boolean {
        val user = getParticipant(uuid)
        return if (user != null) !user.reason.equals(Msg.REASON_NOT_YET.msg, ignoreCase = true) && !user.reason.equals(Msg.REASON_NO_PERM.msg, ignoreCase = true) else false
    }

    fun getReason(uuid: UUID): String {
        val user = getParticipant(uuid)
        return if (user != null) I18n.color(user.reason) else I18n.color(Msg.REASON_NOT_YET.msg)
    }

    fun getChoice(uuid: UUID): Choice? {
        val user = getParticipant(uuid)
        return user?.choice
    }

    val process: Int
        get() {
            val all = participants!!.size
            val accept = listParticipants(Predicate { user: Participant? -> user!!.choice == Choice.ACCEPT }).size
            val refuse = listParticipants(Predicate { user: Participant? -> user!!.choice == Choice.REFUSE }).size
            val least = plugin.pConfig.config.getInt(ConfPath.Path.SETTINGS_PARTICIPANT_LEAST.path, 5)
            val rate: Int
            rate = if (all >= least) {
                when (type) {
                    Type.NORMAL -> (accept / all.toDouble() * 100).toInt()
                    Type.REACHAMOUNT -> (accept / goal.toDouble() * 100).toInt()
                    Type.LEASTNOT -> (Math.max(goal - refuse, 0) / goal.toDouble() * 100).toInt()
                    else -> 0
                }
            } else (all / least.toDouble() * 100).toInt()
            return rate
        }

    fun getUserStatus(uuid: UUID): UserStatus {
        if (!open) return UserStatus.DONE
        val isVoted = isVoted(uuid)
        val hasReason = hasReason(uuid)
        return if (isVoted && hasReason) UserStatus.DONE else if (isVoted) UserStatus.NO_REASON else UserStatus.FIRST
    }

    override fun getTypeName(): String {
        return "投票数据文件/$fileName"
    }

    override fun getFileName(): String {
        return BasicUtil.getNoExFileName(file.name)
    }

    override fun init() {
        title = "$ownerName 的投票"
        description = ArrayList(listOf(Msg.NO_DESCRIPTION.msg))
        choices = object : HashMap<Choice?, String?>() {
            init {
                put(Choice.ACCEPT, ChatColor.GREEN.toString() + Choice.ACCEPT.name)
                put(Choice.NEUTRAL, ChatColor.YELLOW.toString() + Choice.NEUTRAL.name)
                put(Choice.REFUSE, ChatColor.RED.toString() + Choice.REFUSE.name)
            }
        }
        autocast = ArrayList()
        results = object : HashMap<Result?, String?>() {
            init {
                put(Result.PASS, ChatColor.GREEN.toString() + Result.PASS.name)
                put(Result.DRAW, ChatColor.YELLOW.toString() + Result.DRAW.name)
                put(Result.REJECT, ChatColor.RED.toString() + Result.REJECT.name)
                put(Result.CANCEL, ChatColor.RED.toString() + Result.CANCEL.name)
            }
        }
        participants = ArrayList()
    }

    override fun saveDefault() {
        init()
        save()
    }

    override fun getFile(): File {
        return file
    }

    override fun setFile(@NonNull file: File) {
        this.file = file
    }

    override fun load(@NonNull file: File) {
        try {
            val data: FileConfiguration = YamlConfiguration.loadConfiguration(file)
            voteID = fileName
            val information = data.getConfigurationSection("Information") ?: return
            open = information.getBoolean("Open", true)
            cancelled = information.getBoolean("Cancelled", false)
            isDraft = information.getBoolean("Draft", false)
            type = Type.valueOf(information.getString("Type", "NORMAL")!!.toUpperCase())
            goal = information.getInt("Goal", Int.MAX_VALUE)
            owner = UUID.fromString(information.getString("Owner"))
            startTime = information.getLong("Timestamp")
            duration = information.getString("Duration")
            val setting = data.getConfigurationSection("Settings")
            if (setting != null) {
                title = I18n.color(setting.getString("Title", "$ownerName 的投票"))
                description = setting.getStringList("Description")
                description!!.replaceAll { text: String? -> I18n.color(text) }
            }
            val choiceSection = setting!!.getConfigurationSection("Choices")
            choices = HashMap()
            if (choiceSection != null) {
                for (choiceKey in choiceSection.getKeys(false)) {
                    val choice = EnumUtil.valueOf(Choice::class.java, choiceKey.toUpperCase())
                    choices[choice] = I18n.color(choiceSection.getString(choiceKey))
                }
            }
            autocast = setting.getStringList("Autocast")
            val resultSection = setting.getConfigurationSection("Results") // TODO 这里有一个全都是 null 的问题
            results = HashMap()
            if (resultSection != null) {
                for (resultKey in resultSection.getKeys(false)) {
                    val result = EnumUtil.valueOf(Result::class.java, resultKey.toUpperCase())
                    results[result] = I18n.color(choiceSection!!.getString(resultKey))
                }
            }
            val participantSection = data.getConfigurationSection("Participants")
            participants = ArrayList()
            if (participantSection != null) {
                for (uuid in participantSection.getKeys(false)) {

                    // 旧版本数据文件的转换
                    val choice = EnumUtil.valueOf(Choice::class.java, uuid.toUpperCase())
                    if (choice != null) {
                        val targetOldSection = participantSection.getConfigurationSection(uuid)
                        for (oldUUID in targetOldSection!!.getKeys(false)) participants.add(Participant(UUID.fromString(oldUUID), choice, false, targetOldSection.getString(oldUUID!!)))
                        continue
                    }
                    val userSection = participantSection.getConfigurationSection(uuid) ?: continue
                    participants.add(Participant(userSection))
                }
            }
        } catch (e: Throwable) {
            plugin.lang.logError(I18n.LOAD, typeName, e, null)
        }
    }

    override fun reload() {
        plugin.lang.logAction(I18n.RELOAD, typeName)
        load(file)
    }

    override fun save() {
        val data: FileConfiguration = YamlConfiguration.loadConfiguration(file)
        val information = data.createSection("Information")
        information["Open"] = open
        information["Cancelled"] = cancelled
        information["Draft"] = isDraft
        information["Type"] = type!!.name
        information["Goal"] = goal
        information["Owner"] = owner.toString()
        information["Timestamp"] = startTime
        information["Duration"] = duration
        val setting = data.createSection("Settings")
        setting["Title"] = I18n.deColor(title, '&')
        val desc2save: List<String?> = ArrayList(description)
        desc2save.replaceAll(UnaryOperator { s: String? -> I18n.deColor(s, '&') })
        setting["Description"] = desc2save
        choices!!.forEach { (choice: Choice?, s: String?) -> setting["Choices." + choice!!.name] = I18n.deColor(s, '&') }
        setting["Autocast"] = autocast
        results!!.forEach { (result: Result?, s: String?) -> setting["Results." + result!!.name] = I18n.deColor(s, '&') }
        val participantSection = data.createSection("Participants")
        participants!!.forEach(Consumer { participant: Participant? -> participant!!.save(participantSection) })
        try {
            data.save(file)
        } catch (e: IOException) {
            plugin.lang.logError(I18n.SAVE, typeName, e, null)
        }
    }

    override fun getID(): PID {
        return id
    }

    override fun setID(@NonNull pid: PID) {
        id = pid
    }

    override fun getOwner(): UUID {
        return owner!!
    }

    override fun setOwner(uuid: UUID) {
        owner = uuid
    }

    override fun getTimestamp(): Long {
        return startTime
    }

    override fun setTime(time: Long) {
        startTime = time
    }

    enum class Type(val mode: Int, override val name: String, val desc: String) {
        NORMAL(0, "普通投票", "同意人数大于反对人数"), REACHAMOUNT(1, "多数同意投票", "同意人数需达到指定数量"), LEASTNOT(2, "否决投票", "反对人数不超过指定数量");

        companion object {
            fun mode(mode: Int): Type {
                var mode = mode
                if (mode > 2) mode = 0
                if (mode < 0) mode = 2
                return when (mode) {
                    0 -> NORMAL
                    1 -> REACHAMOUNT
                    2 -> LEASTNOT
                    else -> NORMAL
                }
            }
        }

    }

    enum class Choice(override val name: String) {
        ACCEPT("同意"), NEUTRAL("中立"), REFUSE("反对");

    }

    enum class Result(override val name: String) {
        PASS("通过"), REJECT("未通过"), DRAW("平票"), CANCEL("被取消");

    }

    enum class Duration(val code: Char, override val name: String, val time: Int) {
        // yyyy-MM-dd HH:mm:ss
        DAY('d', "天", 86400), HOUR('H', "时", 3600), MINUTE('m', "分", 60), SECOND('s', "秒", 1);

    }

    enum class UserStatus {
        FIRST, NO_REASON, DONE
    }

    enum class Data(override val name: String) {
        ID("投票ID"), OPEN("进行状态"), CANCELLED("取消状态"), DRAFT("草稿状态"), ANONYMOUS("允许匿名投票"), PUBLIC("公开投票进度"), EDITABLE("可编辑所投票"), TYPE("投票类型"), GOAL("目标人数"), OWNER("发起者"), STARTTIME("发起时间"), DURATION("持续时间"), TITLE("投票标题"), DESCRIPTION("投票简述"), CHOICE("投票选项"), AUTOCAST("自动执行内容"), RESULT("投票结果"), PARTICIPANT("参加者"), PROCESS("投票进度");

    }

    class Participant : Timestamp {
        val uuid: UUID
        val choice: Choice?
        val anonymous: Boolean
        val reason: String?
        var timestamp: Long

        constructor(uuid: UUID, choice: Choice?, anonymous: Boolean, reason: String?) {
            this.uuid = uuid
            this.choice = choice
            this.anonymous = anonymous
            this.reason = reason
            timestamp = System.currentTimeMillis()
        }

        constructor(@NonNull section: ConfigurationSection) {
            uuid = UUID.fromString(section.name)
            choice = EnumUtil.valueOf(Choice::class.java, section.getString("Choice"))
            anonymous = section.getBoolean("Anonymous", false)
            reason = section.getString("Reason", Msg.REASON_NOT_YET.msg)
            timestamp = section.getLong("Timestamp")
        }

        fun save(participantSection: ConfigurationSection) {
            val userSection = participantSection.createSection(uuid.toString())
            userSection["Choice"] = choice!!.name
            userSection["Anonymous"] = anonymous
            userSection["Reason"] = reason
            userSection["Timestamp"] = timestamp
        }

        override fun getTimestamp(): Long {
            return timestamp
        }

        override fun setTime(l: Long) {
            timestamp = l
        }
    }

    companion object {
        fun getDurationTimestamp(duration: String?): Long {
            var result: Long = 0
            var clone = duration!!.toUpperCase()
            var durationType: Duration
            while (getFirstIndexOf(clone).also { durationType = it!! } != null) {
                val index = clone.indexOf(durationType.code)
                try {
                    val target = clone.substring(0, index)
                    val amount = target.toInt()
                    result += amount * durationType.time.toLong()
                    clone = clone.substring(index + 1)
                } catch (e: Throwable) {
                    break
                }
            }
            return result * 1000
        }

        fun getFirstIndexOf(target: String): Duration? {
            var index = Int.MAX_VALUE
            var durationType: Duration? = null
            for (type in Duration.values()) {
                val currentIndex = target.indexOf(type.code)
                if (currentIndex == -1) continue
                if (currentIndex < index) {
                    index = currentIndex
                    durationType = type
                }
            }
            return durationType
        }
    }
}