package net.shoal.sir.limitedriptide.utils;

import lombok.Getter;
import lombok.Setter;
import net.shoal.sir.limitedriptide.enums.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocaleUtil {

    @Getter @Setter private String defaultLocaleKey = "Chinese";
    private Plugin plugin;
    private File dataFolder;
    @Getter private Map<String, FileConfiguration> locales = new HashMap<>();

    public LocaleUtil(Plugin plugin) {
        this.plugin = plugin;
        dataFolder = new File(plugin.getDataFolder() + File.separator + "Locales");
        init();
    }

    public void init() {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
            plugin.saveResource("Locales/" + defaultLocaleKey + ".yml", false);
            Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7[&b&lEP's &aLocale Tool&7] &e&l> &7Language folder not found, has been automatically generated."));
        } else {
            if(dataFolder.listFiles() == null || dataFolder.listFiles().length == 0) {
                plugin.saveResource("Locales/" + defaultLocaleKey + ".yml", false);
            }
        }
        load();
    }

    private void load() {
        File[] localeDataFiles = dataFolder.listFiles(pathname -> {
            String fileName = pathname.getName();
            return fileName.endsWith(".yml");
        });

        if(localeDataFiles != null) {
            for(File dataFile : localeDataFiles) {
                locales.put(CommonUtil.getNoExFileName(dataFile.getName()), YamlConfiguration.loadConfiguration(dataFile));
            }
        }
        Bukkit.getLogger().info(ChatColor.translateAlternateColorCodes('&', "&7[&b&lEP's &aLocale Tool&7] &a&l> &7Loading language data successful, " + locales.size() + " locale(s) loaded in total."));
    }

    public void debug(String message) {
        if(plugin.getConfig().getBoolean("Debug")) {
            Bukkit.getLogger().info(buildMessage(null, MessageType.DEBUG, message));
        }
    }

    public String getMessage(String key, MessageType type, String section, String path) {
        FileConfiguration data = locales.containsKey(key) ? locales.get(key) : locales.get(defaultLocaleKey);

        if(data.getKeys(false).contains(section)) {
            String message = data.getConfigurationSection(section).getString(path);
            if(message != null && !message.equalsIgnoreCase("")) {
                return buildMessage(key, type, message);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', "&c&lERROR&7(Encountered language information encountered an error, please contact the administrator to resolve.)");
    }

    public String buildMessage(String key, MessageType type, String message) {
        FileConfiguration data = locales.containsKey(key) ? locales.get(key) : locales.get(defaultLocaleKey);
        String pluginPrefix = type != MessageType.DEBUG ? data.getString("Plugin.Prefix") : "&9[&d" + plugin.getName() + "&9]&7(&d&lDEBUG&7) ";
        String typePrefix = type != MessageType.DEBUG ? data.getString("Plugin." + type.toString()) : "&d&l>> ";

        return ChatColor.translateAlternateColorCodes('&', pluginPrefix + typePrefix + message);
    }

    public List<String> getHelpMessage(String key) {
        if(locales.containsKey(key)) {
            return locales.get(key).getStringList("Plugin.HelpMessage");
        }
        return locales.get(defaultLocaleKey).getStringList("Plugin.HelpMessage");
    }
}
