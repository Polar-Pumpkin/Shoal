package net.shoal.sir.voteup.api;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.ConfPath;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.Msg;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoteUpPlaceholder {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");
    public static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}");

    public static String parse(Vote vote, String text) {
        if (text == null) return null;

        Matcher m = PLACEHOLDER_PATTERN.matcher(text);

        while (m.find()) {
            String format = m.group(1);
            int index = format.indexOf("_");
            if (index >= format.length()) continue;

            Vote.Data identifier = EnumUtil.valueOf(Vote.Data.class, (index <= 0 ? format : format.substring(0, index)).toUpperCase());
            if (identifier == null) continue;
            String params = index <= 0 ? "" : format.substring(index + 1);

            String value = request(vote, identifier, params);
            if (value != null) text = text.replaceAll(Pattern.quote(m.group()), Matcher.quoteReplacement(value));
        }

        return I18n.color(text);
    }

    private static String request(Vote vote, Vote.Data identifier, String params) {
        if (vote == null) return Msg.ERROR_PLACEHOLDER_REQUEST.msg;
        if (params.equalsIgnoreCase("display")) return identifier.name;
        PPlugin plugin = VoteUp.getInstance();

        switch (identifier) {
            case ID:
                return vote.voteID;
            case OPEN:
                return plugin.lang.getRaw(plugin.localeKey, "Vote", "Status." + (vote.open ? "Processing" : "End"));
            case TYPE:
                if ("desc".equalsIgnoreCase(params)) return vote.type.desc;
                return vote.type.name;
            case GOAL:
                return String.valueOf(vote.goal);
            case OWNER:
                return vote.getOwnerName();
            case STARTTIME:
                if ("desc".equalsIgnoreCase(params))
                    return TimeUtil.getDescriptionTimeFromTimestamp(vote.getTimestamp());
                return vote.getTime();
            case DURATION:
                String duration = vote.duration;
                for (Vote.Duration key : Vote.Duration.values())
                    duration = duration.replace(String.valueOf(key.code), " " + key.name + " ");
                if (duration.endsWith(" ")) duration = duration.substring(0, duration.lastIndexOf(" "));
                return duration;
            case TITLE:
                return vote.title;
            case DESCRIPTION:
                return String.format(Msg.VOTE_VALUE_DESCRIPTION.msg, vote.description.size());
            case CHOICE:
                Vote.Choice choice = EnumUtil.valueOf(Vote.Choice.class, params.toUpperCase());
                if (choice == null) return Msg.ERROR_PLACEHOLDER_REQUEST.msg;
                return vote.choices.getOrDefault(choice, ChatColor.BLUE + choice.name);
            case AUTOCAST:
                boolean usermode = plugin.pConfig.getConfig().getBoolean(ConfPath.Path.AUTOCAST_USERMODE.path, true);
                boolean blacklist = plugin.pConfig.getConfig().getBoolean(ConfPath.Path.AUTOCAST_BLACKLIST.path, true);
                if ("mode".equalsIgnoreCase(params))
                    return usermode ? Msg.AUTOCAST_MODE_USERMODE.msg : (blacklist ? Msg.AUTOCAST_MODE_BLACKLIST.msg : Msg.AUTOCAST_MODE_WHITELIST.msg);
                else if ("desc".equalsIgnoreCase(params))
                    return blacklist ? Msg.AUTOCAST_MODE_BLACKLIST_DESC.msg : Msg.AUTOCAST_MODE_WHITELIST_DESC.msg;
                else if ("content".equalsIgnoreCase(params))
                    return usermode ? Msg.AUTOCAST_MODE_USERMODE_DESC.msg : Arrays.toString(plugin.pConfig.getConfig().getStringList(ConfPath.Path.AUTOCAST_LIST.path).toArray());
                return String.format(Msg.VOTE_VALUE_AUTOCAST.msg, vote.autocast.size());
            case RESULT:
                Vote.Result result = EnumUtil.valueOf(Vote.Result.class, params.toUpperCase());
                if (result == null) return vote.result().name;
                return vote.results.getOrDefault(result, ChatColor.BLUE + result.name);
            case PARTICIPANT:
                Vote.Choice choiceType = EnumUtil.valueOf(Vote.Choice.class, params.toUpperCase());
                if (choiceType != null)
                    return String.valueOf(vote.listParticipants(user -> user.choice == choiceType).size());
                return String.format(Msg.VOTE_VALUE_PARTICIPANT.msg, vote.participants.size());
            case PROCESS:
                return vote.getProcess() + "%";
            case ANONYMOUS:
                if ("desc".equalsIgnoreCase(params))
                    return vote.allowAnonymous ? Msg.VOTE_ANONYMOUS_DESC.msg : "";
                return vote.allowAnonymous ? Msg.VOTE_ANONYMOUS_ENABLE.msg : Msg.VOTE_ANONYMOUS_DISABLE.msg;
            case PUBLIC:
                return vote.isPublic ? Msg.VOTE_PUBLIC_ENABLE.msg : Msg.VOTE_PUBLIC_DISABLE.msg;
            case EDITABLE:
                return vote.allowEdit ? Msg.VOTE_EDITABLE_ENABLE.msg : Msg.VOTE_EDITABLE_DISABLE.msg;
            default:
                return Msg.ERROR_PLACEHOLDER_REQUEST.msg;
        }
    }

    public static List<String> parse(Vote vote, List<String> lore) {
        List<String> result = new ArrayList<>();
        for (String text : lore) {
            result.add(parse(vote, text));
            if (text.contains("%DESCRIPTION%") || text.contains("%AUTOCAST%")) {
                boolean isDescription = text.contains("%DESCRIPTION%");
                List<String> content = new ArrayList<>();
                (isDescription ? vote.description : vote.autocast).forEach(
                        line -> content.add(I18n.color(text.substring(0, text.indexOf("%")) + (isDescription ? "" : "/") + line))
                );
                result.addAll(content);
            }
        }
        return result;
    }

    public static ItemStack applyPlaceholder(ItemStack item, Vote vote) {
        ItemStack result = item.clone();
        ItemMeta meta = result.getItemMeta();
        if (meta == null) return result;
        meta.setDisplayName(parse(vote, meta.getDisplayName()));
        if (meta.getLore() != null) meta.setLore(parse(vote, meta.getLore()));
        result.setItemMeta(meta);
        return result;
    }
}
