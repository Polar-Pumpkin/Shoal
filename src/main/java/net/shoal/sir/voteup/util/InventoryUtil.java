package net.shoal.sir.voteup.util;

import net.shoal.sir.voteup.VoteUp;
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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryUtil {

    private static LocaleUtil locale;

    public static ChestMenu buildChestMenu(FileConfiguration data) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7调用 buildChestMenu 方法.");

        String id = "unloadedMenu";
        String title = "VoteUp 未初始化菜单";
        int row = 6;
        Inventory result = Bukkit.createInventory(null, row * 9, title);
        List<MenuItem> itemList = new ArrayList<>();
        Sound open = null;
        Sound close = null;

        if(data != null) {
            id = CommonUtil.getNoExFileName(data.getName());
            ConfigurationSection setting = data.getConfigurationSection("Setting");
            locale.debug("&7读取菜单设置数据.");
            if(setting != null) {
                title = CommonUtil.color(setting.getString("Title"));
                locale.debug("&7菜单标题: &c" + title);
                row = setting.getInt("Row");
                locale.debug("&7菜单行数: &c" + row);
                result = Bukkit.createInventory(null, row * 9, title);
                if(data.isConfigurationSection("Setting.Sound")) {
                    locale.debug("&7发现菜单音效设置.");
                    ConfigurationSection sound = setting.getConfigurationSection("Sound");
                    if(sound != null) {
                        open = Sound.valueOf(Objects.requireNonNull(sound.getString("Open")).toUpperCase());
                        locale.debug("&7菜单打开音效: &c" + open.toString());
                        close = Sound.valueOf(Objects.requireNonNull(sound.getString("Close")).toUpperCase());
                        locale.debug("&7菜单关闭音效: &c" + close.toString());
                    }
                }
            }

            ConfigurationSection items = data.getConfigurationSection("Items");
            locale.debug("&7读取菜单物品数据.");
            for(String key : Objects.requireNonNull(items).getKeys(false)) {
                locale.debug("&7物品唯一标识 ID: &c" + key);
                ConfigurationSection target = items.getConfigurationSection(key);
                ItemStack item = CommonUtil.buildItem(Objects.requireNonNull(target));
                locale.debug("&7构建物品数据: &c" + item.toString());
                int x = target.getInt("Position.X");
                int y = target.getInt("Position.Y");
                locale.debug("&7物品坐标: (&c" + x + "&7, &c" + y +"&7)");
                result.setItem(Position.getPositon(x, y), item);
                MenuItemExecutor executor = ExecutorManager.getInstance().getExecutor(ExecutorType.valueOf(key.toUpperCase()));
                locale.debug("&7菜单物品动作执行器是否有效: &c" + (executor != null ? "是" : "否"));
                itemList.add(new MenuItem(key, item, executor, x, y));
                locale.debug("&7已添加物品至菜单物品列表.");
            }
            locale.debug("&7共加载 &c" + itemList.size() + " &7项菜单物品.");
        }

        return new ChestMenu(id, title, result, itemList, open, close);
    }

    public static ChestMenu parsePlaceholder(ChestMenu data, Vote voteData) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7调用 parsePlaceholder 方法.");
        locale.debug("&7菜单参数是否有效: &c" + (data != null ? "是" : "否"));
        locale.debug("&7菜单数据: &c" + data.toString());
        locale.debug("&7投票数据: &c" + voteData.toString());
        Inventory inventory = data.getInventory();
        List<MenuItem> items = data.getItems();
        List<MenuItem> newItems = new ArrayList<>();
        for(MenuItem item : items) {
            ItemStack replaced = PlaceholderUtil.applyPlaceholder(item.getItem(), voteData);
            item.setItem(replaced);
            inventory.setItem(Position.getPositon(item.getXPosition(), item.getYPosition()), replaced);
            newItems.add(item);
        }
        data.setInventory(inventory);
        data.setItems(newItems);
        return data;
    }
}
