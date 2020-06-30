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
import org.serverct.parrot.parrotx.utils.I18n

class CollectReasonPrompt(@NonNull player: Player, @NonNull vote: Vote, type: Vote.Choice?, anonymous: Boolean) : StringPrompt() {
    private val plugin: PPlugin
    private val user: Player
    private val vote: Vote
    private val type: Vote.Choice?
    private val anonymous: Boolean
    override fun getPromptText(context: ConversationContext): String {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, Msg.VOTE_REASON.msg)
    }

    override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg))
            return Prompt.END_OF_CONVERSATION
        }
        if ("exit".equals(input, ignoreCase = true)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT_CANCELLED.msg, TARGET.name + "(" + type!!.name + ")")))
            VoteUpAPI.VOTE_MANAGER!!.back(user)
            return Prompt.END_OF_CONVERSATION
        }
        VoteUpAPI.VOTE_MANAGER!!.vote(vote.voteID, user, type, anonymous, input)
        VoteUpAPI.SOUND!!.ding(user)
        return Prompt.END_OF_CONVERSATION
    }

    companion object {
        val TARGET = Vote.Data.PARTICIPANT
    }

    init {
        plugin = PPlugin.getInstance()
        user = player
        this.vote = vote
        this.type = type
        this.anonymous = anonymous
    }
}