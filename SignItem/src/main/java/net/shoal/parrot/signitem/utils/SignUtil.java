package net.shoal.parrot.signitem.utils;

import net.shoal.parrot.signitem.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUtil {

    private final static Pattern KEY_WORD_PATTERN = Pattern.compile("\\{(.*)}");

    public static List<String> generateSign(final Map<String, String> content) {
        final List<String> sign = new ArrayList<>();
        if (Objects.isNull(content) || content.isEmpty()) {
            return sign;
        }
        for (String identify : ConfigManager.identifies) {
            for (Map.Entry<String, String> entry : content.entrySet()) {
                String keyword = entry.getKey();
                String value = entry.getValue();
                identify = identify.replace("{" + keyword + "}", value);
            }
            sign.add(identify);
        }
        sign.replaceAll(I18n::color);

        return sign;
    }

    public static ItemStack sign(final ItemStack item, final Map<String, String> content) {
        if (Objects.isNull(item)) {
            return null;
        }
        final ItemMeta meta = Optional.ofNullable(item.getItemMeta()).orElse(Bukkit.getItemFactory().getItemMeta(item.getType()));
        final List<String> lores = new ArrayList<>(Optional.ofNullable(meta.getLore()).orElse(new ArrayList<>()));

        final Map<String, String> existSign = getSign(item);
        final List<String> oldSign = generateSign(existSign);
        final List<String> newSign = generateSign(content);
        if (newSign.isEmpty()) {
            lores.removeAll(oldSign);
        } else {
            if (existSign.isEmpty()) {
                lores.addAll(newSign);
            } else {
                oldSign.forEach(sign -> {
                    final int index = oldSign.indexOf(sign);
                    lores.replaceAll(lore -> {
                        if (lore.equals(sign)) {
                            return newSign.get(index);
                        }
                        return lore;
                    });
                });
            }
        }

        meta.setLore(lores);
        item.setItemMeta(meta);
        return item;
    }

    public static Map<String, String> getSign(final ItemStack item) {
        final Map<String, String> result = new HashMap<>();
        if (Objects.isNull(item) || Objects.isNull(item.getItemMeta())) {
            return result;
        }

        final List<String> lores = new ArrayList<>(Optional.ofNullable(item.getItemMeta().getLore()).orElse(new ArrayList<>()));
        int lastMatchIndex = -1;
        identify_Loop:
        for (String identify : ConfigManager.identifies) {
            identify = I18n.color(identify);
            if (!ConfigManager.strictColor) {
                identify = ChatColor.stripColor(identify);
            }
            final Matcher keywordMatcher = KEY_WORD_PATTERN.matcher(identify);
            final boolean hasVariable = keywordMatcher.find();
            String keyword = null;
            Pattern pattern = null;
            if (hasVariable) {
                keyword = keywordMatcher.group(1);
                pattern = Pattern.compile(identify.replace("{" + keyword + "}", "(.*)"));
            }

            if (lastMatchIndex != -1) {
                if (lastMatchIndex + 1 >= lores.size()) {
                    break;
                }
                String lore = lores.get(lastMatchIndex + 1);
                if (!ConfigManager.strictColor) {
                    lore = ChatColor.stripColor(lore);
                }
                if (hasVariable) {
                    final Matcher loreMatcher = pattern.matcher(lore);
                    if (!loreMatcher.find()) {
                        break;
                    }
                    result.put(keyword, loreMatcher.group(1));
                } else {
                    if (!lore.equals(identify)) {
                        break;
                    }
                }
                lastMatchIndex++;
            } else {
                for (String lore : lores) {
                    final int index = lores.indexOf(lore);
                    if (!ConfigManager.strictColor) {
                        lore = ChatColor.stripColor(lore);
                    }

                    if (hasVariable) {
                        final Matcher loreMatcher = pattern.matcher(lore);
                        if (!loreMatcher.find()) {
                            continue;
                        }
                        result.put(keyword, loreMatcher.group(1));
                    } else {
                        if (!lore.equals(identify)) {
                            continue;
                        }
                    }
                    lastMatchIndex = index;
                    continue identify_Loop;
                }
            }
        }
        return result;
    }

}
