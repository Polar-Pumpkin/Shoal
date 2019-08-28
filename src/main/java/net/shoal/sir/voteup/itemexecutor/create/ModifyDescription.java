package net.shoal.sir.voteup.itemexecutor.create;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.ChatAPIUtil;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import net.shoal.sir.voteup.util.PlaceholderUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ModifyDescription implements MenuItemExecutor {

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        Player user = (Player) event.getWhoClicked();
        if(user.hasPermission(VoteUpPerm.CREATE_CUSTOM_DESCRIPTION.perm())) {
            Vote creating = VoteManager.getInstance().getCreatingVote(user.getName());
            CommonUtil.closeInventory(user);
            SoundManager.getInstance().ding(user.getName());
            ChatAPIUtil.sendEditableList(
                    user,
                    creating.getDescription(),
                    PlaceholderUtil.check(CommonUtil.color("&7投票 &c%TITLE% &7的简述信息 &6&l>>>"), creating),
                    "&a&l[Add] ",
                    "/vote modify desc add ",
                    "&e&l[Edit] ",
                    "/vote modify desc set",
                    "&c&l[Del] ",
                    "/vote modify desc del ",
                    "&a&l>>> &7返回编辑菜单",
                    "/vote create back"
            );
        } else {
            SoundManager.getInstance().fail(user.getName());
            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7您没有权限这么做. 使用 &d/vote create back &7可以返回投票草稿."));
        }
        return true;
    }
}
