package net.shoal.sir.voteup.itemexecutor.createmenu;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.conversation.prompts.SetResultPormpt;
import net.shoal.sir.voteup.enums.ResultType;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SetResult implements MenuItemExecutor {
    @Override
    public boolean execute(InventoryClickEvent event, Object value) {

        Player user = (Player) event.getWhoClicked();
        CommonUtil.closeInventory(user);
        SoundManager.getInstance().ding(user.getName());
        Conversation conversation = new ConversationFactory(VoteUp.getInstance())
                .withFirstPrompt(new SetResultPormpt(user, ResultType.PASS))
                .buildConversation(user);
        conversation.begin();
        return true;
    }
}
