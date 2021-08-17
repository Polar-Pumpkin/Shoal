package net.shoal.sir.voteup.data;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.data.flags.Timestamp;
import org.serverct.parrot.parrotx.utils.ConfigUtil;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.TimeUtil;

import java.util.*;

public class Notice implements Timestamp {

    private final PPlugin plugin = VoteUp.getInstance();
    public int number;
    public Type type;
    public String voteID;
    public List<UUID> announced = new ArrayList<>();
    public Map<String, Object> params = new HashMap<>();
    private long time;

    public Notice(Type type, String voteID, int number, Map<String, Object> params) {
        this.type = type;
        this.voteID = voteID;
        this.number = number;
        this.params.putAll(params);
        this.time = System.currentTimeMillis();
    }

    /*
    voteID:
      '1':
        Type: type
        Timestamp: timestamp
        Announced:
          - uuid
          - uuid
        Params:
          Voter: Cat
          Choice: ACCEPT
          Reason: string
     */

    public Notice(String voteID, @NonNull ConfigurationSection section) {
        this.voteID = voteID;
        this.number = Integer.parseInt(section.getName());
        this.type = EnumUtil.valueOf(Type.class, section.getString("Type").toUpperCase());
        this.time = section.getLong("Timestamp");
        section.getStringList("Announced").forEach(uuid -> this.announced.add(UUID.fromString(uuid)));
        this.params.putAll(ConfigUtil.getMap(section, "Params"));
    }

    public void save(@NonNull ConfigurationSection section) {
        section.set("Type", type.name());
        section.set("Timestamp", time);
        List<String> strList = new ArrayList<>();
        this.announced.forEach(uuid -> strList.add(uuid.toString()));
        section.set("Announced", strList);
        ConfigurationSection params = section.createSection("Params");
        this.params.forEach(params::set);
    }

    public String announce(UUID uuid) {
        Vote vote = VoteUpAPI.VOTE_MANAGER.getVote(voteID);
        if (vote == null) return null;
        String result = null;

        switch (type) {
            case VOTE_END:
            case VOTE:
            default:
                if (this.announced.contains(uuid)) return null;
                this.announced.add(uuid);
                result = plugin.lang.getRaw(plugin.localeKey, "Vote", "Notice." + type.name() + (vote.isOwner(uuid) ? ".Noticer" : ".Starter"));
                for (Map.Entry<String, Object> entry : this.params.entrySet())
                    result = result.replace("%" + entry.getKey().toLowerCase() + "%", (String) entry.getValue());
                result = VoteUpPlaceholder.parse(vote, result)
                        .replace("%time%", TimeUtil.getDescriptionTimeFromTimestamp(time) + " &7[" + getTime() + "]");
                break;
            case AUTOCAST_WAIT_EXECUTE:
                if (!vote.isOwner(uuid) || this.announced.contains(uuid)) return null;
                this.announced.add(uuid);
                Player owner = Bukkit.getPlayer(vote.getOwner());
                if (owner == null) break;
                vote.autocast.forEach(owner::performCommand);
                break;
        }

        return result;
    }

    public boolean isOver() {
        Vote vote = VoteUpAPI.VOTE_MANAGER.getVote(voteID);
        if (vote == null) return true;

        switch (type) {
            case VOTE:
            case VOTE_END:
            default:
                List<String> admins = VoteUpAPI.CONFIG.admins;
                List<String> announced = new ArrayList<>();
                this.announced.forEach(uuid -> announced.add(uuid.toString()));
                return announced.equals(admins);
            case AUTOCAST_WAIT_EXECUTE:
                return this.announced.contains(vote.getOwner());
        }
    }

    @Override
    public long getTimestamp() {
        return time;
    }

    @Override
    public void setTime(long l) {
        this.time = l;
    }

    public enum Type {
        VOTE_END,
        VOTE,
        AUTOCAST_WAIT_EXECUTE,
    }
}
