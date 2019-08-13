package net.shoal.sir.voteup.command;

import net.shoal.sir.voteup.command.subcommands.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class CommandHandler implements CommandExecutor {

    private Map<String, Subcommand> subcommandMap = new HashMap<>();

    public CommandHandler() {
        registerSubcommand("help", new Help());
        registerSubcommand("create", new Create());
        registerSubcommand("reload", new Reload());
        registerSubcommand("debug", new DebugMode());
        registerSubcommand("modify", new Modify());
        registerSubcommand("view", new View());
    }

    private void registerSubcommand(String command, Subcommand executor) {
        subcommandMap.put(command, executor);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length >= 1) {
            if(subcommandMap.containsKey(args[0])) {
                return subcommandMap.get(args[0]).execute(sender, args);
            }
        } else {
            return subcommandMap.get("help").execute(sender, args);
        }
        return false;
    }
}
