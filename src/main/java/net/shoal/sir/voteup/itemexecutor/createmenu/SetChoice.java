package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.conversation.prompts.SetChoicePrompt;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SetChoice implements MenuItemExecutor {

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        Player user = (Player) event.getWhoClicked();
        CommonUtil.closeInventory(user);
        SoundManager.getInstance().ding(user.getName());
        Conversation conversation = new ConversationFactory(VoteUp.getInstance())
                .withFirstPrompt(new SetChoicePrompt(user, ChoiceType.ACCEPT))
                .buildConversation(user);
        conversation.begin();
        return true;
    }
}
