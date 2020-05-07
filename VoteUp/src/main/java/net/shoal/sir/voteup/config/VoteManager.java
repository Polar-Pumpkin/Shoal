package net.shoal.sir.voteup.config;

import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.data.Notice;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.inventory.CreateInventoryHolder;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import net.shoal.sir.voteup.task.VoteEndTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.config.PFolder;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteManager extends PFolder {

    private final Map<String, Vote> voteMap = new HashMap<>();
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
        if (!data.open) return;
        if (endTaskMap.containsKey(voteID)) return;
        long timeRemain = data.startTime + Vote.getDurationTimestamp(data.duration) - System.currentTimeMillis();
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
        Vote vote = new Vote(plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_PARTICIPANT_LEAST.path, 3), uuid, "1d");
        voteMap.put(vote.voteID, vote);
        return vote;
    }

    public boolean setVoteData(String voteID, @NonNull Player editor, Vote.Data dataType, Object value) {
        Vote vote = getVote(voteID);
        if (vote == null) {
            BasicUtil.send(plugin, editor, plugin.lang.build(plugin.localeKey, I18n.Type.ERROR, String.format(I18n.color(BuiltinMsg.VOTE_EDIT_FAIL.msg), dataType.name)));
            VoteUpAPI.SOUND.fail(editor);
            return false;
        }
        vote.set(dataType, value);
        BasicUtil.send(plugin, editor, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(BuiltinMsg.VOTE_EDIT_SUCCESS.msg), dataType.name)));
        VoteUpAPI.SOUND.success(editor);
        return true;
    }

    public Vote draftVote(UUID uuid) {
        for (Map.Entry<String, Vote> entry : voteMap.entrySet()) {
            Vote vote = entry.getValue();
            if (vote.isOwner(uuid) && vote.isDraft) return vote;
        }
        return create(uuid);
    }

    public void back(@NonNull Player user) {
        BasicUtil.openInventory(plugin, user, new CreateInventoryHolder<>(draftVote(user.getUniqueId()), user).getInventory());
    }

    public void start(@NonNull Player user) {
        Vote vote = draftVote(user.getUniqueId());
        if (vote == null) return;
        startCountdown(vote.voteID);
        vote.startTime = System.currentTimeMillis();
        vote.open = true;
        vote.isDraft = false;

        VoteUpAPI.SOUND.voteEvent(true);
        if (plugin.pConfig.getConfig().getBoolean(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_VOTESTART.path, true))
            BasicUtil.broadcastTitle(
                    "",
                    VoteUpPlaceholder.parse(vote, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.Start.Subtitle")),
                    plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                    plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                    plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
            );
        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(JsonChatUtil.buildClickText(
                VoteUpPlaceholder.parse(vote, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Event.Start.Broadcast")),
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + vote.voteID),
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(BuiltinMsg.VOTE_CLICK.msg)))
        )));
    }

    public void vote(String voteID, @NonNull Player user, Vote.Choice choice, String reason) {
        UUID uuid = user.getUniqueId();
        Vote vote = getVote(voteID);
        if (vote == null) return;
        if (!vote.open) {
            I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Closed"));
            return;
        }
        if (!VoteUpPerm.VOTE.hasPermission(user, choice)) return;

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
        Notice notice = VoteUpAPI.CACHE_MANAGER.log(Notice.Type.VOTE, voteID, new HashMap<String, Object>() {
            {
                put("Voter", user.getName());
                put("Choice", choice.name());
                put("Reason", reason);
            }
        });

        if (starter != null && starter.isOnline()) {
            String announce = notice.announce(user.getUniqueId());
            if (announce != null)
                I18n.send(starter, VoteUpPlaceholder.parse(vote, announce));
        }

        plugin.pConfig.getConfig().getStringList(ConfigManager.Path.ADMIN.path).forEach(
                adminID -> {
                    Player admin = Bukkit.getPlayer(UUID.fromString(adminID));
                    if (admin != null) {
                        String announce = notice.announce(admin.getUniqueId());
                        if (announce != null)
                            I18n.send(admin, VoteUpPlaceholder.parse(vote, announce));
                    }
                }
        );
    }

    public void endVote(String voteID) {
        Vote vote = getVote(voteID);
        if (vote == null) return;
        if (vote.open) {
            vote.open = false;
            vote.save();

            if (vote.isPassed()) vote.autocast.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            VoteUpAPI.SOUND.voteEvent(false);
            if (plugin.pConfig.getConfig().getBoolean(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_VOTEEND.path, false))
                BasicUtil.broadcastTitle(
                        "",
                        VoteUpPlaceholder.parse(vote, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.End.Subtitle")),
                        plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEIN.path, 5),
                        plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_STAY.path, 10),
                        plugin.pConfig.getConfig().getInt(ConfigManager.Path.SETTINGS_BROADCAST_TITLE_FADEOUT.path, 7)
                );
            BasicUtil.broadcast(VoteUpPlaceholder.parse(vote, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Event.End.Broadcast")));

            Notice notice = VoteUpAPI.CACHE_MANAGER.log(Notice.Type.VOTE_END, voteID, new HashMap<>());
            plugin.pConfig.getConfig().getStringList(ConfigManager.Path.ADMIN.path).forEach(
                    adminID -> {
                        Player admin = Bukkit.getPlayer(UUID.fromString(adminID));
                        if (admin != null) {
                            String announce = notice.announce(admin.getUniqueId());
                            if (announce != null)
                                I18n.send(admin, VoteUpPlaceholder.parse(vote, announce));
                        }
                    }
            );
        }
    }

    @Override
    public void saveAll() {
        voteMap.forEach((voteID, vote) -> vote.save());
    }

    @Override
    public void delete(String id) {
        voteMap.remove(id);
    }
}
