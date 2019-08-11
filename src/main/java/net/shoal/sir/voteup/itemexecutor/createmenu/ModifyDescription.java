package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.ChatAPIUtil;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.PlaceholderUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ModifyDescription implements MenuItemExecutor {

    private Player user;

    @Override
    public boolean execute(InventoryClickEvent event) {
        user = (Player) event.getWhoClicked();
        Vote creating = VoteManager.getInstance().getCreatingVote(user.getName());
        CommonUtil.closeInventory(user);
        SoundManager.getInstance().ding(user.getName());
        ChatAPIUtil.sendEditableList(
                user,
                creating.getDescription(),
                PlaceholderUtil.check(CommonUtil.color("&7投票 &c%TITLE% &7的简述信息 &6&l>>>"), creating),
                "&a&l[+] ",
                "/vote modify desc add ",
                "&c&l[-] ",
                "/vote modify desc del ",
                "&a&l>>> &7返回编辑菜单",
                "/vote create back"
        );
        return true;
    }
}
