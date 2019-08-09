package net.shoal.sir.voteup.listener;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.data.ChestMenu;
import net.shoal.sir.voteup.data.MenuItem;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    private LocaleUtil locale;

    private ChestMenu gui;
    private MenuItem item;

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        locale = VoteUp.getInstance().getLocale();
        gui = GuiManager.getInstance().checkGui(event.getView().getTitle());

        if(gui != null) {
            locale.debug("&7触发 &cInventoryClickEvent &7事件, 且经检验, 被点击界面为本插件所属: &c" + gui.getTitle());
            event.setCancelled(true);
            locale.debug("&7取消事件 &c" + (event.isCancelled() ? "成功" : "失败"));
            item = gui.getItem(event.getCurrentItem());
            locale.debug("&7尝试获取被点击物品的 MenuItem 对象, 获取到的对象 &c" + (item != null ? "不为" : "为") + " &7null");
            if(item != null) {
                locale.debug("&7MenuItem ID: &c" + item.getId());
                MenuItemExecutor executor = item.getExecutor();
                locale.debug("&7菜单物品动作执行器是否有效: &c" + (executor != null ? "是" : "否"));
                if(executor != null) {
                    executor.execute(event);
                } else {
                    locale.debug("&7菜单物品动作执行器无效, 点击操作已忽略.");
                }
            }
        }
    }
}
