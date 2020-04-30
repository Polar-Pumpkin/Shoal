package net.shoal.sir.voteup.api;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.BuiltinMsg;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.serverct.parrot.parrotx.PPlugin;
import org.serverct.parrot.parrotx.utils.EnumUtil;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VoteUpPlaceholder {

    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("[%]([^%]+)[%]");

    public static String parse(Vote vote, String text, Pattern pattern) {
        if (text == null) return null;

        Matcher m = pattern.matcher(text);

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
        if (vote == null) return BuiltinMsg.ERROR_PLACEHOLDER_REQUEST.msg;
        if (params.equalsIgnoreCase("display")) return identifier.name;
        PPlugin plugin = VoteUp.getInstance();

        switch (identifier) {
            case ID:
                return vote.voteID;
            case STATUS:
                return plugin.lang.getRaw(plugin.localeKey, "Vote", "Status." + (vote.status ? "Processing" : "End"));
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
                return String.format(BuiltinMsg.VOTE_VALUE_DESCRIPTION.msg, vote.description.size());
            case CHOICE:
                Vote.Choice choice = EnumUtil.valueOf(Vote.Choice.class, params.toUpperCase());
                if (choice == null) return BuiltinMsg.ERROR_PLACEHOLDER_REQUEST.msg;
                return vote.choices.getOrDefault(choice, ChatColor.BLUE + choice.name);
            case AUTOCAST:
                return String.format(BuiltinMsg.VOTE_VALUE_AUTOCAST.msg, vote.autocast.size());
            case RESULT:
                Vote.Result result = EnumUtil.valueOf(Vote.Result.class, params.toUpperCase());
                if (result == null) return BuiltinMsg.ERROR_PLACEHOLDER_REQUEST.msg;
                return vote.results.getOrDefault(result, ChatColor.BLUE + result.name);
            case PARTICIPANT:
                return String.format(BuiltinMsg.VOTE_VALUE_PARTICIPANT.msg, vote.autocast.size());
            default:
                return BuiltinMsg.ERROR_PLACEHOLDER_REQUEST.msg;
        }
    }

    public static List<String> parse(List<String> list, Vote data) {
        List<String> result = new ArrayList<>();
        for (String text : list) {
            if (text.contains("%DESCRIPTION%") || text.contains("%AUTOCAST%")) {
                result.add(text);

                boolean isDescription;
                if (text.contains("%DESCRIPTION%")) {
                    isDescription = true;
                } else if (text.contains("%AUTOCAST%")) {
                    isDescription = false;
                } else {
                    result.add(check(text, data));
                    continue;
                }

                int targetIndex = result.indexOf(text);
                String prefix = isDescription ? "&b▶ &7&o" : "";
                boolean isFirstLine = true;
                List<String> content = isDescription ? data.getDescription() : data.getAutoCast();

                for (int index = content.size() - 1; index >= 0; index--) {
                    if (isFirstLine) {
                        result.set(targetIndex, CommonUtil.color(prefix + content.get(index)));
                        isFirstLine = false;
                    } else {
                        result.add(targetIndex, CommonUtil.color(prefix + content.get(index)));
                    }
                }
            } else {
                result.add(check(text, data));
            }
        }
        return result;
    }

    public static ItemStack applyPlaceholder(ItemStack item, Vote data) {
        ItemStack result = item.clone();
        if (result.hasItemMeta()) {
            ItemMeta meta = result.getItemMeta();

            if (meta != null) {
                meta.setDisplayName(check(meta.getDisplayName(), data));
                if (meta.hasLore()) {
                    meta.setLore(parse(Objects.requireNonNull(meta.getLore()), data));
                }
                result.setItemMeta(meta);
            }
        }
        return result;
    }
}
