package net.shoal.sir.voteup.api;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.I18n;

public enum VoteUpPerm {
    VOTE_ACCEPT("VoteUp.vote.accept"),
    VOTE_NEUTRAL("VoteUp.vote.neutral"),
    VOTE_REFUSE("VoteUp.vote.refuse"),
    REASON("VoteUp.vote.reason");

    public final String node;

    VoteUpPerm(String perm) {
        this.node = perm;
    }

    public boolean hasPermission(@NonNull Player user) {
        PPlugin plugin = VoteUp.getInstance();
        boolean result = user.hasPermission(node);
        if (!result) user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "NoPerm"));
        return result;
    }
}
