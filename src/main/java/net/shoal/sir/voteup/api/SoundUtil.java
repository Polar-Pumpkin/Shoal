package net.shoal.sir.voteup.api;

import net.shoal.sir.voteup.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.utils.EnumUtil;

import java.util.UUID;

public class SoundUtil {
    public void success(UUID uuid) {
        play(uuid, ConfigManager.SOUND_ACTION_SUCCESS);
    }

    public void fail(UUID uuid) {
        play(uuid, ConfigManager.SOUND_ACTION_FAILURE);
    }

    public void ding(UUID uuid) {
        play(uuid, ConfigManager.SOUND_ACTION_START);
    }

    public void voteEvent(boolean isStart) {
        Bukkit.getOnlinePlayers().forEach(
                player -> play(player.getUniqueId(), (isStart ? ConfigManager.SOUND_VOTE_START : ConfigManager.SOUND_VOTE_END))
        );
    }

    public void play(UUID uuid, String sound) {
        Player user = Bukkit.getPlayer(uuid);
        if (user != null) user.playSound(user.getLocation(), EnumUtil.valueOf(Sound.class, sound.toUpperCase()), 1, 1);
    }

    public void play(UUID uuid, Sound sound) {
        Player user = Bukkit.getPlayer(uuid);
        if (user != null) user.playSound(user.getLocation(), sound, 1, 1);
    }

}
