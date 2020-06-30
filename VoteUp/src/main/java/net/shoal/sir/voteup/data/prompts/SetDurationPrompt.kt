package net.shoal.sir.voteup.data.prompts

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.conversations.ConversationContext
import org.bukkit.conversations.Prompt
import org.bukkit.conversations.ValidatingPrompt
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.I18n
import java.util.regex.Pattern

class SetDurationPrompt(@NonNull player: Player, @NonNull vote: Vote) : ValidatingPrompt() {
    private val plugin: PPlugin
    private val user: Player
    private val vote: Vote
    override fun isInputValid(context: ConversationContext, input: String): Boolean {
        if (input.contains("d") || input.contains("H") || input.contains("m")) {
            val r = Pattern.compile("^[0-9dhmDHM]*$")
            if (r.matcher(input).matches()) return true
        }
        I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_DURATION.msg))
        VoteUpAPI.SOUND!!.fail(user)
        return false
    }

    override fun getPromptText(context: ConversationContext): String {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT.msg, TARGET.name + "(符号: &dd &7天, &dH &7小时, &dm &7分)"))
    }

    override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt? {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg))
            return Prompt.END_OF_CONVERSATION
        }
        if ("exit".equals(input, ignoreCase = true)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT_CANCELLED.msg, TARGET.name)))
            VoteUpAPI.VOTE_MANAGER!!.back(user)
            return Prompt.END_OF_CONVERSATION
        }
        vote.duration = input
        BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(Msg.VOTE_EDIT_SUCCESS.msg), TARGET.name)))
        VoteUpAPI.SOUND!!.success(user)
        VoteUpAPI.VOTE_MANAGER!!.back(user)
        return Prompt.END_OF_CONVERSATION
    }

    companion object {
        val TARGET = Vote.Data.DURATION
    }

    init {
        plugin = PPlugin.getInstance()
        user = player
        this.vote = vote
    }
}