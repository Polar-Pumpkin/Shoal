package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.ResultType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class SetResultPormpt extends StringPrompt {

    private LocaleUtil locale;

    private Player user;
    private String voteID;
    private ResultType type;

    public SetResultPormpt(Player player, ResultType type) {
        this.user = player;
        this.voteID = player.getName();
        this.type = type;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7请输入投票结果显示内容, 类型: " + type.getName() + "(&9支持颜色代码&7).");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7(SetResultPormpt) 会话输入值已验证通过.");
        String result = CommonUtil.color(input);
        locale.debug("&7新选项内容(输入值): &c" + result);
        boolean setResult = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.valueOf(type.toString()), result);
        locale.debug("&7设置值: &c" + (setResult ? "成功" : "失败"));

        switch (type) {
            case PASS:
                return new SetResultPormpt(user, ResultType.REJECT);
            case REJECT:
            default:
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
}
