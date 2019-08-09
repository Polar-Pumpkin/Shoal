package net.shoal.sir.limitedriptide.utils;

import lombok.Getter;
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
            plugin.saveResource("Locales/English.yml", false);
            Bukkit.getLogger().info("&e&l> &7Language folder not found, has been automatically generated.");
        } else {
            File[] localeDataFiles = dataFolder.listFiles(pathname -> {
                String fileName = pathname.getName();
                return fileName.endsWith(".yml");
            });

            for(File dataFile : localeDataFiles) {
                locales.put(CommonUtil.getNoExFileName(dataFile.getName()), YamlConfiguration.loadConfiguration(dataFile));
            }
            Bukkit.getLogger().info("&a&l> &7Loading language folder successful, " + localeDataFiles.length + " locale(s) loaded in total.");
        }
    }

    public void debug(String message) {
        if(plugin.getConfig().getBoolean("Debug")) {
            Bukkit.getLogger().info(buildMessage(null, MessageType.DEBUG, message));
        }
    }

    public String getMessage(String key, MessageType type, String section, String path) {
        FileConfiguration data = locales.containsKey(key) ? locales.get(key) : locales.get("English");

        if(data.getKeys(false).contains(section)) {
            String message = data.getConfigurationSection(section).getString(path);
            if(message != null && !message.equalsIgnoreCase("")) {
                return buildMessage(key, type, message);
            }
        }

        return ChatColor.translateAlternateColorCodes('&', "&c&lERROR&7(Encountered language information encountered an error, please contact the administrator to resolve.)");
    }

    public String buildMessage(String key, MessageType type, String message) {
        FileConfiguration data = locales.containsKey(key) ? locales.get(key) : locales.get("English");
        String pluginPrefix = type != MessageType.DEBUG ? data.getString("Plugin.Prefix") : "&9[&d" + plugin.getName() + "&9]&7(&d&lDEBUG&7) ";
        String typePrefix = type != MessageType.DEBUG ? data.getString("Plugin." + type.toString()) : "&d&l>> ";

        return ChatColor.translateAlternateColorCodes('&', pluginPrefix + typePrefix + message);
    }

    public List<String> getHelpMessage(String key) {
        if(locales.containsKey(key)) {
            return locales.get(key).getStringList("Plugin.HelpMessage");
        }
        return locales.get("English").getStringList("Plugin.HelpMessage");
    }
}
