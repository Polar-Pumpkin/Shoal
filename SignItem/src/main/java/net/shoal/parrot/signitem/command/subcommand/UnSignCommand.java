package net.shoal.parrot.signitem.command.subcommand;

import net.shoal.parrot.signitem.SignItem;
import net.shoal.parrot.signitem.config.ConfigManager;
import net.shoal.parrot.signitem.utils.SignUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.command.BaseCommand;

import java.util.HashMap;
import java.util.Map;

public class UnSignCommand extends BaseCommand {
    public UnSignCommand() {
        super(SignItem.getInstance(), "unsign", 0);
        describe("抹去物品上的签名");
        mustPlayer(true);
    }

    @Override
    protected void call(String[] args) {
        final ItemStack item = user.getInventory().getItemInMainHand();

        if (item.getType() == Material.AIR) {
            lang.sender.warn(user, "Action.Air");
            user.playSound(user.getLocation(), ConfigManager.failure, 1, 1);
            return;
        }

        final Map<String, String> exist = SignUtil.getSign(item);
        if (!exist.isEmpty() && !user.getName().equals(exist.get("name"))) {
            lang.sender.warn(user, "Action.NotOwner");
            user.playSound(user.getLocation(), ConfigManager.failure, 1, 1);
            return;
        }

        SignUtil.sign(item, new HashMap<>());
        lang.sender.info(user, "Action.Erasing");
        user.playSound(user.getLocation(), ConfigManager.erasing, 1, 1);
    }
}
