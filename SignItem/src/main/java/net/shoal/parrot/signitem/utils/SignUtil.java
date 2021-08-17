package net.shoal.parrot.signitem.utils;

import net.shoal.parrot.signitem.SignItem;
import net.shoal.parrot.signitem.config.ConfigManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.serverct.parrot.parrotx.utils.i18n.I18n;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        final ItemStack item = new ItemStack(Material.OAK_SIGN);
        final ItemMeta meta = item.getItemMeta();
        meta.setLore(sign);

        return meta.getLore();
    }

    public static ItemStack sign(final ItemStack item, final Map<String, String> content) {
        if (Objects.isNull(item)) {
            return null;
        }
        final ItemMeta meta =
                Optional.ofNullable(item.getItemMeta()).orElse(Bukkit.getItemFactory().getItemMeta(item.getType()));
        final List<String> lores = new ArrayList<>(Optional.ofNullable(meta.getLore()).orElse(new ArrayList<>()));

        final Map<String, String> existSign = getSign(item);
        final List<String> oldSign = generateSign(existSign);
        final List<String> newSign = generateSign(content);

        if (newSign.isEmpty()) {
            lores.removeAll(oldSign);
            meta.getPersistentDataContainer().remove(SignItem.DESC);
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

    @NotNull
    public static Map<String, String> getSign(final ItemStack item) {
        final Map<String, String> result = new HashMap<>();
        if (Objects.isNull(item) || Objects.isNull(item.getItemMeta())) {
            return result;
        }

        final List<String> lores =
                new ArrayList<>(Optional.ofNullable(item.getItemMeta().getLore()).orElse(new ArrayList<>()));
        int lastMatchIndex = -1;
        identify_Loop:
        for (String identify : ConfigManager.identifies) {
            identify = I18n.color(identify);
            final Matcher keywordMatcher = KEY_WORD_PATTERN.matcher(identify);
            final boolean hasVariable = keywordMatcher.find();

            String keyword = null;
            String prefix = null;
            Pattern pattern = null;
            if (hasVariable) {
                keyword = keywordMatcher.group(1);
                final String placeholder = "{" + keyword + "}";
                prefix = identify.substring(0, identify.indexOf(placeholder));
                pattern = Pattern.compile(identify.replace(placeholder, "(.*)"));
            }

            if (lastMatchIndex != -1) {
                if (lastMatchIndex + 1 >= lores.size()) {
                    break;
                }
                String lore = lores.get(lastMatchIndex + 1);
                if (Objects.nonNull(prefix) && !lore.startsWith(prefix)) {
                    lore = complete(lore, prefix);
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

        final ItemMeta meta = item.getItemMeta();
        if (Objects.nonNull(meta)) {
            final String desc = meta.getPersistentDataContainer().get(SignItem.DESC, PersistentDataType.STRING);
            if (!StringUtils.isEmpty(desc)) {
                result.put("desc", desc);
            }
        }
        return result;
    }

    private static String complete(final String content, final String template) {
        final Map<Integer, Integer> similarities = new HashMap<>();

        for (int contentIndex = 0; contentIndex < content.length(); contentIndex++) {
            final char c = content.charAt(contentIndex);
            if (!template.startsWith(String.valueOf(c))) {
                continue;
            }

            int similarity = 1;
            for (int templateIndex = 1; templateIndex < template.length(); templateIndex++) {
                final char contentChar = content.charAt(contentIndex + templateIndex);
                final char templateChar = template.charAt(templateIndex);
                if (contentChar != templateChar) {
                    similarities.put(contentIndex, similarity);
                    break;
                }
                similarity++;
            }
        }

        final List<Map.Entry<Integer, Integer>> collect = similarities.entrySet().stream()
                .sorted(Comparator.comparingInt(Map.Entry::getValue))
                .collect(Collectors.toList());
        Collections.reverse(collect);
        final int start = collect.get(0).getKey();

        final StringBuilder builder = new StringBuilder(content);
        char lastSimilarChar = 'a';
        for (int index = start; index - start < template.length(); index++) {
            final char contentChar = builder.toString().charAt(index);
            final char templateChar = template.charAt(index - start);
            if (contentChar == templateChar) {
                lastSimilarChar = templateChar;
                continue;
            }

            builder.insert(index, templateChar);
        }
        if (lastSimilarChar == '&' || lastSimilarChar == '§') {
            builder.insert(start + template.length(), lastSimilarChar);
        }
        return builder.toString();
    }

}
