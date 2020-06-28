package net.shoal.sir.voteup.data.prompts;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;

public class SetResultPrompt extends StringPrompt {

    public static final Vote.Data TARGET = Vote.Data.RESULT;
    private final PPlugin plugin;
    private final Player user;
    private final Vote vote;
    private final Vote.Result type;

    public SetResultPrompt(@NonNull Player player, @NonNull Vote vote, Vote.Result type) {
        this.plugin = VoteUp.getInstance();
        this.user = player;
        this.vote = vote;
        this.type = type;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(BuiltinMsg.VOTE_EDIT.msg, TARGET.name + "(" + type.name + ")"));
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, BuiltinMsg.ERROR_EDIT_NO_PERM.msg));
            return Prompt.END_OF_CONVERSATION;
        }

        if ("exit".equalsIgnoreCase(input)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(BuiltinMsg.VOTE_EDIT_CANCELLED.msg, TARGET.name + "(" + type.name + ")")));
            VoteUpAPI.VOTE_MANAGER.back(user);
            return Prompt.END_OF_CONVERSATION;
        } else if ("next".equalsIgnoreCase(input)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(BuiltinMsg.VOTE_EDIT_PASSED.msg, TARGET.name + "(" + type.name + ")")));
            return next(type);
        }

        vote.results.put(type, I18n.color(input));
        BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(BuiltinMsg.VOTE_EDIT_SUCCESS.msg), TARGET.name)));
        VoteUpAPI.SOUND.success(user);
        return next(type);
    }

    private Prompt next(Vote.Result type) {
        switch (type) {
            case PASS:
                return new SetResultPrompt(user, vote, Vote.Result.DRAW);
            case DRAW:
                return new SetResultPrompt(user, vote, Vote.Result.REJECT);
            case REJECT:
                return new SetResultPrompt(user, vote, Vote.Result.CANCEL);
            case CANCEL:
            default:
                VoteUpAPI.VOTE_MANAGER.back(user);
                return Prompt.END_OF_CONVERSATION;
        }
    }
}
