package net.shoal.parrot.prefixme;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.parrot.prefixme.subcommand.GetCommand;
import net.shoal.parrot.prefixme.subcommand.SetCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.command.PCommand;
import org.serverct.parrot.parrotx.command.subcommands.DebugCommand;
import org.serverct.parrot.parrotx.command.subcommands.HelpCommand;
import org.serverct.parrot.parrotx.command.subcommands.ReloadCommand;
import org.serverct.parrot.parrotx.command.subcommands.VersionCommand;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

public class Command extends CommandHandler {
    public Command() {
        super(PrefixMe.getInst(), "prefixme");
        register(new GetCommand());
        register(new SetCommand());

        register(new DebugCommand(plugin, "PrefixMe.command.debug"));
        register(new ReloadCommand(plugin, "PrefixMe.command.reload"));
        register(new VersionCommand(plugin));
        register(new HelpCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(plugin.getLang().data.get(plugin.localeKey, "Message.Empty"));
//            PCommand defCommand = commands.get((Objects.isNull(defaultCmd) ? "help" : defaultCmd));
//            if (defCommand == null) {
//                // plugin.lang.getHelp(plugin.localeKey).forEach(sender::sendMessage);
//                formatHelp().forEach(sender::sendMessage);
//            } else {
//                boolean hasPerm = (defCommand.getPermission() == null || defCommand.getPermission().equals("")) || sender.hasPermission(defCommand.getPermission());
//                if (hasPerm) {
//                    return defCommand.execute(sender, args);
//                }
//
//                String msg = plugin.getLang().data.warn("您没有权限这么做.");
//                if (sender instanceof Player) {
//                    TextComponent text = JsonChatUtil.getFromLegacy(msg);
//                    text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&7所需权限 ▶ &c" + defCommand.getPermission()))));
//                    ((Player) sender).spigot().sendMessage(text);
//                } else sender.sendMessage(msg);
//            }
            return true;
        }

        PCommand pCommand = commands.get(args[0].toLowerCase());
        if (pCommand == null) {
            if (sender instanceof Player) {
                if (!sender.hasPermission("PrefixMe.use")) {
                    sender.sendMessage(plugin.getLang().data.getInfo("Plugin.NoPerm"));
                    return true;
                }
                final StringBuilder builder = new StringBuilder();
                for (String arg : args) {
                    builder.append(arg).append(" ");
                }
                final String content = builder.toString().trim();
                if (content.length() <= 0) {
                    plugin.getLang().sender.error((Player) sender, "Message.Empty");
                    return true;
                }
                if (content.length() > Conf.limit) {
                    plugin.getLang().sender.error((Player) sender, "Message.Limit");
                    return true;
                }
                PrefixManager.getInst().set(((Player) sender).getUniqueId(), content);
                sender.sendMessage(plugin.getLang().data.get(plugin.localeKey, "Message", "Set", content));
            }
//            sender.sendMessage(plugin.getLang().data.warn("未知命令, 请检查您的命令拼写是否正确."));
//            plugin.getLang().log.error(I18n.EXECUTE, "子命令/" + args[0], sender.getName() + " 尝试执行未注册子命令");
            return true;
        }

        boolean hasPerm = (pCommand.getPermission() == null || pCommand.getPermission().equals("")) || sender.hasPermission(pCommand.getPermission());
        if (hasPerm) {
            String[] newArg = new String[args.length - 1];
            if (args.length >= 2) {
                System.arraycopy(args, 1, newArg, 0, args.length - 1);
            }
            return pCommand.execute(sender, newArg);
        }

        String msg = plugin.getLang().data.warn("您没有权限这么做.");
        if (sender instanceof Player) {
            TextComponent text = JsonChatUtil.getFromLegacy(msg);
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&7所需权限 ▶ &c" + pCommand.getPermission()))));
            ((Player) sender).spigot().sendMessage(text);
        } else sender.sendMessage(msg);

        return true;
    }
}
