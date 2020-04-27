package net.shoal.sir.voteup.config;

import lombok.Getter;
import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import org.bukkit.Sound;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.data.annotations.PConfigBoolean;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigManager extends PConfig {

    @PConfigBoolean(path = "Sound.Enable", def = true)
    public static boolean SOUND_ENABLE;
    public static Sound SOUND_ACTION_START;
    public static Sound SOUND_ACTION_SUCCESS;
    public static Sound SOUND_ACTION_FAILURE;
    public static Sound SOUND_VOTE_START;
    public static Sound SOUND_VOTE_END;

    @PConfigBoolean(path = "Autocast.Enable", def = true)
    public static boolean AUTOCAST_ENABLE;
    @PConfigBoolean(path = "Autocast.Blacklist", def = true)
    public static boolean AUTOCAST_BLACKLIST;
    public static List<String> AUTOCAST_LIST;

    public static List<UUID> ADMIN;

    public ConfigManager() {
        super(VoteUp.getInstance(), "config", "主配置文件");
    }

    @Override
    public void saveDefault() {
        this.plugin.saveDefaultConfig();
    }

    @Override
    public void load(@NonNull File file) {
        SOUND_ACTION_START = EnumUtil.valueOf(Sound.class, this.config.getString(Path.SOUND_ACTION_START.path, "ENTITY_ARROW_HIT_PLAYER").toUpperCase());
        SOUND_ACTION_SUCCESS = EnumUtil.valueOf(Sound.class, this.config.getString(Path.SOUND_ACTION_SUCCESS.path, "ENTITY_VILLAGER_YES").toUpperCase());
        SOUND_ACTION_FAILURE = EnumUtil.valueOf(Sound.class, this.config.getString(Path.SOUND_ACTION_FAILURE.path, "BLOCK_ANVIL_DESTROY").toUpperCase());
        SOUND_VOTE_START = EnumUtil.valueOf(Sound.class, this.config.getString(Path.SOUND_VOTE_START.path, "ENTITY_ARROW_HIT_PLAYER").toUpperCase());
        SOUND_VOTE_END = EnumUtil.valueOf(Sound.class, this.config.getString(Path.SOUND_VOTE_END.path, "BLOCK_ANVIL_HIT").toUpperCase());

        AUTOCAST_LIST = this.config.getStringList(Path.AUTOCAST_LIST.path);

        ADMIN = new ArrayList<>();
        try {
            this.config.getStringList(Path.ADMIN.path).forEach(s -> ADMIN.add(UUID.fromString(s)));
        } catch (Throwable e) {
            plugin.lang.logError(I18n.LOAD, getTypeName(), e, null);
        }
    }

    @Override
    public void save() {
        this.config.set(Path.SOUND_ENABLE.path, SOUND_ENABLE);
        this.config.set(Path.SOUND_ACTION_START.path, SOUND_ACTION_START.name());
        this.config.set(Path.SOUND_ACTION_SUCCESS.path, SOUND_ACTION_SUCCESS.name());
        this.config.set(Path.SOUND_ACTION_FAILURE.path, SOUND_ACTION_FAILURE.name());
        this.config.set(Path.SOUND_VOTE_START.path, SOUND_VOTE_START.name());
        this.config.set(Path.SOUND_VOTE_END.path, SOUND_VOTE_END.name());

        this.config.set(Path.AUTOCAST_ENABLE.path, AUTOCAST_ENABLE);
        this.config.set(Path.AUTOCAST_BLACKLIST.path, AUTOCAST_BLACKLIST);
        this.config.set(Path.AUTOCAST_LIST.path, AUTOCAST_LIST);

        List<String> admins = new ArrayList<>();
        ADMIN.forEach(s -> admins.add(s.toString()));
        this.config.set(Path.ADMIN.path, admins);

        super.save();
    }

    public enum Path {
        SOUND_ENABLE("Sound.Enable"),
        SOUND_ACTION_START("Sound.Action.Start"),
        SOUND_ACTION_SUCCESS("Sound.Action.Success"),
        SOUND_ACTION_FAILURE("Sound.Action.Failure"),
        SOUND_VOTE_START("Sound.Vote.Start"),
        SOUND_VOTE_END("Sound.Vote.End"),
        AUTOCAST_ENABLE("Autocast.Enable"),
        AUTOCAST_BLACKLIST("Autocast.Blacklist"),
        AUTOCAST_LIST("Autocast.List"),
        ADMIN("Admin"),
        ;

        @Getter
        private final String path;

        Path(String path) {
            this.path = path;
        }
    }
}
