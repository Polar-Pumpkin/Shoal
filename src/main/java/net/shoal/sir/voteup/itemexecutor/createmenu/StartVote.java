package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StartVote implements MenuItemExecutor {

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        Player user = (Player) event.getWhoClicked();
        if(user.hasPermission(VoteUpPerm.CREATE.perm())) {
            CommonUtil.closeInventory(user);
            VoteManager.getInstance().finishVoteCreating(user.getName());
        } else {
            SoundManager.getInstance().fail(user.getName());
            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7您没有权限这么做. 使用 &d/vote create back &7可以返回投票草稿."));
        }
        return true;
    }
}
