package net.shoal.sir.voteup.data.prompts

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.api.VoteUpPlaceholder
import net.shoal.sir.voteup.config.ConfPath
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.I18n
import org.serverct.parrot.parrotx.utils.JsonChatUtil

class ModifyContentPrompt(@NonNull player: Player, @NonNull vote: Vote, index: Int, targetDesc: Boolean, action: String) : StringPrompt() {
    private val plugin: PPlugin
    private val user: Player
    private val vote: Vote
    private val index: Int
    private val targetDesc: Boolean
    private val action: String
    override fun getPromptText(context: ConversationContext): String {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT.msg, TARGET.name))
    }

    override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg))
            return Prompt.END_OF_CONVERSATION
        }
        if ("exit".equals(input, ignoreCase = true)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT_CANCELLED.msg, TARGET.name)))
            VoteUpAPI.VOTE_MANAGER!!.back(user)
            return Prompt.END_OF_CONVERSATION
        }
        if (!targetDesc) {
            if (plugin.pConfig.config.getBoolean(ConfPath.Path.AUTOCAST_ENABLE.path, true)) {
                val inputArgs = input!!.split(" ").toTypedArray()
                val commandList = plugin.pConfig.config.getStringList(ConfPath.Path.AUTOCAST_LIST.path)
                val usermode = plugin.pConfig.config.getBoolean(ConfPath.Path.AUTOCAST_USERMODE.path, true)
                if (!usermode) {
                    val blackMode = plugin.pConfig.config.getBoolean(ConfPath.Path.AUTOCAST_BLACKLIST.path, false)
                    if (blackMode && commandList.contains(inputArgs[0]) || !blackMode && !commandList.contains(inputArgs[0])) {
                        I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_AUTOCAST_IGNORE.msg))
                        return Prompt.END_OF_CONVERSATION
                    }
                }
            } else I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_AUTOCAST_DISABLE.msg))
        }
        val list = if (targetDesc) vote.description else vote.autocast
        when (action) {
            "add" -> if (index > list!!.size) list.add(input) else list.add(index, input)
            "set" -> if (index > list!!.size) list[list.size - 1] = input else list[index] = input
            else -> return Prompt.END_OF_CONVERSATION
        }
        if (targetDesc) vote.description = list else vote.autocast = list
        BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(Msg.VOTE_EDIT_SUCCESS.msg), TARGET.name)))
        VoteUpAPI.SOUND!!.success(user)
        JsonChatUtil.sendEditableList(
                user,
                list,
                VoteUpPlaceholder.parse(vote, String.format((if (targetDesc) Msg.VOTE_VALUE_DESCRIPTION else Msg.VOTE_VALUE_AUTOCAST).msg, list.size)),
                "&a&l[插入] ",
                "/vote modify " + (if (targetDesc) "desc" else "autocast") + " add ",
                "&e&l[编辑] ",
                "/vote modify " + (if (targetDesc) "desc" else "autocast") + " set ",
                "&c&l[删除] ",
                "/vote modify " + (if (targetDesc) "desc" else "autocast") + " del ",
                "&7[&a&l>>> &7返回菜单]",
                "/vote create back"
        )
        return Prompt.END_OF_CONVERSATION
    }

    companion object {
        var TARGET: Vote.Data
    }

    init {
        plugin = PPlugin.getInstance()
        user = player
        this.vote = vote
        this.index = index
        this.targetDesc = targetDesc
        TARGET = if (targetDesc) Vote.Data.DESCRIPTION else Vote.Data.AUTOCAST
        this.action = action
    }
}