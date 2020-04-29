package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class CollectReasonPrompt extends StringPrompt {

    private LocaleUtil locale;

    private final Player user;
    private final String voteID;
    private final ChoiceType type;

    public CollectReasonPrompt(String voteID, Player player, ChoiceType type) {
        this.user = player;
        this.voteID = voteID;
        this.type = type;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7您为什么选择这个选项呢? 发表一下您的看法吧.");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();

        if (!user.hasPermission(VoteUpPerm.ADMIN.perm()) && !voteID.split("_")[0].equalsIgnoreCase(user.getName())) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7权限验证失败, 您不具有修改目标投票内容的权限."), user.getName());
            return Prompt.END_OF_CONVERSATION;
        }

        plugin.lang.debug("&7(CollectReasonPrompt) 会话输入值已验证通过.");
        plugin.lang.debug("&7投票原因(输入值): &c" + CommonUtil.color(input));

        if("exit".equalsIgnoreCase(input)) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7您已取消输入."), user.getName());
            VoteManager.getInstance().backCreating(user, voteID);
            return Prompt.END_OF_CONVERSATION;
        }

        VoteManager.getInstance().vote(voteID, user, input, type);
        return Prompt.END_OF_CONVERSATION;
    }
}
