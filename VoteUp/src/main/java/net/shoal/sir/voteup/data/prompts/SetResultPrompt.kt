package net.shoal.sir.voteup.data.prompts

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.StringPrompt
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.I18n

class SetResultPrompt(@NonNull player: Player, @NonNull vote: Vote, type: Vote.Result) : StringPrompt() {
    private val plugin: PPlugin
    private val user: Player
    private val vote: Vote
    private val type: Vote.Result
    override fun getPromptText(context: ConversationContext): String {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT.msg, TARGET.name + "(" + type.name + ")"))
    }

    override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg))
            return Prompt.END_OF_CONVERSATION
        }
        if ("exit".equals(input, ignoreCase = true)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT_CANCELLED.msg, TARGET.name + "(" + type.name + ")")))
            VoteUpAPI.VOTE_MANAGER!!.back(user)
            return Prompt.END_OF_CONVERSATION
        } else if ("next".equals(input, ignoreCase = true)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT_PASSED.msg, TARGET.name + "(" + type.name + ")")))
            return next(type)
        }
        vote.results!![type] = I18n.color(input)
        BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(Msg.VOTE_EDIT_SUCCESS.msg), TARGET.name)))
        VoteUpAPI.SOUND!!.success(user)
        return next(type)
    }

    private fun next(type: Vote.Result): Prompt {
        return when (type) {
            Vote.Result.PASS -> SetResultPrompt(user, vote, Vote.Result.DRAW)
            Vote.Result.DRAW -> SetResultPrompt(user, vote, Vote.Result.REJECT)
            Vote.Result.REJECT -> SetResultPrompt(user, vote, Vote.Result.CANCEL)
            Vote.Result.CANCEL -> {
                VoteUpAPI.VOTE_MANAGER!!.back(user)
                Prompt.END_OF_CONVERSATION
            }
            else -> {
                VoteUpAPI.VOTE_MANAGER!!.back(user)
                Prompt.END_OF_CONVERSATION
            }
        }
    }

    companion object {
        val TARGET = Vote.Data.RESULT
    }

    init {
        plugin = PPlugin.getInstance()
        user = player
        this.vote = vote
        this.type = type
    }
}