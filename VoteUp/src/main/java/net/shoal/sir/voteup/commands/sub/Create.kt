package net.shoal.sir.voteup.commands.sub

import io.netty.handler.codec.smtp.SmtpRequests.data
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.sct.easylib.util.function.command.SubCommand
import java.util.*


class Create : SubCommand {
    override fun execute(sender: CommandSender, args: Array<out String>): Boolean {
        if (sender is Player) {
            when (args.size) {
                1 -> {

                }
                2-> {

                }
                3 -> {

                }
            }
            return true
        }
        return false
    }

    override fun getParams(): MutableMap<Int, Array<String>> {
        return mutableMapOf(
                1 to arrayOf("back")
        )
    }

}