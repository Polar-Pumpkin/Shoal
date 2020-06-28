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
import org.serverct.parrot.parrotx.utils.I18n;

public class SetIDPrompt extends StringPrompt {

    public static final Vote.Data TARGET = Vote.Data.ID;
    private final PPlugin plugin;
    private final Player user;
    private final Vote vote;

    public SetIDPrompt(@NonNull Player player, @NonNull Vote vote) {
        this.plugin = VoteUp.getInstance();
        this.user = player;
        this.vote = vote;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(BuiltinMsg.VOTE_EDIT.msg, TARGET.name));
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, BuiltinMsg.ERROR_EDIT_NO_PERM.msg));
            return Prompt.END_OF_CONVERSATION;
        }

        if ("exit".equalsIgnoreCase(input)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(BuiltinMsg.VOTE_EDIT_CANCELLED.msg, TARGET.name)));
            VoteUpAPI.VOTE_MANAGER.back(user);
            return Prompt.END_OF_CONVERSATION;
        }

        vote.voteID = I18n.deColor(input, '&');
        // VoteUpAPI.VOTE_MANAGER.setVoteData(vote.voteID, user, TARGET, I18n.color(input));
        VoteUpAPI.VOTE_MANAGER.back(user);
        return Prompt.END_OF_CONVERSATION;
    }
}
