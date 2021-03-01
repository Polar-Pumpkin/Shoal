package net.shoal.parrot.signitem.command.subcommand;

import net.shoal.parrot.signitem.SignItem;
import net.shoal.parrot.signitem.config.ConfigManager;
import net.shoal.parrot.signitem.utils.SignUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.command.BaseCommand;
import org.serverct.parrot.parrotx.utils.TimeUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SignCommand extends BaseCommand {
    public SignCommand() {
        super(SignItem.getInstance(), "sign", 1);
        describe("为手持物品签名");
        mustPlayer(true);

        addParam(CommandParam.builder()
                .name("签名内容")
                .description("签名内容")
                .position(0)
                .continuous(true)
                .build());
    }

    @Override
    protected void call(String[] args) {
        final String desc = (String) convert(0, args);
        final ItemStack item = user.getInventory().getItemInMainHand();

        if (Objects.isNull(desc) || desc.isEmpty()) {
            lang.sender.warn(user, "Action.Empty");
            user.playSound(user.getLocation(), ConfigManager.failure, 1, 1);
            return;
        }
        if (item.getType() == Material.AIR) {
            lang.sender.warn(user, "Action.Air");
            user.playSound(user.getLocation(), ConfigManager.failure, 1, 1);
            return;
        }
        if (desc.length() > ConfigManager.descLength) {
            lang.sender.warn(user, "Action.Limit");
            user.playSound(user.getLocation(), ConfigManager.failure, 1, 1);
            return;
        }

        final Map<String, String> content = new HashMap<>();
        content.put("name", user.getName());
        content.put("desc", desc);
        content.put("time", TimeUtil.getFormattedDate(new Date(), new SimpleDateFormat(ConfigManager.timeFormat)));

        SignUtil.sign(item, content);
        lang.sender.info(user, "Action.Success");
        user.playSound(user.getLocation(), ConfigManager.success, 1, 1);
    }
}
