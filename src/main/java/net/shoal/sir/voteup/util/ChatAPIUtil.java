package net.shoal.sir.voteup.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ChatAPIUtil {

    public static TextComponent buildClickText(String text, ClickEvent click, HoverEvent hover) {
        TextComponent msg = build(text);
        msg.setClickEvent(click);
        msg.setHoverEvent(hover);
        return msg;
    }

    public static TextComponent build(String text) {
        BaseComponent[] messages = TextComponent.fromLegacyText(text);
        TextComponent msg = new TextComponent("");
        for(BaseComponent component : messages) {
            msg.addExtra(component);
        }
        return msg;
    }

}
