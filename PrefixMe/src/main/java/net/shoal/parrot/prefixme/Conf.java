package net.shoal.parrot.prefixme;

import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.ItemUtil;

import java.util.Objects;

public class Conf extends PConfig {

    public static ItemStack prefixItem;
    public static Sound prefixSound;
    public static Sound deprefixSound;
    public static ItemStack deprefixItem;
    public static int limit;
    private static Conf inst;

    public Conf() {
        super(PrefixMe.getInst(), "config", "主配置文件");
    }

    public static Conf getInst() {
        if (Objects.isNull(inst)) {
            inst = new Conf();
        }
        return inst;
    }

    @Override
    public void load() {
        limit = config.getInt("CharacterLimit", Integer.MAX_VALUE);
        final ConfigurationSection item = config.getConfigurationSection("Item");
        if (Objects.isNull(item)) {
            return;
        }
        final ConfigurationSection prefix = item.getConfigurationSection("Prefix");
        if (Objects.nonNull(prefix)) {
            Conf.prefixItem = ItemUtil.build(plugin, prefix);
            String prefixSound = prefix.getString("Sound");
            if (Objects.isNull(prefixSound)) {
                prefixSound = "ENTITY_VILLAGER_WORK_CARTOGRAPHER";
            }
            Conf.prefixSound = EnumUtil.valueOf(Sound.class, prefixSound.toUpperCase());
        }
        final ConfigurationSection dePrefix = item.getConfigurationSection("Deprefix");
        if (Objects.nonNull(dePrefix)) {
            Conf.deprefixItem = ItemUtil.build(plugin, dePrefix);
            String deprefixSound = dePrefix.getString("Sound");
            if (Objects.isNull(deprefixSound)) {
                deprefixSound = "ENTITY_ITEM_BREAK";
            }
            Conf.deprefixSound = EnumUtil.valueOf(Sound.class, deprefixSound.toUpperCase());
        }
    }
}
