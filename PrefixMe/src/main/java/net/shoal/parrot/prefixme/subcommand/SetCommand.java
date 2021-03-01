package net.shoal.parrot.prefixme.subcommand;

import net.shoal.parrot.prefixme.PrefixManager;
import net.shoal.parrot.prefixme.PrefixMe;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.command.BaseCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class SetCommand extends BaseCommand {
    public SetCommand() {
        super(PrefixMe.getInst(), "set", 1);
        describe("指定设置玩家的称号");
        perm("PrefixMe.command.set");

        addParam(CommandParam.builder()
                .name("玩家 ID")
                .description("目标玩家 ID")
                .position(0)
                .suggest(() -> {
                    final List<String> result = new ArrayList<>();
                    Bukkit.getOnlinePlayers().forEach(user -> result.add(user.getName()));
                    return result.toArray(new String[0]);
                })
                .validate(username -> Objects.nonNull(Bukkit.getPlayerExact(username)))
                .validateMessage(lang.data.get(plugin.localeKey, "Message.TargetNotExist"))
                .converter((Function<String[], Player>) args -> Bukkit.getPlayerExact(args[0]))
                .build());
    }

    @Override
    protected void call(String[] args) {
        final Player user = (Player) convert(0, args);
        if (Objects.isNull(user)) {
            sender.sendMessage(lang.data.get(plugin.localeKey, "Message.TargetNotExist"));
            return;
        }
        String[] newArg = new String[args.length - 1];
        if (args.length >= 2) {
            System.arraycopy(args, 1, newArg, 0, args.length - 1);
        } else {
            sender.sendMessage(plugin.getLang().data.get(plugin.localeKey, "Message.Empty"));
            return;
        }
        final StringBuilder builder = new StringBuilder();
        for (String arg : newArg) {
            builder.append(arg).append(" ");
        }
        final String content = builder.toString().trim();
        PrefixManager.getInst().set(user.getUniqueId(), content);
        sender.sendMessage(lang.data.get(plugin.localeKey, "Message", "Force", user.getName(), content));
    }
}
