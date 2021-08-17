package net.shoal.sir.voteup.api;

import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.utils.EnumUtil;

import java.util.Optional;
import java.util.UUID;

public class VoteUpSound {
    public static void success(@NonNull Player user) {
        play(user.getUniqueId(), Optional.ofNullable(EnumUtil.valueOf(Sound.class, VoteUpAPI.CONFIG.action_success)).orElse(Sound.ENTITY_VILLAGER_YES));
    }

    public static void fail(@NonNull Player user) {
        play(user.getUniqueId(), Optional.ofNullable(EnumUtil.valueOf(Sound.class, VoteUpAPI.CONFIG.action_failure)).orElse(Sound.BLOCK_ANVIL_DESTROY));
    }

    public static void ding(@NonNull Player user) {
        play(user.getUniqueId(), Optional.ofNullable(EnumUtil.valueOf(Sound.class, VoteUpAPI.CONFIG.action_start)).orElse(Sound.ENTITY_ARROW_HIT_PLAYER));
    }

    public static void voteEvent(boolean isStart) {
        Bukkit.getOnlinePlayers().forEach(
                player -> play(player.getUniqueId(),
                        (isStart
                                ? Optional.ofNullable(EnumUtil.valueOf(Sound.class, VoteUpAPI.CONFIG.vote_start)).orElse(Sound.ENTITY_ARROW_HIT_PLAYER)
                                : Optional.ofNullable(EnumUtil.valueOf(Sound.class, VoteUpAPI.CONFIG.vote_end)).orElse(Sound.BLOCK_ANVIL_HIT)
                        ))
        );
    }

    public static void play(UUID uuid, String sound) {
        play(uuid, EnumUtil.valueOf(Sound.class, sound.toUpperCase()));
    }

    public static void play(UUID uuid, Sound sound) {
        if (!VoteUpAPI.CONFIG.sound_enable) return;
        Player user = Bukkit.getPlayer(uuid);
        if (user != null) user.playSound(user.getLocation(), sound, 1, 1);
    }

}
