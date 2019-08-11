package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class SetAmountPrompt extends NumericPrompt {

    private LocaleUtil locale;

    private Player user;
    private String voteID;

    public SetAmountPrompt(Player player) {
        this.user = player;
        this.voteID = player.getName();
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7(SetAmountPrompt) 会话输入值已验证通过.");
        locale.debug("&7新人数要求(输入值): &c" + input);
        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.AMOUNT, input);
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        CommonUtil.openInventory(
                user,
                InventoryUtil.parsePlaceholder(
                        GuiManager.getInstance().getMenu(GuiManager.CREATE_MENU),
                        VoteManager.getInstance().getCreatingVote(voteID))
        );
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7请输入需求同意人数(正整数).");
    }
}
