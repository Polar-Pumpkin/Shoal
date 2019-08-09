package net.shoal.sir.voteup.conversation.prompts;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoteDurationPrompt extends ValidatingPrompt {
    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        if(input.contains("d") || input.contains("H") || input.contains("m")) {
            String[] patternList = {
                    "^[0-9]d[0-9]H[0-9]m$",
                    "^[0-9]d[0-9]H$",
                    "^[0-9]d[0-9]m$",
                    "^[0-9]H[0-9]m$",
                    "^[0-9]d$",
                    "^[0-9]H$",
                    "^[0-9]m$"
            };
            for(String pattern : patternList) {
                Pattern r = Pattern.compile(pattern);
                if(r.matcher(input).matches()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        return Prompt.END_OF_CONVERSATION;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        LocaleUtil locale = VoteUp.getInstance().getLocale();
        return locale.buildMessage(VoteUp.getInstance().getLocaleKey(), MessageType.INFO, "&7请输入投票持续时间(符号: &dd &7天, &dH &7小时, &dm &7分).");
    }
}
