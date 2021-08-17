package net.shoal.parrot.egg4everyone.listener;

import net.shoal.parrot.egg4everyone.Egg4everyone;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataType;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoload;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;

@PAutoload
public class PlayerJoinListener implements Listener {

    private final PPlugin plugin;
    private final I18n lang;

    public PlayerJoinListener() {
        this.plugin = ParrotXAPI.getPlugin(Egg4everyone.class);
        this.lang = this.plugin.getLang();
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player user = event.getPlayer();
        final String tag = user.getPersistentDataContainer().get(Egg4everyone.KEY, PersistentDataType.STRING);
        if (!StringUtils.isEmpty(tag)) {
            return;
        }

        final Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/kill_dragon"));
        if (Objects.isNull(advancement)) {
            lang.log.error("无法获取到「解放末地」进度.");
            return;
        }

        final AdvancementProgress progress = user.getAdvancementProgress(advancement);
        if (!progress.isDone()) {
            return;
        }

        for (final String criterion : progress.getAwardedCriteria()) {
            progress.revokeCriteria(criterion);
        }
        lang.log.info("已移除玩家 &a{0} &r的「解放末地」进度.", user.getName());
    }
}
