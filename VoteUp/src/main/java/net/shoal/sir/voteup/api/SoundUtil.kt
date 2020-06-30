package net.shoal.sir.voteup.api;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.ConfPath;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.EnumUtil;

import java.util.UUID;

@SuppressWarnings("all")
public class SoundUtil {
    private final PPlugin plugin = VoteUp.getInstance();

    public void success(@NonNull Player user) {
        play(user.getUniqueId(), plugin.pConfig.getConfig().getString(ConfPath.Path.SOUND_ACTION_SUCCESS.path, "ENTITY_VILLAGER_YES"));
    }

    public void fail(@NonNull Player user) {
        play(user.getUniqueId(), plugin.pConfig.getConfig().getString(ConfPath.Path.SOUND_ACTION_FAILURE.path, "BLOCK_ANVIL_DESTROY"));
    }

    public void ding(@NonNull Player user) {
        play(user.getUniqueId(), plugin.pConfig.getConfig().getString(ConfPath.Path.SOUND_ACTION_START.path, "ENTITY_ARROW_HIT_PLAYER"));
    }

    public void voteEvent(boolean isStart) {
        Bukkit.getOnlinePlayers().forEach(
                player -> play(player.getUniqueId(),
                        (isStart
                                ? plugin.pConfig.getConfig().getString(ConfPath.Path.SOUND_VOTE_START.path, "ENTITY_ARROW_HIT_PLAYER")
                                : plugin.pConfig.getConfig().getString(ConfPath.Path.SOUND_VOTE_END.path, "BLOCK_ANVIL_HIT")
                        )
                )
        );
    }

    public void play(UUID uuid, String sound) {
        play(uuid, EnumUtil.valueOf(Sound.class, sound.toUpperCase()));
    }

    public void play(UUID uuid, Sound sound) {
        Player user = Bukkit.getPlayer(uuid);
        if (user != null) user.playSound(user.getLocation(), sound, 1, 1);
    }

}
