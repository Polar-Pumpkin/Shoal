package net.shoal.sir.voteup.data;

import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.enums.Msg;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.serverct.parrot.parrotx.data.InventoryExecutor;

import java.util.Optional;

public interface VoteInventoryExecutor extends InventoryExecutor {

    FileConfiguration getFile();

    Vote getVote();

    default Inventory basicConstruct() {
        FileConfiguration file = getFile();
        String title = "未初始化菜单";
        if (file == null) return Bukkit.createInventory(this, 0, title);
        title = VoteUpPlaceholder.parse(getVote(), file.getString("Settings.Title", Msg.ERROR_GUI_TITLE.msg));
        return Bukkit.createInventory(this, file.getInt("Settings.Row", 0) * 9, Optional.ofNullable(title).orElse(Msg.ERROR_GUI_TITLE.msg));
    }

}
