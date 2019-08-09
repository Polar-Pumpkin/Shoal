package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteType;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SwitchType implements MenuItemExecutor {

    private LocaleUtil locale;
    private Player user;

    private boolean result;
    private String success;
    private String failure;

    @Override
    public boolean execute(InventoryClickEvent event) {
        locale = VoteUp.getInstance().getLocale();
        user = (Player) event.getWhoClicked();
        CommonUtil.closeInventory(user);
        user.playSound(user.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        result = switchVoteType(user.getName());
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        success = locale.buildMessage(VoteUp.getInstance().getLocaleKey(), MessageType.INFO, "&7切换投票类型成功.");
        failure = locale.buildMessage(VoteUp.getInstance().getLocaleKey(), MessageType.WARN, "&7切换投票类型失败, 您的 ID 下没有待发布的投票.");
        user.sendMessage(result ? success : failure);
        CommonUtil.openInventory(user, InventoryUtil.parsePlaceholder(GuiManager.getInstance().getMenu("CreateMenu"), VoteManager.getInstance().getCreatingVote(user.getName())).getInventory());
        return true;
    }

    private boolean switchVoteType(String key) {
        Vote creating = VoteManager.getInstance().getCreatingVote(key);
        return VoteManager.getInstance().setCreatingVoteData(key, VoteDataType.TYPE, (creating.getType() == VoteType.NORMAL ? VoteType.REACHAMOUNT : VoteType.NORMAL));
    }
}
