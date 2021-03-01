package net.shoal.parrot.prefixme.subcommand;

import net.shoal.parrot.prefixme.Conf;
import net.shoal.parrot.prefixme.PrefixMe;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.serverct.parrot.parrotx.command.BaseCommand;

import java.util.Arrays;
import java.util.function.Function;

public class GetCommand extends BaseCommand {
    public GetCommand() {
        super(PrefixMe.getInst(), "get", 0);
        describe("获取一个道具");
        perm("PrefixMe.command.get");
        mustPlayer(true);

        addParam(CommandParam.builder()
                .name("prefix/deprefix")
                .description("获取设置称号/消除称号物品")
                .position(0)
                .suggest(() -> new String[]{"prefix", "deprefix"})
                .validate(mode -> Arrays.asList("prefix", "deprefix").contains(mode.toLowerCase()))
                .validateMessage(warn("未知物品名称."))
                .converter((Function<String[], String>) args -> args[0].toLowerCase())
                .build());
    }

    @Override
    protected void call(String[] args) {
        final PlayerInventory inv = user.getInventory();
        ItemStack item = new ItemStack(Material.AIR);
        switch ((String) convert(0, args)) {
            case "prefix":
                item = Conf.prefixItem;
                break;
            case "deprefix":
                item = Conf.deprefixItem;
                break;
        }
        inv.addItem(item.clone());
    }
}
