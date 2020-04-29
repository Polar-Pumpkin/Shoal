package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;

public class SetAmountPrompt extends NumericPrompt {

    private LocaleUtil locale;

    private final Player user;
    private final String voteID;

    public SetAmountPrompt(Player player) {
        this.user = player;
        this.voteID = player.getName();
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
        locale = VoteUp.getInstance().getLocale();

        if (!user.hasPermission(VoteUpPerm.ADMIN.perm()) && !voteID.split("_")[0].equalsIgnoreCase(user.getName())) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7权限验证失败, 您不具有修改目标投票内容的权限."), user.getName());
            return Prompt.END_OF_CONVERSATION;
        }

        plugin.lang.debug("&7(SetAmountPrompt) 会话输入值已验证通过.");
        plugin.lang.debug("&7新人数要求(输入值): &c" + input);

        if(input.equals(-1)) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7您已取消输入."), user.getName());
            VoteManager.getInstance().backCreating(user, voteID);
            return Prompt.END_OF_CONVERSATION;
        }

        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.AMOUNT, input);
        plugin.lang.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        VoteManager.getInstance().backCreating(user, voteID);
        return null;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7请输入需求同意人数(正整数).");
    }
}
