package net.shoal.parrot.prefixme;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PrefixManager extends PConfig {

    private static PrefixManager inst;
    @Getter
    private final Map<UUID, String> prefixMap = new HashMap<>();

    public PrefixManager() {
        super(PrefixMe.getInst(), "Prefixes", "玩家称号数据文件");
    }

    public static PrefixManager getInst() {
        if (Objects.isNull(inst)) {
            inst = new PrefixManager();
        }
        return inst;
    }

    @Override
    public void load() {
        final ConfigurationSection section = config.getConfigurationSection("Prefixes");
        if (Objects.isNull(section)) {
            return;
        }
        for (String key : section.getKeys(false)) {
            final UUID uuid = UUID.fromString(key);
            this.prefixMap.put(uuid, section.getString(key));
        }
    }

    @Override
    public void save() {
        final ConfigurationSection section = config.createSection("Prefixes");
        this.prefixMap.forEach((uuid, prefix) -> section.set(uuid.toString(), prefix));
        try {
            config.save(file);
        } catch (IOException e) {
            lang.log.error(I18n.SAVE, name(), e, "shoal");
        }
    }

    public void set(final UUID uuid, final String content) {
        lang.log.debug("玩家 " + Bukkit.getOfflinePlayer(uuid).getName() + " 尝试修改称号: " + content);
        lang.log.debug("称号长度: " + content.length());
        lang.log.debug("限制长度: " + Conf.limit);
        this.prefixMap.put(uuid, content);
    }

    public String get(final UUID uuid) {
        return I18n.color(this.prefixMap.getOrDefault(uuid, ""));
    }
}
