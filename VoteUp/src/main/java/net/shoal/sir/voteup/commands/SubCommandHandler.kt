package net.shoal.sir.voteup.commands

import net.shoal.sir.voteup.commands.sub.Create
import net.shoal.sir.voteup.commands.sub.Reload
import net.shoal.sir.voteup.commands.sub.View
import org.bukkit.plugin.java.JavaPlugin
import org.sct.easylib.util.function.command.CommandHandler

class SubCommandHandler(instance: JavaPlugin, cmd: String) : CommandHandler(instance, cmd) {
    init {
        registerSubCommand("create", Create())
        registerSubCommand("view", View())
        registerSubCommand("reload", Reload())
    }
}