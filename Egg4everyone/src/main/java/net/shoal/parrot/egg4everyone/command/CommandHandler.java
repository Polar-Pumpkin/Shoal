package net.shoal.parrot.egg4everyone.command;

import net.shoal.parrot.egg4everyone.Egg4everyone;
import net.shoal.parrot.egg4everyone.command.subcommand.InfoCommand;
import net.shoal.parrot.egg4everyone.command.subcommand.RemoveCommand;
import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.command.subcommands.DebugCommand;
import org.serverct.parrot.parrotx.command.subcommands.HelpCommand;
import org.serverct.parrot.parrotx.command.subcommands.ReloadCommand;
import org.serverct.parrot.parrotx.command.subcommands.VersionCommand;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoload;

@PAutoload
public class CommandHandler extends org.serverct.parrot.parrotx.command.CommandHandler {

    public CommandHandler() {
        super(ParrotXAPI.getPlugin(Egg4everyone.class), "egg4everyone");
        register(new InfoCommand());
        register(new RemoveCommand());

        register(new HelpCommand(plugin));
        register(new VersionCommand(plugin));
        register(new ReloadCommand(plugin, ".admin"));
        register(new DebugCommand(plugin, ".admin"));
    }
}
