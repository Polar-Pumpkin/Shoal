package net.shoal.sir.voteup.listener;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.ChestMenu;
import net.shoal.sir.voteup.data.MenuItem;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        VoteManager vm = VoteManager.getInstance();
        Player user = (Player) event.getWhoClicked();
        ChestMenu gui = GuiManager.getInstance().checkGui(event.getView().getTitle());

        if(gui != null) {
            plugin.lang.debug("&7触发 &cInventoryClickEvent &7事件, 且经检验, 被点击界面为本插件所属: &c" + gui.getTitle());
            event.setCancelled(true);
            plugin.lang.debug("&7取消事件 &c" + (event.isCancelled() ? "成功" : "失败"));
            String id = event.getView().getTitle().replace(gui.getTitle(), "");
            plugin.lang.debug("&7获取到的投票 ID 为: &c" + id);
            MenuItem item = gui.getItem(event.getCurrentItem(), ("CreateMenu".equalsIgnoreCase(gui.getId()) ? vm.getCreatingVote(user.getName()) : vm.getVote(id)));
            plugin.lang.debug("&7尝试获取被点击物品的 MenuItem 对象, 获取到的对象 &c" + (item != null ? "不为" : "为") + " &7null");
            if(item != null) {
                plugin.lang.debug("&7MenuItem ID: &c" + item.getId());
                MenuItemExecutor executor = item.getExecutor();
                plugin.lang.debug("&7菜单物品动作执行器是否有效: &c" + (executor != null ? "是" : "否"));
                if(executor != null) {
                    executor.execute(event, id);
                } else {
                    plugin.lang.debug("&7菜单物品动作执行器无效, 点击操作已忽略.");
                }
            }
        }
    }
}
