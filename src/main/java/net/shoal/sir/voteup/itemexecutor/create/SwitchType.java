package net.shoal.sir.voteup.itemexecutor.create;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.*;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SwitchType implements MenuItemExecutor {

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        Player user = (Player) event.getWhoClicked();
        if(user.hasPermission(VoteUpPerm.CREATE_CUSTOM_TYPE.perm())) {
            CommonUtil.closeInventory(user);
            SoundManager.getInstance().ding(user.getName());
            boolean result = switchVoteType(user.getName());
            locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));
            CommonUtil.openInventory(
                    user,
                    InventoryUtil.parsePlaceholder(
                            GuiManager.getInstance().getMenu(GuiConfiguration.CREATE_MENU.getName()),
                            VoteManager.getInstance().getCreatingVote(user.getName()),
                            user
                    )
            );
        } else {
            SoundManager.getInstance().fail(user.getName());
            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7您没有权限这么做. 使用 &d/vote create back &7可以返回投票草稿."));
        }
        return true;
    }

    private boolean switchVoteType(String key) {
        Vote creating = VoteManager.getInstance().getCreatingVote(key);
        switch(creating.getType()) {
            case NORMAL:
                return VoteManager.getInstance().setCreatingVoteData(key, VoteDataType.TYPE, VoteType.REACHAMOUNT);
            case REACHAMOUNT:
                return VoteManager.getInstance().setCreatingVoteData(key, VoteDataType.TYPE, VoteType.LEASTNOT);
            case LEASTNOT:
            default:
                return VoteManager.getInstance().setCreatingVoteData(key, VoteDataType.TYPE, VoteType.NORMAL);
        }
    }
}
