package net.shoal.sir.voteup.util;

import com.google.common.collect.Lists;
import net.shoal.sir.voteup.VoteUp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CommonUtil {

    private static LocaleUtil locale;

    /**
     * 取服务器在线玩家
     *
     * @return 玩家集合
     */
    public static List<Player> getOnlinePlayers() {
        // 实例化两个List用于存放Player和World
        List<Player> players = Lists.newArrayList();
        List<World> worlds = Lists.newArrayList();
        worlds.addAll(Bukkit.getWorlds());
        // 遍历所有的世界
        for (int i = 0; i < worlds.size(); i++) {
            // 如果第i个世界的玩家是空的则进行下一次循环
            if (worlds.get(i).getPlayers().isEmpty()) {
                continue;
            } else {
                // 不是空的则添加到players集合中
                players.addAll(worlds.get(i).getPlayers());
            }
        }
        return players;
    }

    public static ItemStack buildItem(ConfigurationSection section) {
        ConfigurationSection itemSection = section.getConfigurationSection("ItemStack");
        if(itemSection != null) {
            Material material = Material.valueOf(Objects.requireNonNull(itemSection.getString("Material")).toUpperCase());
            ItemStack result = new ItemStack(material);
            ItemMeta meta = result.getItemMeta();

            if(meta == null) {
                meta = Bukkit.getItemFactory().getItemMeta(material);
            }
            assert meta != null;
            meta.setDisplayName(color(Objects.requireNonNull(itemSection.getString("Display"))));

            List<String> lore = new ArrayList<>();
            for(String text : itemSection.getStringList("Lore")) {
                lore.add(color(text));
            }
            meta.setLore(lore);

            if(itemSection.isConfigurationSection("Enchants")) {
                ConfigurationSection enchants = itemSection.getConfigurationSection("Enchants");
                for(String key : enchants.getKeys(false)) {
                    meta.addEnchant(Objects.requireNonNull(Enchantment.getByName(key.toUpperCase())), enchants.getInt(key), true);
                }
            }

            List<String> itemFlag = itemSection.getStringList("ItemFlag");
            if(itemFlag != null && !itemFlag.isEmpty()) {
                for(String key : itemFlag) {
                    meta.addItemFlags(ItemFlag.valueOf(key.toUpperCase()));
                }
            }

            result.setItemMeta(meta);
            return result;
        }
        return new ItemStack(Material.AIR);
    }

    public static String getNoExFileName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot >-1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static void closeInventory(HumanEntity humanEntity) {
        BukkitRunnable invTask = new BukkitRunnable() {
            @Override
            public void run() {
                humanEntity.closeInventory();
            }
        };
        invTask.runTask(VoteUp.getInstance());
    }

    public static void openInventory(HumanEntity humanEntity, Inventory inventory){
        BukkitRunnable invTask = new BukkitRunnable() {
            @Override
            public void run() {
                humanEntity.openInventory(inventory);
            }
        };
        invTask.runTask(VoteUp.getInstance());
    }

    public static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void message(String text, Player user) {
        user.sendMessage(color(text));
    }

    public static void message(String text, String playerName) {
        Player target = Bukkit.getPlayer(playerName);
        if(target != null && target.isOnline()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    target.sendMessage(color(text));
                }
            }.runTaskLater(VoteUp.getInstance(), 1);
        }
    }

    public static void broadcastTitle(String title, String subtitle) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendTitle(color(title), color(subtitle), 1 * 20, 5 * 20, 3 * 20));
    }
}
