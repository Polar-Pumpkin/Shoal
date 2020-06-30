package net.shoal.sir.voteup.command.subcommands

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.data.inventory.DetailsInventoryHolder
import net.shoal.sir.voteup.enums.Msg
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.command.PCommand
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.I18n

class ViewCmd : PCommand {
    override fun getPermission(): String {
        return null
    }

    override fun getDescription(): String {
        return "查看投票的详细信息"
    }

    override fun execute(plugin: PPlugin, sender: CommandSender, args: Array<String>): Boolean {
        // /vote view voteID
        if (sender is Player) {
            val user = sender
            if (VoteUpPerm.VIEW.hasPermission(user)) {
                if (args.size == 2) {
                    val vote = VoteUpAPI.VOTE_MANAGER!!.getVote(args[1])
                    if (vote != null) BasicUtil.openInventory(plugin, user, DetailsInventoryHolder(vote, user, null).inventory) else I18n.send(user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_GET_VOTE.msg))
                } else I18n.send(user, plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"])
            }
        } else sender.sendMessage(plugin.lang.build(plugin.localeKey, I18n.Type.ERROR, Msg.ERROR_COMMAND_NOT_PLAYER.msg))
        return true
    }
}