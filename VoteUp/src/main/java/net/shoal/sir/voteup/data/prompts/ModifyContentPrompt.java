package net.shoal.sir.voteup.data.prompts;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpAPI;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.api.VoteUpPlaceholder;
import net.shoal.sir.voteup.api.VoteUpSound;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.Msg;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.BasicUtil;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;

import java.util.List;

public class ModifyContentPrompt extends StringPrompt {

    public static Vote.Data TARGET;
    private final PPlugin plugin;
    private final Player user;
    private final Vote vote;
    private final int index;
    private final boolean targetDesc;
    private final String action;

    public ModifyContentPrompt(@NonNull Player player, @NonNull Vote vote, int index, boolean targetDesc, String action) {
        this.plugin = VoteUp.getInstance();
        this.user = player;
        this.vote = vote;
        this.index = index;
        this.targetDesc = targetDesc;
        TARGET = targetDesc ? Vote.Data.DESCRIPTION : Vote.Data.AUTOCAST;
        this.action = action;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        return plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT.msg, TARGET.name));
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String input) {
        if (!VoteUpPerm.EDIT.hasPermission(user, TARGET)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_NO_PERM.msg));
            return Prompt.END_OF_CONVERSATION;
        }

        if ("exit".equalsIgnoreCase(input)) {
            I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(Msg.VOTE_EDIT_CANCELLED.msg, TARGET.name)));
            VoteUpAPI.VOTE_MANAGER.back(user);
            return Prompt.END_OF_CONVERSATION;
        }

        if (!targetDesc) {
            if (VoteUpAPI.CONFIG.autocast_enable) {
                String[] inputArgs = input.split(" ");
                List<String> commandList = VoteUpAPI.CONFIG.autocast_list;
                boolean usermode = VoteUpAPI.CONFIG.autocast_userMode;

                if (!usermode) {
                    boolean blackMode = VoteUpAPI.CONFIG.autocast_blackList;

                    if ((blackMode && commandList.contains(inputArgs[0])) || (!blackMode && !commandList.contains(inputArgs[0]))) {
                        I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_AUTOCAST_IGNORE.msg));
                        return Prompt.END_OF_CONVERSATION;
                    }
                }
            } else
                I18n.sendAsync(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.WARN, Msg.ERROR_EDIT_AUTOCAST_DISABLE.msg));
        }

        List<String> list = targetDesc ? vote.description : vote.autocast;
        switch (action) {
            case "add":
                if (index > list.size()) list.add(input);
                else list.add(index, input);
                break;
            case "set":
                if (index > list.size()) list.set(list.size() - 1, input);
                else list.set(index, input);
                break;
            default:
                return Prompt.END_OF_CONVERSATION;
        }

        if (targetDesc) vote.description = list;
        else vote.autocast = list;
        BasicUtil.send(plugin, user, plugin.lang.build(plugin.localeKey, I18n.Type.INFO, String.format(I18n.color(Msg.VOTE_EDIT_SUCCESS.msg), TARGET.name)));
        VoteUpSound.success(user);

        JsonChatUtil.sendEditableList(
                user,
                list,
                VoteUpPlaceholder.parse(vote, String.format((targetDesc ? Msg.VOTE_VALUE_DESCRIPTION : Msg.VOTE_VALUE_AUTOCAST).msg, list.size())),
                "&a&l[插入] ",
                "/vote modify " + (targetDesc ? "desc" : "autocast") + " add ",
                "&e&l[编辑] ",
                "/vote modify " + (targetDesc ? "desc" : "autocast") + " set ",
                "&c&l[删除] ",
                "/vote modify " + (targetDesc ? "desc" : "autocast") + " del ",
                "&7[&a&l>>> &7返回菜单]",
                "/vote create back"
        );
        return Prompt.END_OF_CONVERSATION;
    }
}
