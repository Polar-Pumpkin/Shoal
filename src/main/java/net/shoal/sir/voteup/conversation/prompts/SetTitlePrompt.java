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
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class SetTitlePrompt extends StringPrompt {

    private LocaleUtil locale;

    private Player user;
    private String voteID;

    public SetTitlePrompt(Player player) {
        this.user = player;
        this.voteID = player.getName();
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7请输入投票标题(&9支持颜色代码&7).");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7(SetTitlePrompt) 会话输入值已验证通过.");
        String title = CommonUtil.color(input);
        locale.debug("&7新标题(输入值): &c" + title);
        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.TITLE, title);
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        CommonUtil.openInventory(
                user,
                InventoryUtil.parsePlaceholder(
                        GuiManager.getInstance().getMenu(GuiManager.CREATE_MENU),
                        VoteManager.getInstance().getCreatingVote(voteID),
                        user
                )
        );
        return Prompt.END_OF_CONVERSATION;
    }
}
