package net.shoal.parrot.egg4everyone.listener;

import net.shoal.parrot.egg4everyone.Egg4everyone;
import org.bukkit.Material;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.api.ParrotXAPI;
import org.serverct.parrot.parrotx.data.autoload.annotations.PAutoload;
import org.serverct.parrot.parrotx.utils.TimeUtil;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Date;
import java.util.HashMap;

@PAutoload
public class PlayerDoneAdvancementListener implements Listener {

    private final PPlugin plugin;
    private final I18n lang;

    public PlayerDoneAdvancementListener() {
        this.plugin = ParrotXAPI.getPlugin(Egg4everyone.class);
        this.lang = this.plugin.getLang();
    }

    @EventHandler
    public void onPlayerAdvancementDone(final PlayerAdvancementDoneEvent event) {
        final Player user = event.getPlayer();
        final Advancement advancement = event.getAdvancement();
        if (!advancement.getKey().getKey().equals("end/kill_dragon")) {
            return;
        }

        user.getPersistentDataContainer().set(Egg4everyone.KEY, PersistentDataType.STRING, TimeUtil.getDefaultFormatDate(new Date()));

        final HashMap<Integer, ItemStack> cantGive =
                user.getInventory().addItem(new ItemStack(Material.DRAGON_EGG));
        if (cantGive.isEmpty()) {
            lang.sender.info(user, "Get");
            return;
        }

        lang.sender.info(user, "Drop");
        user.getWorld().dropItemNaturally(user.getLocation(), new ItemStack(Material.DRAGON_EGG));
    }

}
