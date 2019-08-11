package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

import java.util.Map;

public class SetChoicePrompt implements Prompt {

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
    public boolean blocksForInput(ConversationContext context) {
        return false;
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();
        locale.debug("&7(SetChoicePrompt) 会话输入值已验证通过.");
        String accept = CommonUtil.color(input);
        locale.debug("&7新选项内容(输入值): &c" + accept);
        Map<ChoiceType, String> choices = VoteManager.getInstance().getCreatingVote(user.getName()).getChoices().getChoices();
        choices.put(type, accept);
        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.CHOICE, choices);
        locale.debug("&7设置值: &c" + (result ? "成功" : "失败"));

        Prompt next;
        switch(type) {
            case ACCEPT:
                next = new SetChoicePrompt(user, ChoiceType.NEUTRAL);
                break;
            case NEUTRAL:
                next = new SetChoicePrompt(user, ChoiceType.REFUSE);
                break;
            case REFUSE:
            default:
                next = Prompt.END_OF_CONVERSATION;
                break;
        }
        return next;
    }
}
