package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.SoundManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class SetDurationPrompt extends ValidatingPrompt {

    private LocaleUtil locale;

    private final Player user;
    private final String voteID;

    public SetDurationPrompt(Player player) {
        this.user = player;
        this.voteID = player.getName();
    }

    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();
        if(input.contains("d") || input.contains("H") || input.contains("m")) {
            Pattern r = Pattern.compile("^[0-9dhmDHM]*$");
            if(r.matcher(input).matches()) {
                return true;
            }
        }
        CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7无法识别您输入的时长."), user);
        SoundManager.getInstance().fail(user.getName());
        return false;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        locale = VoteUp.getInstance().getLocale();

        if (!user.hasPermission(VoteUpPerm.ADMIN.perm()) && !voteID.split("_")[0].equalsIgnoreCase(user.getName())) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.WARN, "&7权限验证失败, 您不具有修改目标投票内容的权限."), user.getName());
            return Prompt.END_OF_CONVERSATION;
        }

        plugin.lang.debug("&7(SetDurationPrompt) 会话输入值已验证通过.");
        plugin.lang.debug("&7新持续时长(输入值): &c" + input);

        if("exit".equalsIgnoreCase(input)) {
            CommonUtil.message(plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7您已取消输入."), user.getName());
            VoteManager.getInstance().backCreating(user, voteID);
            return Prompt.END_OF_CONVERSATION;
        }

        boolean result = VoteManager.getInstance().setCreatingVoteData(voteID, VoteDataType.DURATION, input);
        plugin.lang.debug("&7设置值: &c" + (result ? "成功" : "失败"));
        VoteManager.getInstance().backCreating(user, voteID);
        return Prompt.END_OF_CONVERSATION;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        locale = VoteUp.getInstance().getLocale();
        return plugin.lang.buildMessage(plugin.localeKey, I18n.Type.INFO, "&7请输入投票持续时间(符号: &dd &7天, &dH &7小时, &dm &7分).");
    }
}
