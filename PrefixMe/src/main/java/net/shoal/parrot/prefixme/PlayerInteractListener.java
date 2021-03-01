package net.shoal.parrot.prefixme;

import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.Objects;

public class PlayerInteractListener implements Listener {

    private final PrefixMe plugin = PrefixMe.getInst();
    private final I18n lang = plugin.getLang();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        final Player user = event.getPlayer();
        final ItemStack item = event.getItem();
        if (Objects.isNull(item)) {
            return;
        }
        if (item.isSimilar(Conf.prefixItem)) {
            final int amountLeft = item.getAmount() - 1;
            if (amountLeft <= 0) {
                event.setCancelled(true);
                user.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                item.setAmount(amountLeft);
                // user.getInventory().setItemInMainHand(item);
            }

            final boolean[] success = new boolean[1];
            new AnvilGUI.Builder()
                    .title(lang.data.get(plugin.localeKey, "Anvil.Title"))
                    .text(lang.data.get(plugin.localeKey, "Anvil.Text"))
                    .item(Conf.prefixItem.clone())
                    .plugin(plugin)
                    .onClose(player -> {
                        if (!success[0]) {
                            player.getInventory().addItem(Conf.prefixItem.clone());
                            I18n.send(player, lang.data.get(plugin.localeKey, "Message.Cancel"));
                        }
                    })
                    .onComplete((player, content) -> {
                        if (content.length() <= 0) {
                            return AnvilGUI.Response.text(lang.data.get(plugin.localeKey, "Message.Empty"));
                        }
                        if (content.length() > Conf.limit) {
                            return AnvilGUI.Response.text(lang.data.get(plugin.localeKey, "Message.Limit"));
                        }
                        PrefixManager.getInst().set(player.getUniqueId(), content);
                        success[0] = true;
                        I18n.send(user, lang.data.get(plugin.localeKey, "Message", "Success", content));
                        user.playSound(user.getLocation(), Conf.prefixSound, 1, 1);
                        return AnvilGUI.Response.close();
                    })
                    .open(user);
        } else if (item.isSimilar(Conf.deprefixItem)) {
            final int amountLeft = item.getAmount() - 1;
            if (amountLeft <= 0) {
                event.setCancelled(true);
                user.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            } else {
                item.setAmount(amountLeft);
                // user.getInventory().setItemInMainHand(item);
            }
            PrefixManager.getInst().set(user.getUniqueId(), "");
            I18n.send(user, lang.data.get(plugin.localeKey, "Message.Deprefix"));
            user.playSound(user.getLocation(), Conf.deprefixSound, 1, 1);
        }
    }

}
