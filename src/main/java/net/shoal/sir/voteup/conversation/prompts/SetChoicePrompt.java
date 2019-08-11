package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.VoteChoice;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetChoicePrompt extends StringPrompt {

    private LocaleUtil locale;

    private Player user;
    private String voteID;
    private ChoiceType type;

    public SetChoicePrompt(Player player, ChoiceType type) {
        this.user = player;
        this.voteID = player.getName();
        this.type = type;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7请输入投票选项内容, 类型: " + type.getName() + "(&9支持颜色代码&7).");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7(SetChoicePrompt) 会话输入值已验证通过.");
        String accept = CommonUtil.color(input);
        locale.debug("&7新选项内容(输入值): &c" + accept);
        VoteChoice choiceData = VoteManager.getInstance().getCreatingVote(user.getName()).getChoices();
        Map<ChoiceType, String> choiceMap = choiceData.getChoices();
        choiceMap.put(type, accept);
        choiceData.setChoices(choiceMap);
        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.CHOICE, choiceData);
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));

        switch(type) {
            case ACCEPT:
                return new SetChoicePrompt(user, ChoiceType.NEUTRAL);
            case NEUTRAL:
                return new SetChoicePrompt(user, ChoiceType.REFUSE);
            case REFUSE:
            default:
                CommonUtil.openInventory(
                        user,
                        InventoryUtil.parsePlaceholder(
                                GuiManager.getInstance().getMenu(GuiManager.CREATE_MENU),
                                VoteManager.getInstance().getCreatingVote(voteID))
                );
                return Prompt.END_OF_CONVERSATION;
        }
    }
}
