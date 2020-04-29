package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

public class SetTitlePrompt extends StringPrompt {

    private LocaleUtil locale;

    private final Player user;
    private final String voteID;

    public SetTitlePrompt(Player player) {
        this.user = player;
        this.voteID = player.getName();
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7请输入投票标题(&9支持颜色代码&7).");
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();

        if (!user.hasPermission(VoteUpPerm.ADMIN.perm()) && !voteID.split("_")[0].equalsIgnoreCase(user.getName())) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7权限验证失败, 您不具有修改目标投票内容的权限."), user.getName());
            return Prompt.END_OF_CONVERSATION;
        }

        plugin.lang.debug("&7(SetTitlePrompt) 会话输入值已验证通过.");

        if("exit".equalsIgnoreCase(input)) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7您已取消输入."), user.getName());
            VoteManager.getInstance().backCreating(user, voteID);
            return Prompt.END_OF_CONVERSATION;
        }

        String title = CommonUtil.color(input);
        plugin.lang.debug("&7新标题(输入值): &c" + title);
        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.TITLE, title);
        plugin.lang.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        VoteManager.getInstance().backCreating(user, voteID);
        return Prompt.END_OF_CONVERSATION;
    }
}
