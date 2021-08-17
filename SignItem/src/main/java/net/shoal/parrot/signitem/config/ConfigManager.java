package net.shoal.parrot.signitem.config;

import net.shoal.parrot.signitem.SignItem;
import org.bukkit.Sound;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.data.autoload.AutoLoadItem;

import java.util.List;

public class ConfigManager extends PConfig {

    public static List<String> identifies;
    public static String timeFormat;
    public static int descLength;
    public static Sound success;
    public static Sound failure;
    public static Sound erasing;

    public ConfigManager() {
        super(SignItem.getInstance(), "config", "主配置文件");
        autoLoad(AutoLoadItem.builder()
                        .path("Settings.Identify")
                        .type(AutoLoadItem.DataType.LIST)
                        .field("identifies")
                        .build(),
                AutoLoadItem.builder()
                        .path("Settings.DescriptionLength")
                        .type(AutoLoadItem.DataType.INT)
                        .field("descLength")
                        .build(),
                AutoLoadItem.builder()
                        .path("Settings.TimeFormat")
                        .type(AutoLoadItem.DataType.STRING)
                        .field("timeFormat")
                        .build(),
                AutoLoadItem.builder()
                        .path("Sound.Success")
                        .type(AutoLoadItem.DataType.SOUND)
                        .field("success")
                        .build(),
                AutoLoadItem.builder()
                        .path("Sound.Failure")
                        .type(AutoLoadItem.DataType.SOUND)
                        .field("failure")
                        .build(),
                AutoLoadItem.builder()
                        .path("Sound.Erasing")
                        .type(AutoLoadItem.DataType.SOUND)
                        .field("erasing")
                        .build()
        );
    }


}
