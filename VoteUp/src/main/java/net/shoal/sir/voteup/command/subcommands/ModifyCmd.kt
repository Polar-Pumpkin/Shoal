package net.shoal.sir.voteup.command.subcommands

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.data.prompts.ModifyContentPrompt
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.command.PCommand
import org.serverct.parrot.parrotx.utils.ConversationUtil
import org.serverct.parrot.parrotx.utils.I18n

class ModifyCmd : PCommand {
    override fun getPermission(): String {
        return null
    }

    override fun getDescription(): String {
        return "修改投票的复合内容, 例如投票简述或自动执行"
    }

    override fun execute(plugin: PPlugin, sender: CommandSender, args: Array<String>): Boolean {
        // /vote modify desc/autocast add/set/del line
        if (sender is Player) {
            val user = sender
            val creating = VoteUpAPI.VOTE_MANAGER!!.draftVote(user.uniqueId)
            if (creating != null) {
                if (args.size == 3 || args.size == 4) {
                    when (args[1]) {
                        "desc" -> modify(user, creating, args, true)
                        "autocast" -> modify(user, creating, args, false)
                        else -> user.sendMessage(plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"])
                    }
                } else user.sendMessage(plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"])
            }
        }
        return true
    }

    private fun modify(user: Player, creating: Vote, args: Array<String>, isDesc: Boolean) {
        val dataType = if (isDesc) Vote.Data.DESCRIPTION else Vote.Data.AUTOCAST
        if (VoteUpPerm.EDIT.hasPermission(user, dataType)) {
            val plugin = PPlugin.getInstance()
            val list = if (isDesc) creating.description else creating.autocast
            if (ADD.equals(args[2], ignoreCase = true)) ConversationUtil.start(plugin, user, ModifyContentPrompt(user, creating, if (args.size == 4) args[3].toInt() else list!!.size + 1, isDesc, ADD), 300) else if (SET.equals(args[2], ignoreCase = true)) ConversationUtil.start(plugin, user, ModifyContentPrompt(user, creating, args[3].toInt(), isDesc, SET), 300) else if (DEL.equals(args[2], ignoreCase = true)) {
                list!!.removeAt(if (args.size == 4) args[3].toInt() else list.size - 1)
                if (isDesc) creating.description = list else creating.autocast = list
            } else user.sendMessage(plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"])
        }
    }

    companion object {
        const val ADD = "add"
        const val SET = "set"
        const val DEL = "del"
    }
}