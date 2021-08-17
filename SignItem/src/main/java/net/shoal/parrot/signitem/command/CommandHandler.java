package net.shoal.parrot.signitem.command;

import net.shoal.parrot.signitem.SignItem;
import net.shoal.parrot.signitem.command.subcommand.SignCommand;
import net.shoal.parrot.signitem.command.subcommand.UnSignCommand;
import org.serverct.parrot.parrotx.command.subcommands.DebugCommand;
import org.serverct.parrot.parrotx.command.subcommands.HelpCommand;
import org.serverct.parrot.parrotx.command.subcommands.ReloadCommand;
import org.serverct.parrot.parrotx.command.subcommands.VersionCommand;

public class CommandHandler extends org.serverct.parrot.parrotx.command.CommandHandler {
    public CommandHandler() {
        super(SignItem.getInstance(), "signitem");
        register(new SignCommand());
        register(new UnSignCommand());

        register(new HelpCommand(plugin));
        register(new DebugCommand(plugin, "SignItem.admin"));
        register(new VersionCommand(plugin));
        register(new ReloadCommand(plugin, "SignItem.admin"));
    }
}
