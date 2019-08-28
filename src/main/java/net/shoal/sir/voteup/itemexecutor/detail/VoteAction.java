package net.shoal.sir.voteup.itemexecutor.detail;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.conversation.prompts.CollectReasonPrompt;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

public class VoteAction implements MenuItemExecutor {

    private ChoiceType type;

    public VoteAction(ChoiceType type) {
        this.type = type;
    }

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        Player user = (Player) event.getWhoClicked();
        CommonUtil.closeInventory(user);
        SoundManager.getInstance().ding(user.getName());
        String id = (String) value;
        if(id != null && !id.equalsIgnoreCase("")) {
            if(event.getClick() == ClickType.LEFT) {
                VoteManager.getInstance().vote(id, user, CommonUtil.color(VoteManager.VOTE_REASON_UNUPLOADED), type);
            } else if(event.getClick() == ClickType.RIGHT) {
                Conversation conversation = new ConversationFactory(VoteUp.getInstance())
                        .withFirstPrompt(new CollectReasonPrompt(id, user, type))
                        .buildConversation(user);
                conversation.begin();
            }
        } else {
            user.sendMessage(locale.buildMessage(VoteUp.LOCALE, MessageType.ERROR, "处理数据时遇到错误, 请联系管理员寻求帮助."));
        }
        return true;
    }
}
