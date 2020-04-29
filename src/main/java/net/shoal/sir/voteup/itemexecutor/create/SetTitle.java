package net.shoal.sir.voteup.itemexecutor.create;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.conversation.prompts.SetTitlePrompt;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class SetTitle implements MenuItemExecutor {

    @Override
    public boolean execute(InventoryClickEvent event, Object value) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        Player user = (Player) event.getWhoClicked();
        if(user.hasPermission(VoteUpPerm.CREATE_CUSTOM_TITLE.perm())) {
            CommonUtil.closeInventory(user);
            SoundManager.getInstance().ding(user.getName());
            Conversation conversation = new ConversationFactory(VoteUp.getInstance())
                    .withFirstPrompt(new SetTitlePrompt(user))
                    .buildConversation(user);
            conversation.begin();
        } else {
            SoundManager.getInstance().fail(user.getName());
            user.sendMessage(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7您没有权限这么做. 使用 &d/vote create back &7可以返回投票草稿."));
        }
        return true;
    }
}
