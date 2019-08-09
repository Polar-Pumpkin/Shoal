package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.Sound;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;

public class SetTitlePrompt extends ValidatingPrompt {

    private LocaleUtil locale;

    private Player user;
    private String voteID;

    public SetTitlePrompt(Player player) {
        this.user = player;
        this.voteID = player.getName();
    }

    private String title;
    private boolean result;
    private String success;
    private String failure;

    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        return true;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7(SetTitlePrompt) 会话输入值已验证通过.");
        title = CommonUtil.color(input);
        locale.debug("&7新标题(输入值): &c" + title);
        result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.TITLE, title);
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        success = locale.buildMessage(VoteUp.getInstance().getLocaleKey(), MessageType.INFO, "&7设置投票标题成功: " + title);
        failure = locale.buildMessage(VoteUp.getInstance().getLocaleKey(), MessageType.WARN, "&7设置投票标题失败, 您的 ID 下没有待发布的投票.");
        user.sendMessage(result ? success : failure);
        user.playSound(user.getLocation(), result ? Sound.ENTITY_VILLAGER_YES : Sound.BLOCK_ANVIL_BREAK, 1, 1);
        CommonUtil.openInventory(user, InventoryUtil.parsePlaceholder(GuiManager.getInstance().getMenu("CreateMenu"), VoteManager.getInstance().getCreatingVote(voteID)).getInventory());
        return Prompt.END_OF_CONVERSATION;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return locale.buildMessage(VoteUp.getInstance().getLocaleKey(), MessageType.INFO, "&7请输入投票标题(&9支持颜色代码&7).");
    }
}
