package net.shoal.sir.voteup.api;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.I18n;

public enum VoteUpPerm {
    VOTE("VoteUp.vote."),
    REASON("VoteUp.vote.reason"),
    CREATE("VoteUp.create"),
    VIEW("VoteUp.view"),
    SEARCH("VoteUp.search"),
    ANONYMOUS("VoteUp.view.anonymous"),
    EDIT("VoteUp.edit."),
    NOTICE("VoteUp.notice"),
    ADMIN("VoteUp.admin"),
    ALL("VoteUp.*"),
    ;

    public final String node;

    VoteUpPerm(String perm) {
        this.node = perm;
    }

    public boolean hasPermission(@NonNull Player user, Object... params) {
        PPlugin plugin = VoteUp.getInstance();
        boolean result;
        switch (this) {
            case VOTE:
                if (params.length == 0) {
                    result = false;
                    break;
                }
                result = user.hasPermission(node + ((Vote.Choice) params[0]).name().toLowerCase()) || adminPerm(user);
                break;
            case EDIT:
                if (params.length == 0) {
                    result = false;
                    break;
                }
                result = user.hasPermission(node + ((Vote.Data) params[0]).name().toLowerCase()) || adminPerm(user);
                break;
            default:
                result = user.hasPermission(node) || adminPerm(user);
        }
        if (!result) user.sendMessage(plugin.lang.get(plugin.localeKey, I18n.Type.WARN, "Plugin", "NoPerm"));
        return result;
    }

    private boolean adminPerm(@NonNull Player user) {
        return (node.endsWith(".") && user.hasPermission(node + "*")) || user.hasPermission(ADMIN.node) || user.hasPermission(ALL.node);
    }
}
