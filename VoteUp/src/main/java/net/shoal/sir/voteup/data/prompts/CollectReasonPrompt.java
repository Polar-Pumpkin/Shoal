package net.shoal.sir.voteup.data.prompts;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpSound;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.Msg;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.I18n;

public class CollectReasonPrompt extends StringPrompt {

    public static final Vote.Data TARGET = Vote.Data.PARTICIPANT;
    private final PPlugin plugin;
    private final Player user;
    private final Vote vote;
    private final Vote.Choice type;
    private final boolean anonymous;

    public CollectReasonPrompt(@NonNull Player player, @NonNull Vote vote, Vote.Choice type, boolean anonymous) {
        this.plugin = VoteUp.getInstance();
        this.user = player;
        this.vote = vote;
        this.type = type;
        this.anonymous = anonymous;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, Msg.VOTE_REASON.msg);
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg));
            return Prompt.END_OF_CONVERSATION;
        }

        if ("exit".equalsIgnoreCase(input)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT_CANCELLED.msg, TARGET.name + "(" + type.name + ")")));
            VoteUpAPI.VOTE_MANAGER.back(user);
            return Prompt.END_OF_CONVERSATION;
        }

        VoteUpAPI.VOTE_MANAGER.vote(vote.voteID, user, type, anonymous, input);
        VoteUpSound.ding(user);
        return Prompt.END_OF_CONVERSATION;
    }
}
