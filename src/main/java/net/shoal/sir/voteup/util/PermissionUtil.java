package net.shoal.sir.voteup.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {

    public static List<Player> hasPermission(String perm) {
        List<Player> result = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> {
            if(player.hasPermission(perm)) {
                result.add(player);
            }
        });
        return result;
    }



}
