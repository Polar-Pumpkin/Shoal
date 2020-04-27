package net.shoal.sir.voteup.data;

import lombok.Getter;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.PData;
import org.serverct.parrot.parrotx.data.PID;
import org.serverct.parrot.parrotx.data.flags.Owned;
import org.serverct.parrot.parrotx.data.flags.Timestamp;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Vote implements PData, Owned, Timestamp {

    public String voteID;
    public boolean status;
    public Type type;
    public int goal;
    public UUID owner;
    public long startTime;
    public String duration;

    public String title;
    public List<String> description;
    public Map<Choice, String> choices;
    public List<String> autocast;
    public Map<Result, String> results;

    public Map<Choice, Map<UUID, String>> participant;

    private final PPlugin plugin;
    private PID id;
    private File file;

    public Vote(@NonNull PID id, @NonNull File file) {
        this.plugin = id.getPlugin();
        this.id = id;
        this.file = file;
        load(file);
    }

    @Override
    public String getTypeName() {
        return "投票数据文件/" + getFileName();
    }

    @Override
    public String getFileName() {
        return BasicUtil.getNoExFileName(this.file.getName());
    }

    @Override
    public void init() {
        plugin.lang.logAction(I18n.EXECUTE, "init() 方法 -> " + getTypeName());
    }

    @Override
    public void saveDefault() {
        plugin.lang.logAction(I18n.EXECUTE, "saveDefault() 方法 -> " + getTypeName());
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void setFile(@NonNull File file) {
        this.file = file;
    }

    @Override
    public void load(@NonNull File file) {
        try {
            FileConfiguration data = YamlConfiguration.loadConfiguration(file);
            this.voteID = getFileName();

            ConfigurationSection information = data.getConfigurationSection("Information");
            this.status = information.getBoolean("Status", false);
            this.type = Type.valueOf(information.getString("Type", "NORMAL").toUpperCase());
            this.goal = information.getInt("Goal", Integer.MAX_VALUE);
            this.owner = UUID.fromString(information.getString("Owner"));
            this.startTime = information.getLong("Timestamp");
            this.duration = information.getString("Duration");

            ConfigurationSection setting = data.getConfigurationSection("Settings");
            this.title = I18n.color(setting.getString("Title", "&7" + getOwnerName() + " 的投票"));
            this.description = setting.getStringList("Description");
            this.description.replaceAll(I18n::color);

            ConfigurationSection choiceSection = setting.getConfigurationSection("Choices");
            this.choices = new HashMap<>();
            for (String choiceKey : choiceSection.getKeys(false)) {
                Choice choice = EnumUtil.valueOf(Choice.class, choiceKey.toUpperCase());
                this.choices.put(choice, I18n.color(choiceSection.getString(choiceKey)));
            }

            this.autocast = setting.getStringList("Autocast");

            ConfigurationSection resultSection = setting.getConfigurationSection("Results");
            this.results = new HashMap<>();
            for (String resultKey : resultSection.getKeys(false)) {
                Result result = EnumUtil.valueOf(Result.class, resultKey.toUpperCase());
                this.results.put(result, I18n.color(choiceSection.getString(resultKey)));
            }

            ConfigurationSection participantSection = data.getConfigurationSection("Participants");
            this.participant = new HashMap<>();
            for (String choiceKey : participantSection.getKeys(false)) {
                Choice choice = EnumUtil.valueOf(Choice.class, choiceKey.toUpperCase());
                Map<UUID, String> reasons = this.participant.getOrDefault(choice, new HashMap<>());
                ConfigurationSection section = participantSection.getConfigurationSection(choiceKey);
                for (String uuid : section.getKeys(false)) {
                    reasons.put(UUID.fromString(uuid), I18n.color(section.getString(uuid)));
                }
            }

        } catch (Throwable e) {
            plugin.lang.logError(I18n.LOAD, getTypeName(), e, null);
        }
    }

    @Override
    public void reload() {
        plugin.lang.logAction(I18n.RELOAD, getTypeName());
        load(file);
    }

    @Override
    public void save() {
        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection information = data.createSection("Information");
        information.set("Status", status);
        information.set("Type", type.name());
        information.set("Goal", goal);
        information.set("Owner", owner.toString());
        information.set("Timestamp", startTime);
        information.set("Duration", duration);

        ConfigurationSection setting = data.createSection("Settings");
        setting.set("Title", I18n.deColor(title, '&'));
        List<String> desc2save = new ArrayList<>(description);
        desc2save.replaceAll(s -> I18n.deColor(s, '&'));
        setting.set("Description", desc2save);
        this.choices.forEach(
                (choice, s) -> setting.set("Choices." + choice.name(), I18n.deColor(s, '&'))
        );
        setting.set("Autocast", autocast);
        this.results.forEach(
                (result, s) -> setting.set("Results." + result.name(), I18n.deColor(s, '&'))
        );

        this.participant.forEach(
                (choice, reasons) -> reasons.forEach(
                        (uuid, s) -> data.set("Participants." + choice.name() + "." + uuid.toString(), I18n.deColor(s, '&'))
                )
        );

        try {
            data.save(file);
        } catch (IOException e) {
            plugin.lang.logError(I18n.SAVE, getTypeName(), e, null);
        }
    }

    @Override
    public PID getID() {
        return id;
    }

    @Override
    public void setID(@NonNull PID pid) {
        this.id = pid;
    }

    @Override
    public UUID getOwner() {
        return owner;
    }

    @Override
    public void setOwner(UUID uuid) {
        this.owner = uuid;
    }

    @Override
    public long getTimestamp() {
        return startTime;
    }

    @Override
    public void setTime(long time) {
        this.startTime = time;
    }

    public enum Type {
        NORMAL(0, "同意人数大于反对人数"),
        REACHAMOUNT(1, "同意人数需达到指定数量"),
        LEASTNOT(2, "反对人数不超过指定数量");

        @Getter
        private final String desc;
        @Getter
        private final int mode;

        Type(int mode, String type) {
            this.desc = type;
            this.mode = mode;
        }

        public Type mode(int mode) {
            switch (mode) {
                case 0:
                default:
                    return NORMAL;
                case 1:
                    return REACHAMOUNT;
                case 2:
                    return LEASTNOT;
            }
        }
    }

    public enum Choice {
        ACCEPT("同意"),
        NEUTRAL("中立"),
        REFUSE("反对");

        @Getter
        private final String name;

        Choice(String name) {
            this.name = name;
        }
    }

    public enum Result {
        PASS("通过"),
        REJECT("未通过"),
        DRAW("平票"),
        CANCEL("被取消");

        @Getter
        private final String name;

        Result(String name) {
            this.name = name;
        }
    }

    public enum Duration {
        DAY("D", 86400),
        HOUR("H", 3600),
        MINUTE("M", 60);

        @Getter
        private final String name;
        @Getter
        private final int time;

        Duration(String name, int time) {
            this.name = name;
            this.time = time;
        }
    }
}
