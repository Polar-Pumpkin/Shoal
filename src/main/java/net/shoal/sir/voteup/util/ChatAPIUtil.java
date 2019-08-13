package net.shoal.sir.voteup.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.List;

public class ChatAPIUtil {

    public static TextComponent buildClickText(String text, ClickEvent click, HoverEvent hover) {
        TextComponent msg = build(text);
        msg.setClickEvent(click);
        msg.setHoverEvent(hover);
        return msg;
    }

    public static TextComponent build(String text) {
        BaseComponent[] messages = TextComponent.fromLegacyText(CommonUtil.color(text));
        TextComponent msg = new TextComponent("");
        for(BaseComponent component : messages) {
            msg.addExtra(component);
        }
        return msg;
    }

    public static void sendEditableList(Player user, List<String> content, String title, String add, String addCmd, String set, String setCmd, String del, String delCmd, String back, String backCmd) {
        user.sendMessage(CommonUtil.color(title));
        TextComponent clickableAdd;
        if(!content.isEmpty()) {
            for(String desc : content) {
                TextComponent result = new TextComponent("");
                int index = content.indexOf(desc);
                clickableAdd = buildClickText(
                        add,
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, addCmd + (index + 1)),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&a点击在下方插入一行内容")))
                );
                TextComponent clickableSet = buildClickText(
                        set,
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, setCmd + index),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&e点击设置此行内容")))
                );
                TextComponent clickableDel = buildClickText(
                        del,
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, delCmd + index),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&c点击删除此行内容")))
                );
                result.addExtra(clickableAdd);
                result.addExtra(clickableSet);
                result.addExtra(clickableDel);
                result.addExtra(build(CommonUtil.color(desc)));
                user.spigot().sendMessage(result);
            }
        } else {
            TextComponent result = new TextComponent("");
            clickableAdd = buildClickText(
                    add,
                    new ClickEvent(ClickEvent.Action.RUN_COMMAND, addCmd + 0),
                    new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&a点击添加第一行内容")))
            );
            result.addExtra(clickableAdd);
            result.addExtra(build(CommonUtil.color("&7无.")));
            user.spigot().sendMessage(result);
        }

        user.spigot().sendMessage(
                buildClickText(
                        back,
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, backCmd),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&a点击返回")))
                )
        );
    }

}
