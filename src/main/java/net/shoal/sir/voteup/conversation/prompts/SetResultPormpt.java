package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.ResultType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.CommonUtil;
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

        if(!user.hasPermission(VoteUpPerm.ADMIN.perm()) || !voteID.split("//.")[0].equalsIgnoreCase(user.getName())) {
            CommonUtil.message(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7权限验证失败, 您不具有修改目标投票内容的权限."), user.getName());
            return Prompt.END_OF_CONVERSATION;
        }

        locale.debug("&7(SetResultPrompt) 会话输入值已验证通过.");

        if("exit".equalsIgnoreCase(input)) {
            CommonUtil.message(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7您已取消输入."), user.getName());
            VoteManager.getInstance().backCreating(user, voteID);
            return Prompt.END_OF_CONVERSATION;
        } else if("next".equalsIgnoreCase(input)) {
            CommonUtil.message(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7您跳过了输入."), user.getName());
            return next(type);
        }

        String result = CommonUtil.color(input);
        locale.debug("&7新选项内容(输入值): &c" + result);
        boolean setResult = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.valueOf(type.toString()), result);
        locale.debug("&7设置值: &c" + (setResult ? "成功" : "失败"));

        return next(type);
    }

    private Prompt next(ResultType type) {
        switch (type) {
            case PASS:
                return new SetResultPormpt(user, ResultType.REJECT);
            case REJECT:
            default:
                VoteManager.getInstance().backCreating(user, voteID);
                return Prompt.END_OF_CONVERSATION;
        }
    }
}
