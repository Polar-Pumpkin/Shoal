package net.shoal.parrot.egg4everyone.command.subcommand;

import net.shoal.parrot.egg4everyone.Egg4everyone;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.command.BaseCommand;

import java.util.Objects;

public class InfoCommand extends BaseCommand {
    public InfoCommand() {
        super(ParrotXAPI.getPlugin(Egg4everyone.class), "info", 1);
        describe("查询指定玩家身上的成就标记");
        perm(".admin");

        addParam(CommandParam.player(0, "目标玩家 ID, 要求该玩家在线", args -> "玩家离线或不存在."));
    }

    @Override
    protected void call(String[] args) {
        final Player user = convert(0, args, Player.class);
        if (Objects.isNull(user)) {
            return;
        }

        final String tag = user.getPersistentDataContainer().get(Egg4everyone.KEY, PersistentDataType.STRING);
        if (StringUtils.isEmpty(tag)) {
            lang.sender.warnMessage(sender, "该玩家身上没有成就标记.");
            return;
        }

        lang.sender.infoMessage(sender, "玩家 &a{0} &r身上的成就标记为 &a{1}&r.", user.getName(), tag);
    }
}
