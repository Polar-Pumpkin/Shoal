package net.shoal.sir.voteup.data.inventory;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.data.inventory.BaseInventory;
import org.serverct.parrot.parrotx.data.inventory.element.BaseElement;
import org.serverct.parrot.parrotx.data.inventory.element.InventoryButton;

import java.io.File;

public class CreateInventory extends BaseInventory<Vote> {
    public CreateInventory(Vote data, Player user) {
        super(VoteUp.getInstance(), data, user, VoteUp.getInstance().getFile("Guis" + File.separator + "VoteCreate.yml"));

        addElement(InventoryButton.builder()
                .base(BaseElement.of(plugin, ))
                .build());
    }
}
