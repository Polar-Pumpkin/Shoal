package net.shoal.sir.voteup.config;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VoteUpConfig extends PConfig {
    public VoteUpConfig() {
        super(VoteUp.getInstance(), "config", "主配置文件");
    }

    public boolean debug;
    public boolean bStats;
    public String language;

    public boolean sound_enable;
    public String action_start;
    public String action_success;
    public String action_failure;
    public String vote_start;
    public String vote_end;

    public int setting_participantLeast;
    public boolean allow_anonymous;
    public boolean allow_public;
    public boolean allow_edit_participant;
    public boolean allow_edit_vote;

    public boolean title_start;
    public boolean title_end;
    public int title_fadeIn;
    public int title_stay;
    public int title_fadeOut;

    public boolean autocast_enable;
    public boolean autocast_userMode;
    public boolean autocast_blackList;
    public List<String> autocast_list;

    public List<String> admins;

    @Override
    public void load(@NonNull File file) {
        try {
            Class<? extends VoteUpConfig> configClass = this.getClass();
            for (Path key : Path.values()) {
                Field field = configClass.getField(key.fieldName);
                field.setAccessible(true);
                field.set(this, config.get(key.path));
            }
        } catch (Throwable e) {
            plugin.lang.logError(I18n.LOAD, getTypeName(), e, null);
        }
    }

    @Override
    public void save() {
        try {
            Class<? extends VoteUpConfig> configClass = this.getClass();
            for (Path key : Path.values()) {
                Field field = configClass.getField(key.fieldName);
                field.setAccessible(true);
                config.set(key.path, field.get(this));
            }
        } catch (Throwable e) {
            plugin.lang.logError(I18n.SAVE, getTypeName(), e, null);
        }
        super.save();
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
        DEBUG("调试模式", "Debug", DataType.BOOLEAN, "debug"),
        BSTATS("bStats 数据统计", "bStats", DataType.BOOLEAN, "bStats"),
        LANGUAGE("语言", "Language", DataType.STRING, "language"),
        SOUND_ENABLE("提示音", "Sound.Enable", DataType.BOOLEAN, "sound_enable"),
        SOUND_ACTION_START("常规提示音", "Sound.Action.Start", DataType.SOUND, "action_start"),
        SOUND_ACTION_SUCCESS("成功提示音", "Sound.Action.Success", DataType.SOUND, "action_success"),
        SOUND_ACTION_FAILURE("失败提示音", "Sound.Action.Failure", DataType.SOUND, "action_failure"),
        SOUND_VOTE_START("投票开始提示音", "Sound.Vote.Start", DataType.SOUND, "vote_start"),
        SOUND_VOTE_END("投票结束提示音", "Sound.Vote.End", DataType.SOUND, "vote_end"),
        AUTOCAST_ENABLE("自动执行功能", "Autocast.Enable", DataType.BOOLEAN, "autocast_enable"),
        AUTOCAST_USERMODE("玩家权限模式", "Autocast.Usermode", DataType.BOOLEAN, "autocast_userMode"),
        AUTOCAST_BLACKLIST("黑名单模式", "Autocast.Blacklist", DataType.BOOLEAN, "autocast_blackList"),
        AUTOCAST_LIST("关键词列表", "Autocast.List", DataType.LIST, "autocast_list"),
        ADMIN("管理员", "Admin", DataType.LIST, "admins"),
        SETTINGS_PARTICIPANT_LEAST("投票参与者数量最低要求", "Settings.ParticipantLeast", DataType.INT, "setting_participantLeast"),
        SETTINGS_BROADCAST_TITLE_VOTESTART("投票开始时 Title 通知", "Settings.Broadcast.Title.VoteStart", DataType.BOOLEAN, "title_start"),
        SETTINGS_BROADCAST_TITLE_VOTEEND("投票结束时 Title 通知", "Settings.Broadcast.Title.VoteEnd", DataType.BOOLEAN, "title_end"),
        SETTINGS_BROADCAST_TITLE_FADEIN("Title 淡入时长", "Settings.Broadcast.Title.FadeIn", DataType.INT, "title_fadeIn"),
        SETTINGS_BROADCAST_TITLE_STAY("Title 停留时长", "Settings.Broadcast.Title.Stay", DataType.INT, "title_stay"),
        SETTINGS_BROADCAST_TITLE_FADEOUT("Title 淡出时长", "Settings.Broadcast.Title.FadeOut", DataType.INT, "title_fadeOut"),
        SETTINGS_ALLOW_ANONYMOUS("匿名投票功能", "Settings.Allow.Anonymous", DataType.BOOLEAN, "allow_anonymous"),
        SETTINGS_ALLOW_PUBLIC("公开进度功能", "Settings.Allow.Public", DataType.BOOLEAN, "allow_public"),
        SETTINGS_ALLOW_EDIT_VOTE("投票编辑功能", "Settings.Allow.Edit.Vote", DataType.BOOLEAN, "allow_edit_vote"),
        SETTINGS_ALLOW_EDIT_PARTICIPANT("参与者编辑功能", "Settings.Allow.Edit.Participant", DataType.BOOLEAN, "allow_edit_participant"),
        ;

        public final String name;
        public final String path;
        public final DataType dataType;
        public final String fieldName;

        Path(String name, String path, DataType type, String fieldName) {
            this.name = name;
            this.path = path;
            this.dataType = type;
            this.fieldName = fieldName;
        }
    }

    public List<UUID> admins() {
        List<UUID> admins = new ArrayList<>();
        this.admins.forEach(uuid -> admins.add(UUID.fromString(uuid)));
        return admins;
    }
}
