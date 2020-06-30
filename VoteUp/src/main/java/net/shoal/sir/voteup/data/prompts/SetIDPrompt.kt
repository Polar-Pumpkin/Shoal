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

class SetIDPrompt(@NonNull player: Player, @NonNull vote: Vote) : StringPrompt() {
    private val plugin: PPlugin
    private val user: Player
    private val vote: Vote
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
        vote.voteID = I18n.deColor(input, '&')
        // VoteUpAPI.VOTE_MANAGER.setVoteData(vote.voteID, user, TARGET, I18n.color(input));
        VoteUpAPI.VOTE_MANAGER!!.back(user)
        return Prompt.END_OF_CONVERSATION
    }

    companion object {
        val TARGET = Vote.Data.ID
    }

    init {
        plugin = PPlugin.getInstance()
        user = player
        this.vote = vote
    }
}