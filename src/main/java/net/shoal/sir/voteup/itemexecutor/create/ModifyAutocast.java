package net.shoal.sir.voteup.itemexecutor.create;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.ChatAPIUtil;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ModifyAutocast implements MenuItemExecutor {

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        Player user = (Player) event.getWhoClicked();
        if(user.hasPermission(VoteUpPerm.CREATE_CUSTOM_AUTOCAST.perm())) {
            Vote creating = VoteManager.getInstance().getCreatingVote(user.getName());
            CommonUtil.closeInventory(user);
            SoundManager.getInstance().ding(user.getName());
            ChatAPIUtil.sendEditableList(
                    user,
                    creating.getAutoCast(),
                    VoteUpPlaceholder.check(CommonUtil.color("&7投票 &c%TITLE% &7的自动执行命令列表 &6&l>>>"), creating),
                    "&a&l[Add] ",
                    "/vote modify autocast add ",
                    "&e&l[Edit] ",
                    "/vote modify autocast set",
                    "&c&l[Del] ",
                    "/vote modify autocast del ",
                    "&a&l>>> &7返回编辑菜单",
                    "/vote create back"
            );
        } else {
            SoundManager.getInstance().fail(user.getName());
            user.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7您没有权限这么做. 使用 &d/vote create back &7可以返回投票草稿."));
        }
        return true;
    }
}
