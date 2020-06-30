package net.shoal.sir.voteup.data;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.config.ConfPath;
import net.shoal.sir.voteup.enums.Msg;
import org.bukkit.ChatColor;
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
import java.util.function.Predicate;

public class Vote implements PData, Owned, Timestamp {

    private final PPlugin plugin;
    public String voteID;
    public boolean open;
    public boolean cancelled;
    public boolean isDraft;
    public boolean allowAnonymous;
    public boolean isPublic;
    public boolean allowEdit; // TODO 允许投票后编辑
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
    public List<Participant> participants;
    private PID id;
    private File file;

    public Vote(@NonNull File file) {
        this.plugin = VoteUp.getInstance();
        this.file = file;
        load(file);

        this.id = new PID(plugin, "VOTE_" + voteID);
    }

    public Vote(int goal, UUID owner, String duration) {
        this.voteID = UUID.randomUUID().toString();
        this.plugin = VoteUp.getInstance();
        this.id = new PID(plugin, "VOTE_" + voteID);
        this.file = new File(VoteUpAPI.VOTE_MANAGER.getFolder(), voteID + ".yml");

        this.open = false;
        this.cancelled = false;
        this.isDraft = true;
        this.allowAnonymous = false;
        this.isPublic = false;
        this.allowEdit = false;
        this.type = Type.NORMAL;
        this.goal = goal;
        this.owner = owner;
        this.startTime = System.currentTimeMillis();
        this.duration = duration;
        init();
    }

    public static long getDurationTimestamp(String duration) {
        long result = 0;

        String clone = duration;
        Duration durationType;
        while ((durationType = getFirstIndexOf(clone)) != null) {
            int index = clone.indexOf(durationType.code);
            try {
                String target = clone.substring(0, index);
                int amount = Integer.parseInt(target);
                result += amount * durationType.time;
                clone = clone.substring(index + 1);
            } catch (Throwable e) {
                break;
            }
        }

        return result * 1000;
    }

    public static Duration getFirstIndexOf(String target) {
        int index = Integer.MAX_VALUE;
        Duration durationType = null;
        for (Duration type : Duration.values()) {
            int currentIndex = target.indexOf(type.code);
            if (currentIndex == -1) continue;
            if (currentIndex < index) {
                index = currentIndex;
                durationType = type;
            }
        }
        return durationType;
    }

    public Participant getParticipant(UUID uuid) {
        for (Participant participant : participants) if (participant.uuid == uuid) return participant;
        return null;
    }

    public List<Participant> listParticipants(Predicate<Participant> filter) {
        List<Participant> result = new ArrayList<>();
        this.participants.forEach(
                participant -> {
                    if (filter.test(participant))
                        result.add(participant);
                }
        );
        return result;
    }

    public boolean isPassed() {
        int all = participants.size();
        int accept = listParticipants(user -> user.choice == Choice.ACCEPT).size();
        int refuse = listParticipants(user -> user.choice == Choice.REFUSE).size();

        if (all < plugin.pConfig.getConfig().getInt(ConfPath.Path.SETTINGS_PARTICIPANT_LEAST.path, 5))
            return false;

        switch (type) {
            case NORMAL:
                return accept > refuse;
            case REACHAMOUNT:
                return accept >= goal;
            case LEASTNOT:
                return refuse <= goal;
            default:
                return false;
        }
    }

    public Result result() {
        if (cancelled) return Result.CANCEL;

        if (type == Type.NORMAL)
            if (listParticipants(user -> user.choice == Choice.ACCEPT).size() == listParticipants(user -> user.choice == Choice.REFUSE).size())
                return Result.DRAW;

        if (isPassed()) return Result.PASS;
        else return Result.REJECT;
    }

    public boolean isVoted(UUID uuid) {
        return getParticipant(uuid) != null;
    }

    public boolean hasReason(UUID uuid) {
        Participant user = getParticipant(uuid);
        if (user != null)
            return !user.reason.equalsIgnoreCase(Msg.REASON_NOT_YET.msg) && !user.reason.equalsIgnoreCase(Msg.REASON_NO_PERM.msg);
        return false;
    }

    public int getProcess() {
        int all = participants.size();
        int accept = listParticipants(user -> user.choice == Choice.ACCEPT).size();
        int refuse = listParticipants(user -> user.choice == Choice.REFUSE).size();
        int least = plugin.pConfig.getConfig().getInt(ConfPath.Path.SETTINGS_PARTICIPANT_LEAST.path, 5);

        int rate;
        if (all >= least) {
            switch (type) {
                case NORMAL:
                    rate = (int) ((accept / (double) all) * 100);
                    break;
                case REACHAMOUNT:
                    rate = (int) ((accept / (double) goal) * 100);
                    break;
                case LEASTNOT:
                    rate = (int) ((Math.max(goal - refuse, 0) / (double) goal) * 100);
                    break;
                default:
                    rate = 0;
                    break;
            }
        } else rate = (int) ((all / (double) least) * 100);

        return rate;
    }

    public UserStatus getUserStatus(UUID uuid) {
        if (!open) return UserStatus.DONE;
        boolean isVoted = isVoted(uuid);
        boolean hasReason = hasReason(uuid);
        if (isVoted && hasReason) return UserStatus.DONE;
        else if (isVoted) return UserStatus.NO_REASON;
        else return UserStatus.FIRST;
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
        this.title = getOwnerName() + " 的投票";
        this.description = new ArrayList<>(Collections.singletonList(Msg.NO_DESCRIPTION.msg));
        this.choices = new HashMap<Choice, String>() {
            {
                put(Choice.ACCEPT, ChatColor.GREEN + Choice.ACCEPT.name);
                put(Choice.NEUTRAL, ChatColor.YELLOW + Choice.NEUTRAL.name);
                put(Choice.REFUSE, ChatColor.RED + Choice.REFUSE.name);
            }
        };
        this.autocast = new ArrayList<>();
        this.results = new HashMap<Result, String>() {
            {
                put(Result.PASS, ChatColor.GREEN + Result.PASS.name);
                put(Result.DRAW, ChatColor.YELLOW + Result.DRAW.name);
                put(Result.REJECT, ChatColor.RED + Result.REJECT.name);
                put(Result.CANCEL, ChatColor.RED + Result.CANCEL.name);
            }
        };

        this.participants = new ArrayList<>();
    }

    @Override
    public void saveDefault() {
        init();
        save();
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
            if (information == null) return;
            this.open = information.getBoolean("Open", true);
            this.cancelled = information.getBoolean("Cancelled", false);
            this.isDraft = information.getBoolean("Draft", false);
            this.type = Type.valueOf(information.getString("Type", "NORMAL").toUpperCase());
            this.goal = information.getInt("Goal", Integer.MAX_VALUE);
            this.owner = UUID.fromString(information.getString("Owner"));
            this.startTime = information.getLong("Timestamp");
            this.duration = information.getString("Duration");

            ConfigurationSection setting = data.getConfigurationSection("Settings");
            if (setting != null) {
                this.title = I18n.color(setting.getString("Title", getOwnerName() + " 的投票"));
                this.description = setting.getStringList("Description");
                this.description.replaceAll(I18n::color);

                this.allowAnonymous = setting.getBoolean("Anonymous", false);
                this.isPublic = setting.getBoolean("Public", false);
                this.allowEdit = setting.getBoolean("Editable", false);
            }

            ConfigurationSection choiceSection = setting.getConfigurationSection("Choices");
            this.choices = new HashMap<>();
            if (choiceSection != null) {
                for (String choiceKey : choiceSection.getKeys(false)) {
                    Choice choice = EnumUtil.valueOf(Choice.class, choiceKey.toUpperCase());
                    this.choices.put(choice, I18n.color(choiceSection.getString(choiceKey)));
                }
            }


            this.autocast = setting.getStringList("Autocast");

            ConfigurationSection resultSection = setting.getConfigurationSection("Results");
            this.results = new HashMap<>();
            if (resultSection != null) {
                for (String resultKey : resultSection.getKeys(false)) {
                    Result result = EnumUtil.valueOf(Result.class, resultKey.toUpperCase());
                    this.results.put(result, I18n.color(resultSection.getString(resultKey)));
                }
            }

            ConfigurationSection participantSection = data.getConfigurationSection("Participants");
            this.participants = new ArrayList<>();
            if (participantSection != null) {
                for (String uuid : participantSection.getKeys(false)) {

                    // 旧版本数据文件的转换
                    Choice choice = EnumUtil.valueOf(Choice.class, uuid.toUpperCase());
                    if (choice != null) {
                        ConfigurationSection targetOldSection = participantSection.getConfigurationSection(uuid);
                        for (String oldUUID : targetOldSection.getKeys(false))
                            this.participants.add(new Participant(UUID.fromString(oldUUID), choice, false, targetOldSection.getString(oldUUID)));
                        continue;
                    }

                    ConfigurationSection userSection = participantSection.getConfigurationSection(uuid);
                    if (userSection == null) continue;
                    this.participants.add(new Participant(userSection));
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
        if (!BasicUtil.getNoExFileName(file.getName()).equals(voteID))
            this.file = new File(VoteUpAPI.VOTE_MANAGER.getFolder(), voteID + ".yml");

        FileConfiguration data = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection information = data.createSection("Information");
        information.set("Open", open);
        information.set("Cancelled", cancelled);
        information.set("Draft", isDraft);
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
        setting.set("Anonymous", allowAnonymous);
        setting.set("Public", isPublic);
        setting.set("Editable", allowEdit);

        this.choices.forEach(
                (choice, s) -> setting.set("Choices." + choice.name(), I18n.deColor(s, '&'))
        );
        setting.set("Autocast", autocast);
        this.results.forEach(
                (result, s) -> setting.set("Results." + result.name(), I18n.deColor(s, '&'))
        );

        ConfigurationSection participantSection = data.createSection("Participants");
        this.participants.forEach(participant -> participant.save(participantSection));

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
        NORMAL(0, "普通投票", "同意人数大于反对人数"),
        REACHAMOUNT(1, "多数同意投票", "同意人数需达到指定数量"),
        LEASTNOT(2, "否决投票", "反对人数不超过指定数量");

        public final String desc;
        public final String name;
        public final int mode;

        Type(int mode, String name, String type) {
            this.desc = type;
            this.name = name;
            this.mode = mode;
        }

        public static Type mode(int mode) {
            if (mode > 2) mode = 0;
            if (mode < 0) mode = 2;
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

        public final String name;

        Choice(String name) {
            this.name = name;
        }
    }

    public enum Result {
        PASS("通过"),
        REJECT("未通过"),
        DRAW("平票"),
        CANCEL("被取消");

        public final String name;

        Result(String name) {
            this.name = name;
        }
    }

    public enum Duration {
        // yyyy-MM-dd HH:mm:ss
        DAY('d', "天", 86400),
        HOUR('H', "时", 3600),
        MINUTE('m', "分", 60),
        SECOND('s', "秒", 1);

        public final char code;
        public final String name;
        public final int time;

        Duration(char code, String name, int time) {
            this.code = code;
            this.name = name;
            this.time = time;
        }
    }

    public enum UserStatus {
        FIRST,
        NO_REASON,
        DONE
    }

    public enum Data {
        ID("投票ID"),
        OPEN("进行状态"),
        CANCELLED("取消状态"),
        DRAFT("草稿状态"),
        ANONYMOUS("允许匿名投票"),
        PUBLIC("公开投票进度"),
        EDITABLE("可编辑所投票"),
        TYPE("投票类型"),
        GOAL("目标人数"),
        OWNER("发起者"),
        STARTTIME("发起时间"),
        DURATION("持续时间"),
        TITLE("投票标题"),
        DESCRIPTION("投票简述"),
        CHOICE("投票选项"),
        AUTOCAST("自动执行内容"),
        RESULT("投票结果"),
        PARTICIPANT("参加者"),
        PROCESS("投票进度");

        public final String name;

        Data(String type) {
            this.name = type;
        }
    }

    public static class Participant implements Timestamp {
        public final UUID uuid;
        public final Choice choice;
        public final boolean anonymous;
        public final String reason;
        public long timestamp;

        public Participant(UUID uuid, Choice choice, boolean anonymous, String reason) {
            this.uuid = uuid;
            this.choice = choice;
            this.anonymous = anonymous;
            this.reason = reason;
            this.timestamp = System.currentTimeMillis();
        }

        public Participant(@NonNull ConfigurationSection section) {
            this.uuid = UUID.fromString(section.getName());
            this.choice = EnumUtil.valueOf(Choice.class, section.getString("Choice"));
            this.anonymous = section.getBoolean("Anonymous", false);
            this.reason = section.getString("Reason", Msg.REASON_NOT_YET.msg);
            this.timestamp = section.getLong("Timestamp");
        }

        public void save(ConfigurationSection participantSection) {
            ConfigurationSection userSection = participantSection.createSection(uuid.toString());
            userSection.set("Choice", choice.name());
            userSection.set("Anonymous", anonymous);
            userSection.set("Reason", reason);
            userSection.set("Timestamp", timestamp);
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public void setTime(long l) {
            this.timestamp = l;
        }
    }
}
