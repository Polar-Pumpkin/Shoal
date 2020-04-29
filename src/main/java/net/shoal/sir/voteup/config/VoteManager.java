package net.shoal.sir.voteup.config;

import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.VoteChoice;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import net.shoal.sir.voteup.enums.CacheLogType;
import net.shoal.sir.voteup.enums.GuiConfiguration;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.task.VoteEndTask;
import net.shoal.sir.voteup.util.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.config.PFolder;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class VoteManager extends PFolder {

    private final Map<String, Vote> voteMap = new HashMap<>();
    private final Map<UUID, Vote> creatingVoteMap = new HashMap<>();
    private final Map<String, Integer> endTaskMap = new HashMap<>();

    public VoteManager() {
        super(VoteUp.getInstance(), "Votes", "投票数据文件夹");
    }

    @Override
    public void init() {
        super.init();
        voteMap.forEach((s, vote) -> startCountdown(s));
    }

    @Override
    public void load(@NonNull File file) {
        voteMap.put(BasicUtil.getNoExFileName(file.getName()), new Vote(file));
    }

    private void startCountdown(String voteID) {
        Vote data = voteMap.getOrDefault(voteID, null);
        if (data == null) return;
        if (!data.status) return;
        if (endTaskMap.containsKey(voteID)) return;
        long timeRemain = data.startTime + TimeUtil.getDurationTimeStamp(data.duration) - System.currentTimeMillis();
        if (timeRemain <= 0) {
            voteEnd(voteID);
            return;
        }
        BukkitRunnable endTask = new VoteEndTask(voteID);
        endTask.runTaskLater(plugin, (timeRemain / 1000) * 20);
        endTaskMap.put(voteID, endTask.getTaskId());
    }

    public Vote getVote(String id) {
        return voteMap.getOrDefault(id, null);
    }

    public Vote startCreateVote(UUID uuid) {
        Vote vote = new Vote(plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_PASSLEAST_AGREE.path, 3), uuid, "1d");
        creatingVoteMap.put(uuid, vote);
        return vote;
    }

    public boolean setCreatingVoteData(@NonNull Player user, Vote.Data dataType, Object value) {
        Vote vote = creatingVoteMap.getOrDefault(user.getUniqueId(), null);
        if (vote == null) {
            BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.ERROR, String.format(I18n.color(BuiltinMsg.VOTE_VALUE_CHANGE_FAIL.msg), dataType.name)));
            VoteUpAPI.SOUND.fail(user);
            return false;
        }
        vote.set(dataType, value);
        BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(BuiltinMsg.VOTE_VALUE_CHANGE_SUCCESS.msg), dataType.name)));
        VoteUpAPI.SOUND.success(user);
        return true;
    }

    public Vote getCreatingVote(UUID uuid) {
        return creatingVoteMap.getOrDefault(uuid, null);
    }

    public void backCreating(@NonNull Player user) {
        BasicUtil.openInventory(
                plugin,
                user,
                InventoryUtil.parsePlaceholder(
                        GuiManager.getInstance().getMenu(GuiConfiguration.CREATE_MENU.getName()),
                        getCreatingVote(user.getUniqueId()),
                        user
                )
        );
    }

    public void finishVoteCreating(@NonNull Player user) {
        Vote vote = getCreatingVote(user.getUniqueId());
        if (vote == null) return;
        startCountdown(vote.voteID);
        vote.startTime = System.currentTimeMillis();
        creatingVoteMap.remove(user.getUniqueId());
        voteMap.put(vote.voteID, vote);

        VoteUpAPI.SOUND.voteEvent(true);
        BasicUtil.broadcastTitle(
                "",
                PlaceholderUtil.check(
                        plugin.lang.getRaw(plugin.localeKey, "Vote", "Start.Subtitle"), vote
                ),
                plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
        );
        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatAPIUtil.buildClickText(
                PlaceholderUtil.check(plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Start.Broadcast"), vote),
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + vote.voteID),
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color("&a点击立即查看投票详细信息")))
        )));
    }

    public void vote(String voteID, @NonNull Player user, String reason, Vote.Choice choice) {
        Vote vote = getVote(voteID);
        if (vote == null) return;
            if (!vote.status) {
                I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Closed"));
                return;
            }
                Map<Vote.Choice, Map<UUID, String>> participant = vote.participant;
                Map<UUID, String> choiceMap = participant.getOrDefault(choice, new HashMap<>());
                if (user.hasPermission(VoteUpPerm.valueOf("VOTE_" + choice.toString()).perm())) {
                    if (!choiceMap.containsKey(user.getName())) {
                        choiceMap.put(user.getName(), user.hasPermission(VoteUpPerm.VOTE_REASON.perm()) ? reason : VOTE_REASON_NOPERM);
                        participant.put(choice, choiceMap);
                        vote.setParticipant(participant);
                        save(vote.data());
                        CommonUtil.message(plugin.lang.getMessage(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote." + choice.toString()), user.getName());

                        Player starter = Bukkit.getPlayerExact(voteID.split("_")[0]);
                        if (starter != null && starter.isOnline()) {
                            String noticeMsg = plugin.lang.getMessage(plugin.localeKey, I18n.Type.INFO, "Vote", "Voted.Starter")
                                    .replace("%Voter%", user.getName())
                                    .replace("%Choice%", vote.getChoices().getChoice(VoteManager.getInstance().getChoice(voteID, user.getName())))
                                    .replace("%Reason%", CommonUtil.color(reason));
                            CommonUtil.message(PlaceholderUtil.check(noticeMsg, vote), user.getName());
                        } else {
                            CacheManager.getInstance().log(CacheLogType.VOTE_VOTED, voteID, user.getName());
                        }

                        Bukkit.getOnlinePlayers().forEach(player -> {
                            if (player.hasPermission(VoteUpPerm.NOTICE.perm())) {
                                String noticeMsg = plugin.lang.getMessage(plugin.localeKey, I18n.Type.INFO, "Vote", "Voted.Noticer")
                                        .replace("%Voter%", user.getName())
                                        .replace("%Choice%", vote.getChoices().getChoice(VoteManager.getInstance().getChoice(voteID, user.getName())))
                                        .replace("%Reason%", CommonUtil.color(reason));
                                CommonUtil.message(PlaceholderUtil.check(noticeMsg, vote), player.getName());
                            }
                        });
                    } else {
                        CommonUtil.message(plugin.lang.getMessage(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Logged"), user.getName());
                    }
                } else {
                    user.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7您没有权限这么做."));
                }

    public void voteEnd(String voteID) {
        if (voteMap.containsKey(voteID)) {
            Vote endVote = voteMap.get(voteID);
            if (endVote.isStatus()) {
                endVote.setStatus(false);
                save(endVote.data());
                if (isPassed(voteID)) {
                    for (String cmd : endVote.getAutoCast()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    }
                }
                SoundManager.getInstance().voteEvent(false);
                CommonUtil.broadcastTitle(
                        "",
                        PlaceholderUtil.check(
                                plugin.lang.getRawMessage(plugin.localeKey, "Vote", "End.Subtitle"), endVote
                        )
                );
                Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(PlaceholderUtil.check(plugin.lang.getMessage(plugin.localeKey, I18n.Type.INFO, "Vote", "End.Broadcast"), endVote)));
                if (PermissionUtil.hasPermission(VoteUpPerm.NOTICE.perm()).isEmpty()) {
                    CacheManager.getInstance().log(CacheLogType.VOTE_END, endVote.getId(), "");
                }
            }
        }
    }

    public boolean isPassed(String voteID) {
        if (voteMap.containsKey(voteID)) {
            Vote vote = voteMap.get(voteID);
            if (!vote.isStatus()) {
                Map<ChoiceType, Map<String, String>> participants = vote.getParticipant();
                if (participants != null && !participants.isEmpty()) {
                    Map<String, String> accept = (participants.get(ChoiceType.ACCEPT) == null ? new HashMap<>() : participants.get(ChoiceType.ACCEPT));
                    Map<String, String> refuse = (participants.get(ChoiceType.REFUSE) == null ? new HashMap<>() : participants.get(ChoiceType.REFUSE));
                    switch (vote.getType()) {
                        case NORMAL:
                            if (!accept.isEmpty()) {
                                return accept.size() > refuse.size();
                            } else {
                                return false;
                            }
                        case REACHAMOUNT:
                            return accept.size() >= vote.getAmount();
                        case LEASTNOT:
                            return refuse.size() <= vote.getAmount();
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

    public boolean isVoted(String voteID, String playerName) {
        if (voteMap.containsKey(voteID)) {
            Vote data = voteMap.get(voteID);
            for (Map<String, String> participantsMap : data.getParticipant().values()) {
                if (participantsMap.containsKey(playerName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUploadReason(String voteID, String playerName) {
        if (voteMap.containsKey(voteID)) {
            Vote data = voteMap.get(voteID);
            for (Map<String, String> participantsMap : data.getParticipant().values()) {
                if (participantsMap.containsKey(playerName)) {
                    String reason = participantsMap.get(playerName);
                    if (!VOTE_REASON_NOPERM.equalsIgnoreCase(reason) && !VOTE_REASON_UNUPLOADED.equalsIgnoreCase(reason)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String getReason(String voteID, String playerName) {
        if (voteMap.containsKey(voteID)) {
            Vote data = voteMap.get(voteID);
            for (Map<String, String> participantsMap : data.getParticipant().values()) {
                if (participantsMap.containsKey(playerName)) {
                    return CommonUtil.color(participantsMap.get(playerName));
                }
            }
        }
        return CommonUtil.color("&7&o未获取到目标数据.");
    }

    public ChoiceType getChoice(String voteID, String playerName) {
        if (voteMap.containsKey(voteID)) {
            Vote data = voteMap.get(voteID);
            Map<ChoiceType, Map<String, String>> participants = data.getParticipant();
            for (ChoiceType type : participants.keySet()) {
                if (participants.get(type).containsKey(playerName)) {
                    return type;
                }
            }
        }
        return null;
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
        for (ChoiceType key : choices.keySet()) {
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
