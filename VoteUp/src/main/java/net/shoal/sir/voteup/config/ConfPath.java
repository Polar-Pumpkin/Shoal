package net.shoal.sir.voteup.config;

import net.shoal.sir.voteup.VoteUp;
import org.serverct.parrot.parrotx.config.PConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfPath extends PConfig {
    public ConfPath() {
        super(VoteUp.getInstance(), "config", "主配置文件");
    }

    @Override
    public void saveDefault() {
        this.plugin.saveDefaultConfig();
    }

    // TODO 可 GUI 编辑的配置文件。
    // TODO Weight 加权系统。

    public enum DataType {
        STRING("字符串"),
        INT("整数"),
        BOOLEAN("布尔值"),
        LIST("列表"),
        SOUND("SpigotAPI 声音(Sound)枚举");

        public final String name;

        DataType(String name) {
            this.name = name;
        }
    }

    public enum Path {
        BSTATS("bStats", DataType.BOOLEAN),
        SOUND_ENABLE("Sound.Enable", DataType.BOOLEAN),
        SOUND_ACTION_START("Sound.Action.Start", DataType.SOUND),
        SOUND_ACTION_SUCCESS("Sound.Action.Success", DataType.SOUND),
        SOUND_ACTION_FAILURE("Sound.Action.Failure", DataType.SOUND),
        SOUND_VOTE_START("Sound.Vote.Start", DataType.SOUND),
        SOUND_VOTE_END("Sound.Vote.End", DataType.SOUND),
        AUTOCAST_ENABLE("Autocast.Enable", DataType.BOOLEAN),
        AUTOCAST_USERMODE("Autocast.Usermode", DataType.BOOLEAN),
        AUTOCAST_BLACKLIST("Autocast.Blacklist", DataType.BOOLEAN),
        AUTOCAST_LIST("Autocast.List", DataType.LIST),
        ADMIN("Admin", DataType.LIST),
        SETTINGS_PARTICIPANT_LEAST("Settings.ParticipantLeast", DataType.INT),
        SETTINGS_BROADCAST_TITLE_VOTESTART("Settings.Broadcast.Title.VoteStart", DataType.BOOLEAN),
        SETTINGS_BROADCAST_TITLE_VOTEEND("Settings.Broadcast.Title.VoteEnd", DataType.BOOLEAN),
        SETTINGS_BROADCAST_TITLE_FADEIN("Settings.Broadcast.Title.FadeIn", DataType.INT),
        SETTINGS_BROADCAST_TITLE_STAY("Settings.Broadcast.Title.Stay", DataType.INT),
        SETTINGS_BROADCAST_TITLE_FADEOUT("Settings.Broadcast.Title.FadeOut", DataType.INT),
        SETTINGS_ALLOW_ANONYMOUS("Settings.Allow.Anonymous", DataType.BOOLEAN),
        SETTINGS_ALLOW_PUBLIC("Settings.Allow.Public", DataType.BOOLEAN),
        SETTINGS_ALLOW_EDIT_VOTE("Settings.Allow.Edit.Vote", DataType.BOOLEAN),
        SETTINGS_ALLOW_EDIT_PARTICIPANT("Settings.Allow.Edit.Participant", DataType.BOOLEAN),
        ;

        public final String path;
        public final DataType dataType;

        Path(String path, DataType type) {
            this.path = path;
            this.dataType = type;
        }
    }

    public List<UUID> admins() {
        List<UUID> admins = new ArrayList<>();
        config.getStringList(Path.ADMIN.path).forEach(uuid -> admins.add(UUID.fromString(uuid)));
        return admins;
    }
}
