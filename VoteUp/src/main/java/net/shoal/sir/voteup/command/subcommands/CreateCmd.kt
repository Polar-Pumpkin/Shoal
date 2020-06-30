package net.shoal.sir.voteup.command.subcommands

import net.shoal.sir.voteup.api.VoteUpAPI
import net.shoal.sir.voteup.api.VoteUpPerm
import net.shoal.sir.voteup.data.Vote
import net.shoal.sir.voteup.data.inventory.CreateInventoryHolder
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.command.PCommand
import org.serverct.parrot.parrotx.utils.BasicUtil
import org.serverct.parrot.parrotx.utils.I18n
import java.util.*

class CreateCmd : PCommand {
    override fun getPermission(): String {
        return VoteUpPerm.CREATE.node
    }

    override fun getDescription(): String {
        return "创建新投票或返回投票草稿"
    }

    override fun execute(plugin: PPlugin, sender: CommandSender, args: Array<String>): Boolean {
        if (sender is Player) {
            val user = sender
            val data: Vote?
            when (args.size) {
                1 -> data = VoteUpAPI.VOTE_MANAGER!!.create(user.uniqueId)
                2 -> if ("back" == args[1]) data = VoteUpAPI.VOTE_MANAGER!!.draftVote(user.uniqueId) else {
                    data = null
                    user.sendMessage(plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"])
                }
                3 -> if ("player" == args[1]) {
                    val uuid: UUID
                    val target = Bukkit.getPlayerExact(args[2])
                    uuid = target?.uniqueId ?: Bukkit.getOfflinePlayer(args[2]).uniqueId
                    data = VoteUpAPI.VOTE_MANAGER!!.create(uuid)
                } else {
                    data = null
                    user.sendMessage(plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"])
                }
                else -> {
                    data = null
                    user.sendMessage(plugin.lang[plugin.localeKey, I18n.Type.WARN, "Plugin", "UnknownCmd"])
                }
            }
            if (data != null) BasicUtil.openInventory(plugin, user, CreateInventoryHolder(data, user).inventory)
        }
        return true
    }
}