package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.ChatAPIUtil;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import net.shoal.sir.voteup.util.PlaceholderUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class ModifyDescription implements MenuItemExecutor {

    private LocaleUtil locale;
    private Player user;
    private Vote creating;
    private List<String> description;
    private TextComponent add;
    private TextComponent del;

    @Override
    public boolean execute(InventoryClickEvent event) {
        locale = VoteUp.getInstance().getLocale();
        user = (Player) event.getWhoClicked();
        creating = VoteManager.getInstance().getCreatingVote(user.getName());
        description = creating.getDescription();

        CommonUtil.closeInventory(user);

        user.playSound(user.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        user.sendMessage(PlaceholderUtil.check(CommonUtil.color("&7投票 &c%TITLE% &7的简述信息 &6&l>>>"), creating));

        TextComponent result = new TextComponent("");

        if(!description.isEmpty()) {
            for(String desc : description) {
                int index = description.indexOf(desc);
                add = ChatAPIUtil.buildClickText(
                        CommonUtil.color("&a&l[+] "),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "vote dm add " + (index + 1)),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&a点击在下方插入一行描述.")))
                );
                del = ChatAPIUtil.buildClickText(
                        CommonUtil.color("&c&l[-] "),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "vote dm del " + index),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&c点击删除此行描述.")))
                );
                result.addExtra(add);
                result.addExtra(del);
                result.addExtra(ChatAPIUtil.build(CommonUtil.color(desc)));
            }
        } else {
            add = ChatAPIUtil.buildClickText(
                    CommonUtil.color("&a&l[+] "),
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, "vote dm add 0"),
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&a点击添加一行描述.")))
            );
            result.addExtra(add);
            result.addExtra(ChatAPIUtil.build(CommonUtil.color("&7无.")));
        }
        user.spigot().sendMessage(result);
        return true;
    }
}
