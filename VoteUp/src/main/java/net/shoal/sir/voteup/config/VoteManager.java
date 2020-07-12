package net.shoal.sir.voteup.config;

import lombok.NonNull;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.api.VoteUpSound;
import net.shoal.sir.voteup.data.Notice;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.inventory.CreateInventoryHolder;
import net.shoal.sir.voteup.enums.Msg;
import net.shoal.sir.voteup.enums.VoteFilter;
import net.shoal.sir.voteup.task.VoteEndTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.serverct.parrot.parrotx.config.PFolder;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;

import java.io.File;
import java.util.*;
import java.util.function.Predicate;

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

    public void load(@NonNull Vote vote) {
        voteMap.put(vote.voteID, vote);
    }

    private void startCountdown(String voteID) {
        Vote data = voteMap.get(voteID);
        if (data == null) return;
        if (!data.open) return;
        if (data.isDraft) return;
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
        return voteMap.get(id);
    }

    public Vote getNewest() {
        List<Vote> votes = new ArrayList<>(voteMap.values());
        votes.removeIf(vote -> vote.isDraft || !vote.open);
        if (votes.isEmpty()) return null;
        votes.sort(Comparator.comparing(Vote::getTimestamp).reversed());
        return votes.get(0);
    }

    public List<Vote> list(String filter) {
        Predicate<Vote> predicate = vote -> true;
        String[] dataSet = filter.split("[ ]");
        for (String voteFilter : dataSet) {
            String[] data = voteFilter.split("[:]");
            if (data.length != 2) continue;
            VoteFilter key = EnumUtil.valueOf(VoteFilter.class, data[0].toUpperCase());
            String param = data[1];

            switch (key) {
                case OPEN:
                    boolean open = Boolean.parseBoolean(param);
                    predicate = predicate.and(vote -> vote.open == open);
                    break;
                case TIME:
                    long time = System.currentTimeMillis() - Vote.getDurationTimestamp(param);
                    predicate = predicate.and(vote -> vote.startTime >= time);
                    break;
                case OWNER:
                    if (VoteUpPlaceholder.UUID_PATTERN.matcher(param).matches())
                        predicate = predicate.and(vote -> vote.isOwner(UUID.fromString(param)));
                    else
                        predicate = predicate.and(vote -> vote.isOwner(param));
                    break;
                case VOTER:
                    if (VoteUpPlaceholder.UUID_PATTERN.matcher(param).matches())
                        predicate = predicate.and(vote -> vote.isVoted(UUID.fromString(param)));
                    else
                        predicate = predicate.and(vote -> vote.isOwner(param));
                    break;
                case RESULT:
                    Vote.Result result = EnumUtil.valueOf(Vote.Result.class, param);
                    if (result == null) break;
                    predicate = predicate.and(vote -> vote.result() == result);
                    break;
            }
        }
        return list(predicate);
    }

    public List<Vote> list(Predicate<Vote> filter) {
        List<Vote> result = new ArrayList<>();
        voteMap.values().forEach(vote -> {
            if (filter.test(vote)) result.add(vote);
        });
        return result;
    }

    public Vote create(UUID uuid) {
        Vote vote = new Vote(VoteUpAPI.CONFIG.setting_participantLeast, uuid, "1d");
        voteMap.put(vote.voteID, vote);
        return vote;
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
        vote.startTime = System.currentTimeMillis();
        vote.open = true;
        vote.isDraft = false;
        startCountdown(vote.voteID);

        VoteUpSound.voteEvent(true);
        if (VoteUpAPI.CONFIG.title_start)
            BasicUtil.broadcastTitle(
                    "",
                    VoteUpPlaceholder.parse(vote, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.Start.Subtitle")),
                    VoteUpAPI.CONFIG.title_fadeIn,
                    VoteUpAPI.CONFIG.title_stay,
                    VoteUpAPI.CONFIG.title_fadeOut
            );
        Bukkit.getOnlinePlayers().forEach(player -> player.spigot().sendMessage(JsonChatUtil.buildClickText(
                VoteUpPlaceholder.parse(vote, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Event.Start.Broadcast")),
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote view " + vote.voteID),
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(Msg.VOTE_CLICK.msg)))
        )));
    }

    public void vote(String voteID, @NonNull Player user, Vote.Choice choice, boolean anonymous, String reason) {
        UUID uuid = user.getUniqueId();
        Vote vote = getVote(voteID);
        if (vote == null) return;
        if (!vote.open) {
            I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Closed"));
            return;
        }
        if (!VoteUpPerm.VOTE.hasPermission(user, choice)) return;

        if (vote.isVoted(uuid))
            I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote.Fail.Logged"));

        vote.participants.removeIf(participant -> participant.uuid == uuid);
        vote.participants.add(
                new Vote.Participant(
                        uuid,
                        choice,
                        anonymous,
                        VoteUpPerm.REASON.hasPermission(user) ? (reason.length() == 0 ? Msg.REASON_NOT_YET.msg : reason) : Msg.REASON_NO_PERM.msg,
                        VoteUpAPI.CONFIG.weight(user)
                )
        );
        vote.save();

        I18n.send(user, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Vote." + choice.name()));

        if (anonymous) return;
        Notice notice = VoteUpAPI.CACHE_MANAGER.log(Notice.Type.VOTE, voteID, new HashMap<String, Object>() {
            {
                put("Voter", user.getName());
                put("Choice", choice.name());
                put("Reason", reason);
            }
        });

        List<UUID> receiver = new ArrayList<>();

        if (vote.isPublic) Bukkit.getOnlinePlayers().forEach(player -> receiver.add(player.getUniqueId()));
        else {
            receiver.add(vote.getOwner());
            VoteUpAPI.CONFIG.admins.forEach(adminID -> receiver.add(UUID.fromString(adminID)));
        }


        if (!receiver.isEmpty()) {
            String announce = notice.announce(user.getUniqueId());
            if (announce != null)
                receiver.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(target -> I18n.send(target, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, VoteUpPlaceholder.parse(vote, announce))));
        }
    }

    public void endVote(String voteID) {
        Vote vote = getVote(voteID);
        if (vote == null) return;
        if (vote.open) {
            vote.open = false;
            vote.save();

            if (vote.isPassed())
                if (VoteUpAPI.CONFIG.autocast_userMode) {
                    Player owner = Bukkit.getPlayer(vote.getOwner());
                    if (owner != null) vote.autocast.forEach(owner::performCommand);
                    else VoteUpAPI.CACHE_MANAGER.log(Notice.Type.AUTOCAST_WAIT_EXECUTE, voteID, new HashMap<>());
                } else vote.autocast.forEach(cmd -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
            VoteUpSound.voteEvent(false);
            if (VoteUpAPI.CONFIG.title_end)
                BasicUtil.broadcastTitle(
                        "",
                        VoteUpPlaceholder.parse(vote, plugin.lang.getRaw(plugin.localeKey, "Vote", "Event.End.Subtitle")),
                        VoteUpAPI.CONFIG.title_fadeIn,
                        VoteUpAPI.CONFIG.title_stay,
                        VoteUpAPI.CONFIG.title_fadeOut
                );
            BasicUtil.broadcast(VoteUpPlaceholder.parse(vote, plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Event.End.Broadcast")));

            Notice notice = VoteUpAPI.CACHE_MANAGER.log(Notice.Type.VOTE_END, voteID, new HashMap<>());
            VoteUpAPI.CONFIG.admins.forEach(
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
