package net.shoal.sir.voteup.data.prompts;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.ValidatingPrompt;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

import java.util.regex.Pattern;

public class SetDurationPrompt extends ValidatingPrompt {

    public static final Vote.Data TARGET = Vote.Data.DURATION;
    private final PPlugin plugin;
    private final Player user;
    private final Vote vote;

    public SetDurationPrompt(@NonNull Player player, @NonNull Vote vote) {
        this.plugin = VoteUp.getInstance();
        this.user = player;
        this.vote = vote;
    }

    @Override
    protected boolean isInputValid(ConversationContext context, String input) {
        if (input.contains("d") || input.contains("H") || input.contains("m")) {
            Pattern r = Pattern.compile("^[0-9dhmDHM]*$");
            if (r.matcher(input).matches()) return true;
        }
        I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, BuiltinMsg.ERROR_EDIT_DURATION.msg));
        VoteUpAPI.SOUND.fail(user);
        return false;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(BuiltinMsg.VOTE_EDIT.msg, TARGET.name + "(符号: &dd &7天, &dH &7小时, &dm &7分)"));
    }

    @Override
    protected Prompt acceptValidatedInput(ConversationContext context, String input) {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, BuiltinMsg.ERROR_EDIT_NO_PERM.msg));
            return Prompt.END_OF_CONVERSATION;
        }

        if ("exit".equalsIgnoreCase(input)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(BuiltinMsg.VOTE_EDIT_CANCELLED.msg, TARGET.name)));
            VoteUpAPI.VOTE_MANAGER.back(user);
            return Prompt.END_OF_CONVERSATION;
        }

        vote.duration = input;
        BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(BuiltinMsg.VOTE_EDIT_SUCCESS.msg), TARGET.name)));
        VoteUpAPI.SOUND.success(user);
        VoteUpAPI.VOTE_MANAGER.back(user);
        return Prompt.END_OF_CONVERSATION;
    }
}
