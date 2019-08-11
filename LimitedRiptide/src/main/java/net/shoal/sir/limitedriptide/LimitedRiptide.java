package net.shoal.sir.limitedriptide;

import net.shoal.sir.limitedriptide.enums.MessageType;
import net.shoal.sir.limitedriptide.utils.LocaleUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerRiptideEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class LimitedRiptide extends JavaPlugin implements Listener, CommandExecutor {

    private File configFile = new File(getDataFolder() + File.separator + "config.yml");
    private FileConfiguration config;
    private Map<Player, Boolean> glidingPlayers = new HashMap<>();

    private String localeKey;
    private LocaleUtil locale;

    @Override
    public void onEnable() {
        // Plugin startup logic
        if(!configFile.exists()) {
            saveDefaultConfig();
        }
        config = getConfig();
        localeKey = config.getString("Language");
        locale = new LocaleUtil(this);

        Bukkit.getPluginCommand("limitedriptide").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(args.length > 0) {
            switch (args[0]) {
                case "set":
                    if(sender.hasPermission("LimitedRiptide.set")) {
                        if(args.length == 3) {
                            if("normal".equalsIgnoreCase(args[1])) {
                                setCost(sender, Integer.parseInt(args[2]), false);
                            } else if("flying".equalsIgnoreCase(args[1])) {
                                setCost(sender, Integer.parseInt(args[2]), true);
                            } else {
                                sender.sendMessage(locale.getMessage(localeKey, MessageType.WARN, "Command", "Help.Set"));
                            }
                        } else {
                            sender.sendMessage(locale.getMessage(localeKey, MessageType.WARN, "Command", "Help.Set"));
                        }
                    } else {
                        sender.sendMessage(locale.getMessage(localeKey, MessageType.ERROR, "Command", "NoPermission"));
                    }
                    break;
                case "reload":
                    if(sender.hasPermission("LimitedRiptide.reload")) {
                        if(args.length == 1) {
                            reloadConfig();
                            config = getConfig();
                            localeKey = config.getString("Language");
                            sender.sendMessage(locale.getMessage(localeKey, MessageType.INFO, "Command", "ReloadSuccess"));
                        } else {
                            sender.sendMessage(locale.getMessage(localeKey, MessageType.WARN, "Command", "Help.Reload"));
                        }
                    } else {
                        sender.sendMessage(locale.getMessage(localeKey, MessageType.ERROR, "Command", "NoPermission"));
                    }
                    break;
                case "help":
                    if(args.length == 1) {
                        for(String msg : locale.getHelpMessage(localeKey)) {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
                        }
                    } else {
                        sender.sendMessage(locale.getMessage(localeKey, MessageType.WARN, "Command", "Help.Help"));
                    }
                    break;
                case "debug":
                    if(sender.hasPermission("LimitedRiptide.debug")) {
                        FileConfiguration config = getConfig();
                        boolean result = !config.getBoolean("Debug");
                        config.set("Debug", result);
                        sender.sendMessage(
                                locale.getMessage(localeKey, MessageType.INFO, "Command", "ToggleDebug")
                                        .replace("%debug%", (
                                                result ? ChatColor.translateAlternateColorCodes('&', "&a&lENABLED") : ChatColor.translateAlternateColorCodes('&', "&c&lDISABLED")
                                                )
                                        )
                        );
                        saveConfig();
                    }
                    break;
                default:
                    sender.sendMessage(locale.getMessage(localeKey, MessageType.ERROR, "Command", "Help.Unknown"));
                    break;
            }
        } else {
            for(String msg : locale.getHelpMessage(localeKey)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
            }
        }
        return true;
    }

    @EventHandler
    public void onInteract(PlayerRiptideEvent event) {
        locale.debug("&7PlayerRiptideEvent activated: (&aPlayer&7) &c" + event.getPlayer().getName());
        PlayerInventory userInv = event.getPlayer().getInventory();
        ItemStack trident = event.getItem();
        locale.debug("&7Item information: " + trident.toString());
        boolean isMainHand = userInv.getItemInMainHand().getType() == Material.TRIDENT;
        locale.debug("&7Is trident in main hand? " + (isMainHand ? "&a&lYes" : "&c&lNo"));
        boolean isGliding = glidingPlayers.get(event.getPlayer());
        locale.debug("&7Is player flying with Elytra? " + (isGliding ? "&a&lYes" : "&c&lNo"));
        ItemMeta itemMeta = trident.getItemMeta();
        locale.debug("&7Is target trident's ItemMeta null? " + (itemMeta == null ? "&a&lYes" : "&c&lNo"));
        Damageable meta = (Damageable) (itemMeta == null ? Bukkit.getItemFactory().getItemMeta(Material.TRIDENT) : itemMeta);
        if(meta != null) {
            int resultDurability = meta.getDamage() + (isGliding ? config.getInt("DurabilityCost.Flying") : config.getInt("DurabilityCost.Normal"));
            locale.debug("&7After reducing the durability value is: &c&l" + resultDurability);

            if(resultDurability < 250) {
                meta.setDamage(resultDurability);
                trident.setItemMeta((ItemMeta) meta);
                sendItem(userInv, trident, isMainHand);
                locale.debug("&7Trident has been returned to the player after reducing the durability.");
            } else {
                sendItem(userInv, new ItemStack(Material.AIR), isMainHand);
                locale.debug("&7Trident has been broken.");
            }
        } else {
            locale.debug("&c&lERROR! &7After validating, ItemMeta still was null!");
        }
    }

    @EventHandler
    public void onGliding(EntityToggleGlideEvent event) {
        if(event.getEntity() instanceof Player) {
            locale.debug("&7EntityToggleGlideEvent activated: (&aPlayer&7) &c" + event.getEntity().getName());
            locale.debug("&7Is player gliding with Elytra? " + (event.isGliding() ? "&a&lYes" : "&c&lNo"));
            glidingPlayers.put((Player) event.getEntity(), event.isGliding());
            locale.debug("&7" + event.getEntity().getName() + "'s gliding status has been logged.");
        }
    }

    private void sendItem(PlayerInventory userInv, ItemStack item, boolean isMainHand) {
        if(isMainHand) {
            userInv.setItemInMainHand(item);
        } else {
            userInv.setItemInOffHand(item);
        }
    }

    private void setCost(CommandSender sender, int cost, boolean isFlying) {
        if(isFlying) {
            config.set("DurabilityCost.Flying", cost);
        } else {
            config.set("DurabilityCost.Normal", cost);
        }
        saveConfig();
        if(cost < 250) {
            sender.sendMessage(
                    locale.getMessage(localeKey, MessageType.INFO, "Command", "Set.Success")
                            .replace("%mode%", isFlying ? "Elytra Flying Mode" : "Normal Mode")
                            .replace("%value%", String.valueOf(cost))
            );
        } else {
            sender.sendMessage(
                    locale.getMessage(localeKey, MessageType.WARN, "Command", "Set.ReachMaxDurability")
                            .replace("%mode%", isFlying ? "Elytra Flying Mode" : "Normal Mode")
                            .replace("%value%", String.valueOf(cost))
            );
        }
    }
}
