package net.shoal.sir.voteup.config;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.VoteChoice;
import net.shoal.sir.voteup.enums.*;
import net.shoal.sir.voteup.task.VoteEndTask;
import net.shoal.sir.voteup.util.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VoteManager {

    private static LocaleUtil locale;
    private static VoteManager instance;
    public static VoteManager getInstance() {
        if(instance == null) {
            instance = new VoteManager();
        }
        locale = VoteUp.getInstance().getLocale();
        return instance;
    }

    private File dataFolder = new File(VoteUp.getInstance().getDataFolder() + File.separator + "Votes");

    private Map<String, Vote> voteMap = new HashMap<>();
    private Map<String, Map<VoteDataType, Object>> creatingVoteMap = new HashMap<>();
    private Map<String, BukkitRunnable> endTaskMap = new HashMap<>();

    public void load() {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
            Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未找到投票记录文件夹, 已自动生成."));
        } else {
            File[] votes = dataFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));

            if(votes != null && votes.length > 0) {
                for(File dataFile : votes) {
                    FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

                    List<String> desc = new ArrayList<>();
                    for(String text : data.getStringList("Description")) {
                        desc.add(CommonUtil.color(text));
                    }

                    Map<ChoiceType, Map<String, String>> participant = new HashMap<>();
                    if(data.isConfigurationSection("Participant")) {
                        Map<String, String> choiceParticipant = new HashMap<>();
                        for(String choice : data.getConfigurationSection("Participant").getKeys(false)) {
                            ChoiceType choiceType = ChoiceType.valueOf(choice.toUpperCase());
                            ConfigurationSection choiceSection = data.getConfigurationSection("Participant." + choice);
                            for(String playerName : choiceSection.getKeys(false)) {
                                choiceParticipant = new HashMap<>();
                                choiceParticipant.put(playerName, CommonUtil.color(Objects.requireNonNull(choiceSection.getString(playerName))));
                            }
                            participant.put(choiceType, choiceParticipant);
                        }
                    }

                    voteMap.put(
                            CommonUtil.getNoExFileName(dataFile.getName()),
                            new Vote(
                                    CommonUtil.getNoExFileName(dataFile.getName()),
                                    data.getBoolean("Status"),
                                    VoteType.valueOf(Objects.requireNonNull(data.getString("Type")).toUpperCase()),
                                    data.getInt("Amount"),
                                    CommonUtil.color(Objects.requireNonNull(data.getString("Title"))),
                                    desc,
                                    new VoteChoice(data.getConfigurationSection("Choice")),
                                    data.getString("Starter"),
                                    data.getLong("StartTime"),
                                    data.getString("Duration"),
                                    (participant == null ? new HashMap<>() : participant),
                                    data.getStringList("AutoCast"),
                                    CommonUtil.color(data.getString("Display.Pass")),
                                    CommonUtil.color(data.getString("Display.Reject"))
                            )
                    );
                }

                voteMap.forEach((s, vote) -> {
                    startCountdown(s);
                });
                Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7共加载 &c" + voteMap.size() + " &7项投票记录."));
            } else {
                Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7没有投票纪录可供加载."));
            }
        }
    }

    private void startCountdown(String id) {
        locale.debug("&7调用 startCountdown &7方法, 目标投票ID: &c" + id);
        if(voteMap.containsKey(id)) {
            locale.debug("&7已加载目标投票数据.");
            Vote data = voteMap.get(id);
            locale.debug("&7目标投票数据: &c" + data.toString());
            if(data.isStatus()) {
                locale.debug("&7目标投票进行中.");
                if(!endTaskMap.containsKey(id)) {
                    locale.debug("&7该投票ID下未有已记录倒计时任务.");
                    long remaining = data.getStartTime() + TimeUtil.getDurationTimeStamp(data.getDuration()) - System.currentTimeMillis();
                    locale.debug("&7计算得出剩余时间: &c" + remaining + "&7ms");
                    if(remaining > 0) {
                        locale.debug("&7剩余时间大于0.");
                        BukkitRunnable endTask = new VoteEndTask(id);
                        endTask.runTaskLater(VoteUp.getInstance(), (remaining / 1000) * 20);
                        locale.debug("&7已启动倒计时任务: &c" + (remaining / 1000) * 20 + "&7tick(s)");
                        endTaskMap.put(id, endTask);
                    } else {
                        voteEnd(id);
                    }
                }
            }
        }
    }

    public Vote getVote(String id) {
        if(voteMap.containsKey(id)) {
            return voteMap.get(id);
        }
        return null;
    }

    public Vote startCreateVote(String starter) {
        Map<VoteDataType, Object> newVote = new HashMap<>();
        String id = starter + "." + (voteMap.size() + 1);

        newVote.put(VoteDataType.ID, id);
        newVote.put(VoteDataType.STATUS, true);
        newVote.put(VoteDataType.TYPE, VoteType.NORMAL);
        newVote.put(VoteDataType.AMOUNT, 0);
        newVote.put(VoteDataType.TITLE, starter + "的投票");
        newVote.put(VoteDataType.DESCRIPTION, new ArrayList<>());
        newVote.put(VoteDataType.CHOICE, new VoteChoice((ConfigurationSection) null));
        newVote.put(VoteDataType.STARTER, starter);
        newVote.put(VoteDataType.STARTTIME, System.currentTimeMillis());
        newVote.put(VoteDataType.DURATION, "1d");
        newVote.put(VoteDataType.PARTICIPANT, new HashMap<>());
        newVote.put(VoteDataType.AUTOCAST, new ArrayList<>());
        newVote.put(VoteDataType.PASS, CommonUtil.color( "&a&L已通过"));
        newVote.put(VoteDataType.REJECT, CommonUtil.color("&c&l未通过"));

        creatingVoteMap.put(starter, newVote);
        return new Vote(newVote);
    }

    public boolean setCreatingVoteData(String playerName, VoteDataType type, Object value) {
        locale.debug("&7调用 setCreatingVoteData 方法, 目标玩家: &c" + playerName);
        if(creatingVoteMap.containsKey(playerName)) {
            locale.debug("&7目标玩家存在待发布的投票.");
            Map<VoteDataType, Object> voteData = creatingVoteMap.get(playerName);
            voteData.put(type, value);
            locale.debug("&7已覆盖值: &c" + type.toString() + " &9-> " + value);
            creatingVoteMap.put(playerName, voteData);
            CommonUtil.message(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7设置" + type.getName() + "成功: &c" + value.toString()), playerName);
            SoundManager.getInstance().success(playerName);
            return true;
        }
        CommonUtil.message(locale.buildMessage(VoteUp.LOCALE, MessageType.ERROR, "&7设置" + type.getName() + "失败, 您的 ID 下没有待发布的投票."), playerName);
        SoundManager.getInstance().fail(playerName);
        locale.debug("&7设置值失败, 目标玩家不存在待发布的投票, 操作无效.");
        return false;
    }

    public Vote getCreatingVote(String playerName) {
        if(creatingVoteMap.containsKey(playerName)) {
            return new Vote(creatingVoteMap.get(playerName));
        }
        return null;
    }

    public void finishVoteCreating(String playerName) {
        if(creatingVoteMap.containsKey(playerName)) {
            Map<VoteDataType, Object> data = creatingVoteMap.get(playerName);
            data.put(VoteDataType.STARTTIME, System.currentTimeMillis());
            save(data);
            startCountdown((String) data.get(VoteDataType.ID));
            Vote vote = voteMap.get(data.get(VoteDataType.ID));
            SoundManager.getInstance().voteEvent(true);
            CommonUtil.broadcastTitle(
                    "",
                    PlaceholderUtil.check(
                            locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "Start.Subtitle"), vote
                    )
            );
            String msg = locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "Start.Broadcast");
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.spigot().sendMessage(ChatAPIUtil.buildClickText(
                        PlaceholderUtil.check(msg, vote),
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + vote.getId()),
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color("&a点我立即查看投票")))
                ));
            });
            creatingVoteMap.remove(playerName);
        }
    }

    public void vote(String voteID, Player user, String reason, ChoiceType type) {
        locale.debug("&7调用 vote 方法.");
        locale.debug("&7目标投票 ID: &c" + voteID);
        locale.debug("&7操作人: &c" + user.getName());
        locale.debug("&7投票原因: &c" + reason);
        locale.debug("&7选项类型: &c" + type.toString());
        if(voteMap.containsKey(voteID)) {
            locale.debug("&7目标投票已加载.");
            Vote vote = getVote(voteID);
            if(vote.isStatus()) {
                locale.debug("&7目标投票进行中.");
                Map<ChoiceType, Map<String, String>> participant = vote.getParticipant();
                Map<String, String> choiceMap = (participant.get(type) == null ? new HashMap<>() : participant.get(type));
                if(!choiceMap.containsKey(user.getName())) {
                    locale.debug("&7未有先前投票纪录.");
                    choiceMap.put(user.getName(), reason);
                    participant.put(type, choiceMap);
                    vote.setParticipant(participant);
                    save(vote.data());
                    CommonUtil.message(locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "Vote." + type.toString()), user.getName());
                } else {
                    locale.debug("已有先前投票纪录. 操作无效.");
                    CommonUtil.message(locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "Vote.Fail.Logged"), user.getName());
                }
            } else {
                locale.debug("&7目标投票已关闭. 操作无效.");
                CommonUtil.message(locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "Vote.Fail.Closed"), user.getName());
            }
        }
    }

    public void voteEnd(String voteID) {
        if(voteMap.containsKey(voteID)) {
            Vote endVote = voteMap.get(voteID);
            if(endVote.isStatus()) {
                endVote.setStatus(false);
                save(endVote.data());
                if(isPassed(voteID)) {
                    for(String cmd : endVote.getAutoCast()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }
                }
                SoundManager.getInstance().voteEvent(false);
                CommonUtil.broadcastTitle(
                        "",
                        PlaceholderUtil.check(
                                locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "End.Subtitle"), endVote
                        )
                );
                Bukkit.getOnlinePlayers().forEach(player -> {
                    player.sendMessage(PlaceholderUtil.check(locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "End.Broadcast"), endVote));
                });
                if(PermissionUtil.hasPermission("VoteUp.admin").isEmpty()) {
                    CacheManager.getInstance().log(CacheLogType.VOTE_END, endVote.getId());
                }
            }
        }
    }

    public boolean isPassed(String voteID) {
        if(voteMap.containsKey(voteID)) {
            Vote vote = voteMap.get(voteID);
            if(!vote.isStatus()) {
                Map<ChoiceType, Map<String, String>> participants = vote.getParticipant();
                if(participants != null && !participants.isEmpty()) {
                    switch(vote.getType()) {
                        case NORMAL:
                            Map<String, String> accept = (participants.get(ChoiceType.ACCEPT) == null ? new HashMap<>() : participants.get(ChoiceType.ACCEPT));
                            Map<String, String> refuse = (participants.get(ChoiceType.REFUSE) == null ? new HashMap<>() : participants.get(ChoiceType.REFUSE));
                            if(!accept.isEmpty()) {
                                return accept.size() > refuse.size();
                            } else {
                                return false;
                            }
                        case REACHAMOUNT:
                            return participants.get(ChoiceType.ACCEPT).size() >= vote.getAmount();
                        default:
                            return false;
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public void save(Map<VoteDataType, Object> data) {
        String id = (String) data.get(VoteDataType.ID);
        voteMap.put(id, new Vote(data));
        creatingVoteMap.remove(id);
        saveToFile(data);
    }

    public void saveToFile(Map<VoteDataType, Object> data) {
        String id = (String) data.get(VoteDataType.ID);
        File dataFile = new File(dataFolder.getAbsolutePath() + File.separator + id + ".yml");
        FileConfiguration voteData = YamlConfiguration.loadConfiguration(dataFile);

        voteData.set("Status", data.get(VoteDataType.STATUS));
        voteData.set("Type", data.get(VoteDataType.TYPE).toString());
        voteData.set("Amount", data.get(VoteDataType.AMOUNT));
        voteData.set("Title", ((String) data.get(VoteDataType.TITLE)).replace("§", "&"));

        List<String> desc = (List<String>) data.get(VoteDataType.DESCRIPTION);
        desc.replaceAll(s -> s.replace("§", "&"));
        voteData.set("Description", desc);

        Map<ChoiceType, String> choices = ((VoteChoice) data.get(VoteDataType.CHOICE)).getChoices();
        for(ChoiceType key : choices.keySet()) {
            voteData.set("Choice." + key.toString(), choices.get(key).replace("§", "&"));
        }

        voteData.set("Starter", data.get(VoteDataType.STARTER));
        voteData.set("StartTime", data.get(VoteDataType.STARTTIME));
        voteData.set("Duration", data.get(VoteDataType.DURATION));

        Map<ChoiceType, Map<String, String>> participant = (Map<ChoiceType, Map<String, String>>) data.get(VoteDataType.PARTICIPANT);
        ConfigurationSection participantSection = (voteData.getConfigurationSection("Participant") != null ? voteData.getConfigurationSection("Participant") : voteData.createSection("Participant"));
        participant.forEach((type, stringStringMap) -> {
            ConfigurationSection choice = (participantSection.getConfigurationSection(type.toString()) != null ? participantSection.getConfigurationSection(type.toString()) : participantSection.createSection(type.toString()));
            stringStringMap.forEach(choice::set);
        });

        List<String> casts = (List<String>) data.get(VoteDataType.AUTOCAST);
        casts.replaceAll(s -> s.replace("§", "&"));
        voteData.set("AutoCast", casts);

        voteData.set("Display.Pass", ((String) data.get(VoteDataType.PASS)).replace("§", "&"));
        voteData.set("Display.Reject", ((String) data.get(VoteDataType.REJECT)).replace("§", "&"));

        try {
            voteData.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
