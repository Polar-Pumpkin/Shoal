package net.shoal.parrot.egg4everyone;

import org.bukkit.NamespacedKey;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.config.PConfig;

public final class Egg4everyone extends PPlugin {

    public static NamespacedKey KEY;

    @Override
    protected void preload() {
        KEY = new NamespacedKey(this, "Tag");

        pConfig = new PConfig(this, "config", "主配置文件") {
        };
    }

    @Override
    protected void load() {

    }

}
