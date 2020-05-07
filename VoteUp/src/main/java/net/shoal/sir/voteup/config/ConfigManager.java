package net.shoal.sir.voteup.config;

import net.shoal.sir.voteup.VoteUp;
import org.serverct.parrot.parrotx.config.PConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ConfigManager extends PConfig {
    public ConfigManager() {
        super(VoteUp.getInstance(), "config", "主配置文件");
    }

    @Override
    public void saveDefault() {
        this.plugin.saveDefaultConfig();
    }

    public enum Path {
        SOUND_ENABLE("Sound.Enable"),
        SOUND_ACTION_START("Sound.Action.Start"),
        SOUND_ACTION_SUCCESS("Sound.Action.Success"),
        SOUND_ACTION_FAILURE("Sound.Action.Failure"),
        SOUND_VOTE_START("Sound.Vote.Start"),
        SOUND_VOTE_END("Sound.Vote.End"),
        AUTOCAST_ENABLE("Autocast.Enable"),
        AUTOCAST_BLACKLIST("Autocast.Blacklist"),
        AUTOCAST_LIST("Autocast.List"),
        ADMIN("Admin"),
        SETTINGS_PARTICIPANT_LEAST("Settings.ParticipantLeast"),
        SETTINGS_BROADCAST_TITLE_VOTESTART("Settings.Broadcast.Title.VoteStart"),
        SETTINGS_BROADCAST_TITLE_VOTEEND("Settings.Broadcast.Title.VoteEnd"),
        SETTINGS_BROADCAST_TITLE_FADEIN("Settings.Broadcast.Title.FadeIn"),
        SETTINGS_BROADCAST_TITLE_STAY("Settings.Broadcast.Title.Stay"),
        SETTINGS_BROADCAST_TITLE_FADEOUT("Settings.Broadcast.Title.FadeOut"),
        ;

        public final String path;

        Path(String path) {
            this.path = path;
        }
    }

    public List<UUID> admins() {
        List<UUID> admins = new ArrayList<>();
        config.getStringList(Path.ADMIN.path).forEach(uuid -> admins.add(UUID.fromString(uuid)));
        return admins;
    }
}
