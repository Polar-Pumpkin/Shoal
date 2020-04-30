package net.shoal.sir.voteup.util;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.config.ExecutorManager;
import net.shoal.sir.voteup.data.ChestMenu;
import net.shoal.sir.voteup.data.MenuItem;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.ExecutorType;
import net.shoal.sir.voteup.enums.Position;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryUtil {

    private static LocaleUtil locale;

    public static ChestMenu buildChestMenu(File dataFile) {
        locale = VoteUp.getInstance().getLocale();
        plugin.lang.debug("&7调用 buildChestMenu 方法.");

        String id = "unloadedMenu";
        String title = "VoteUp 未初始化菜单";
        int row = 6;
//        Inventory result = Bukkit.createInventory(null, row * 9, title);
        List<MenuItem> itemList = new ArrayList<>();
        Sound open = null;
        Sound close = null;

        if (dataFile != null) {
            FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

            id = CommonUtil.getNoExFileName(dataFile.getName());
            ConfigurationSection setting = data.getConfigurationSection("Setting");
            plugin.lang.debug("&7读取菜单设置数据.");
            if (setting != null) {
                title = CommonUtil.color(setting.getString("Title"));
                plugin.lang.debug("&7菜单标题: &c" + title);
                row = setting.getInt("Row");
                plugin.lang.debug("&7菜单行数: &c" + row);
//                result = Bukkit.createInventory(null, row * 9, title);
                if (data.isConfigurationSection("Setting.Sound")) {
                    plugin.lang.debug("&7发现菜单音效设置.");
                    ConfigurationSection sound = setting.getConfigurationSection("Sound");
                    if (sound != null) {
                        open = Sound.valueOf(Objects.requireNonNull(sound.getString("Open")).toUpperCase());
                        plugin.lang.debug("&7菜单打开音效: &c" + open.toString());
                        close = Sound.valueOf(Objects.requireNonNull(sound.getString("Close")).toUpperCase());
                        plugin.lang.debug("&7菜单关闭音效: &c" + close.toString());
                    }
                }
            }

            ConfigurationSection items = data.getConfigurationSection("Items");
            plugin.lang.debug("&7读取菜单物品数据.");
            for (String key : Objects.requireNonNull(items).getKeys(false)) {
                plugin.lang.debug("&7物品唯一标识 ID: &c" + key);
                ConfigurationSection target = items.getConfigurationSection(key);
                ItemStack item = CommonUtil.buildItem(Objects.requireNonNull(target));
                plugin.lang.debug("&7构建物品数据: &c" + item.toString());
                String x = target.getString("Position.X");
                String y = target.getString("Position.Y");
                plugin.lang.debug("&7物品坐标: (&c" + x + "&7, &c" + y + "&7)");
//                result.setItem(Position.getPositon(x, y), item);
                MenuItemExecutor executor;
                try {
                    executor = ExecutorManager.getInstance().getExecutor(ExecutorType.valueOf(key.toUpperCase()));
                } catch(Throwable e) {
                    executor = null;
                }
                plugin.lang.debug("&7菜单物品动作执行器是否有效: &c" + (executor != null ? "是" : "否"));
                String perm;
                perm = target.getString("ViewPermission");
                plugin.lang.debug("&7菜单物品可视权限: &c" + perm);
                itemList.add(new MenuItem(key, item, executor, x, y, perm));
                plugin.lang.debug("&7已添加物品至菜单物品列表.");
            }
            plugin.lang.debug("&7共加载 &c" + itemList.size() + " &7项菜单物品.");
        }

        return new ChestMenu(id, title, row, itemList, open, close);
    }

    public static Inventory parsePlaceholder(ChestMenu data, Vote voteData, Player user) {
        locale = VoteUp.getInstance().getLocale();
        plugin.lang.debug("&7调用 parsePlaceholder 方法.");
        plugin.lang.debug("&7菜单参数是否有效: &c" + (data != null ? "是" : "否"));
        plugin.lang.debug("&7菜单数据: &c" + data.toString());
        plugin.lang.debug("&7投票数据: &c" + voteData.toString());
        Inventory inventory = constructInventory(data, user, voteData.getId());
        for (MenuItem item : data.getItems()) {
            String perm = item.getPermission();
            if(perm != null && !perm.equals("")) {
                if(!user.hasPermission(perm)) {
                    continue;
                }
            }
            for(int position : Position.getPositionList(item.getXPosition(), item.getYPosition())) {
                inventory.setItem(
                        position,
                        VoteUpPlaceholder.applyPlaceholder(item.getItem().clone(), voteData)
                );
            }
        }
        return inventory;
    }

    public static Inventory constructInventory(ChestMenu gui, @NonNull Player user, String additionTitle) {
        locale = VoteUp.getInstance().getLocale();
        plugin.lang.debug("&7调用 constructInventory 方法.");
        plugin.lang.debug("&7菜单参数是否有效: &c" + (gui != null ? "是" : "否"));
        plugin.lang.debug("&7菜单数据: &c" + gui.toString());
        plugin.lang.debug("&7目标玩家: &c" + user.getName());
        plugin.lang.debug("&7菜单附加标题: &c" + additionTitle);

        Inventory result = Bukkit.createInventory(null, gui.getRow() * 9, gui.getTitle() + additionTitle);
        for(MenuItem item : gui.getItems()) {
            plugin.lang.debug("&7获取菜单物品: &c" + item.toString());

            if("MODIFY_AUTOCAST".equalsIgnoreCase(item.getId())) {
                if(!VoteUp.autocastEnable) {
                    continue;
                }
            }
            String perm = item.getPermission();
            plugin.lang.debug("&7菜单物品可视权限: &c" + perm);
            if(perm != null && !perm.equals("")) {
                plugin.lang.debug("&7菜单物品可视权限有效, 玩家是否拥有对应权限: &c" + (user.hasPermission(perm) ? "是" : "否"));
                if(!user.hasPermission(perm)) {
                    continue;
                }
            }
            for(int position : Position.getPositionList(item.getXPosition(), item.getYPosition())) {
                result.setItem(position, item.getItem().clone());
            }
        }
        return result;
    }
}

