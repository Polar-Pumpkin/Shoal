package net.shoal.sir.voteup.config;

import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import net.shoal.sir.voteup.enums.CacheLogType;
import net.shoal.sir.voteup.enums.GuiConfiguration;
import net.shoal.sir.voteup.task.VoteEndTask;
import net.shoal.sir.voteup.util.ChatAPIUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.config.PFolder;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.io.File;
import java.util.HashMap;
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
            endVote(voteID);
            return;
        }
        BukkitRunnable endTask = new VoteEndTask(voteID);
        endTask.runTaskLater(plugin, (timeRemain / 1000) * 20);
        endTaskMap.put(voteID, endTask.getTaskId());
    }

    public Vote getVote(String id) {
        return voteMap.getOrDefault(id, null);
    }

    public Vote create(UUID uuid) {
        Vote vote = new Vote(plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_PASSLEAST_AGREE.path, 3), uuid, "1d");
        creatingVoteMap.put(uuid, vote);
        return vote;
    }

    public boolean setVoteData(@NonNull Player user, Vote.Data dataType, Object value) {
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

    public void back(@NonNull Player user) {
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

    public void start(@NonNull Player user) {
        Vote vote = getCreatingVote(user.getUniqueId());
        if (vote == null) return;
        startCountdown(vote.voteID);
        vote.startTime = System.currentTimeMillis();
        creatingVoteMap.remove(user.getUniqueId());
        voteMap.put(vote.voteID, vote);

        VoteUpAPI.SOUND.voteEvent(true);
        BasicUtil.broadcastTitle(
                "",
                VoteUpPlaceholder.check(
                        plugin.lang.getRaw(plugin.localeKey, "Vote", "Start.Subtitle"), vote
                ),
                plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
        );
        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(ChatAPIUtil.buildClickText(
                VoteUpPlaceholder.check(plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Start.Broadcast"), vote),
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + vote.voteID),
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(BuiltinMsg.VOTE_CLICK.msg)))
        )));
    }

    public void vote(String voteID, @NonNull Player user, Vote.Choice choice, String reason) {
        UUID uuid = user.getUniqueId();
        Vote vote = getVote(voteID);
        if (vote == null) return;
        if (!vote.status) {
            I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Closed"));
            return;
        }
        if (!EnumUtil.valueOf(VoteUpPerm.class, "VOTE_" + choice.name()).hasPermission(user)) return;

        Map<Vote.Choice, Map<UUID, String>> participant = vote.participants;
        Map<UUID, String> choiceMap = participant.getOrDefault(choice, new HashMap<>());

        if (choiceMap.containsKey(uuid))
            I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Logged"));

        choiceMap.put(uuid, VoteUpPerm.REASON.hasPermission(user) ? (reason.length() == 0 ? BuiltinMsg.REASON_NOT_YET.msg : reason) : BuiltinMsg.REASON_NO_PERM.msg);
        participant.put(choice, choiceMap);
        vote.participants = participant;
        vote.save();

        I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote." + choice.name()));

        Player starter = Bukkit.getPlayer(vote.owner);
        if (starter != null && starter.isOnline()) {
            String noticeMsg = plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Voted.Starter")
                    .replace("%Voter%", user.getName())
                    .replace("%Choice%", I18n.color(vote.choices.getOrDefault(choice, BuiltinMsg.ERROR_GET_CHOICE.msg)))
                    .replace("%Reason%", I18n.color(reason));
            I18n.send(user, VoteUpPlaceholder.check(noticeMsg, vote));
        } else CacheManager.getInstance().log(CacheLogType.VOTE_VOTED, voteID, user.getName());

        // TODO 管理员不在线时的提醒挂起规则
        plugin.pConfig.getConfig().getStringList(ConfigManager.Path.ADMIN.path).forEach(
                adminID -> {
                    Player admin = Bukkit.getPlayer(UUID.fromString(adminID));
                    if (admin != null) {
                        String noticeMsg = plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Voted.Noticer")
                                .replace("%Voter%", user.getName())
                                .replace("%Choice%", I18n.color(vote.choices.getOrDefault(choice, BuiltinMsg.ERROR_GET_CHOICE.msg)))
                                .replace("%Reason%", I18n.color(reason));
                        I18n.send(admin, VoteUpPlaceholder.check(noticeMsg, vote));
                    }
                }
        );
    }

    public void endVote(String voteID) {
        Vote vote = getVote(voteID);
        if (vote == null) return;
        if (vote.status) {
            vote.status = false;
            vote.save();

            if (vote.isPassed()) vote.autocast.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            VoteUpAPI.SOUND.voteEvent(false);
            BasicUtil.broadcastTitle(
                    "",
                    VoteUpPlaceholder.check(plugin.lang.getRaw(plugin.localeKey, "Vote", "End.Subtitle"), vote),
                    plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                    plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                    plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
            );
            BasicUtil.broadcast(VoteUpPlaceholder.check(plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "End.Broadcast"), vote));
            // TODO 管理员不在线时的提醒挂起规则
            CacheManager.getInstance().log(CacheLogType.VOTE_END, voteID, "");
        }
    }
}
