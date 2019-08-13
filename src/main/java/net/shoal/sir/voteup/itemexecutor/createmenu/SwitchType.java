package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteType;
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
        CommonUtil.closeInventory(user);
        SoundManager.getInstance().ding(user.getName());
        boolean result = switchVoteType(user.getName());
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        CommonUtil.openInventory(
                user,
                InventoryUtil.parsePlaceholder(
                        GuiManager.getInstance().getMenu(GuiManager.CREATE_MENU),
                        VoteManager.getInstance().getCreatingVote(user.getName()),
                        user
                )
        );
        return true;
    }

    private boolean switchVoteType(String key) {
        Vote creating = VoteManager.getInstance().getCreatingVote(key);
        return VoteManager.getInstance().setCreatingVoteData(key, VoteDataType.TYPE, (creating.getType() == VoteType.NORMAL ? VoteType.REACHAMOUNT : VoteType.NORMAL));
    }
}
