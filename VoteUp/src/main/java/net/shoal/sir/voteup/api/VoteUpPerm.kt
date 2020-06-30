package net.shoal.sir.voteup.api

import net.shoal.sir.voteup.data.Vote
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.I18n

enum class VoteUpPerm(val node: String) {
    VOTE("VoteUp.vote."), REASON("VoteUp.vote.reason"), CREATE("VoteUp.create"), VIEW("VoteUp.view"), ANONYMOUS("VoteUp.view.anonymous"), EDIT("VoteUp.edit."), NOTICE("VoteUp.notice"), ADMIN("VoteUp.admin"), ALL("VoteUp.*");

    fun hasPermission(@NonNull user: Player, vararg params: Any): Boolean {
        val plugin = PPlugin.getInstance()
        var result: Boolean
        when (this) {
            VOTE -> {
                if (params.size == 0) {
                    result = false
                    break
                }
                result = user.hasPermission(node + (params[0] as Vote.Choice).name.toLowerCase()) || adminPerm(user)
            }
            EDIT -> {
                if (params.size == 0) {
                    result = false
                    break
                }
                result = user.hasPermission(node + (params[0] as Vote.Data).name.toLowerCase()) || adminPerm(user)
            }
            else -> result = user.hasPermission(node) || adminPerm(user)
        }
        if (!result) user.sendMessage(plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "NoPerm"])
        return result
    }

    private fun adminPerm(@NonNull user: Player): Boolean {
        return node.endsWith(".") && user.hasPermission("$node*") || user.hasPermission(node) || user.hasPermission(node)
    }

}