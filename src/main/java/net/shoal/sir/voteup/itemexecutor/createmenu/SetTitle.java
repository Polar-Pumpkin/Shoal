package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.conversation.prompts.SetTitlePrompt;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import org.bukkit.Sound;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SetTitle implements MenuItemExecutor {

    private Player user;

    @Override
    public boolean execute(InventoryClickEvent event) {
        user = (Player) event.getWhoClicked();
        CommonUtil.closeInventory(user);
        user.playSound(user.getLocation(), Sound.ENTITY_ARROW_HIT_PLAYER, 1, 1);
        Conversation conversation = new ConversationFactory(VoteUp.getInstance())
                .withFirstPrompt(new SetTitlePrompt(user))
                .buildConversation(user);
        conversation.begin();
        return true;
    }
}
