package net.shoal.sir.voteup.util;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.Placeholder;
import net.shoal.sir.voteup.enums.VoteDataType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaceholderUtil {

    private static LocaleUtil locale = VoteUp.getInstance().getLocale();

    public static String parse(Placeholder type, Vote data) {
        String result = "";

        switch (type) {
            case ID:
                result = data.getId();
                break;
            case TYPE:
                switch (data.getType()) {
                    case NORMAL:
                        result = data.getType().toString();
                        break;
                    case REACHAMOUNT:
                        result = data.getType().toString() + "&7(&c" + data.getAmount() + "&7)";
                        break;
                    default:
                        result = "读取数据遇到错误.(未知投票种类)";
                }
                break;
            case TITLE:
                result = data.getTitle();
                break;
            case STARTER:
                result = data.getStarter();
                break;
            case AUTOCAST:
                if(!data.getAutoCast().isEmpty()) {
                    result = "暂不支持此类信息显示.";
                } else {
                    result = "无.";
                }
                break;
            case DURATION:
                result = TimeUtil.getDescriptiveDuration(data.getDuration());
                break;
            case DESCRIPTION:
                if(!data.getDescription().isEmpty()) {
                    result = "暂不支持此类信息显示.";
                } else {
                    result = "无.";
                }
                break;
            case ID_NAME:
            case TYPE_NAME:
            case TITLE_NAME:
            case STARTER_NAME:
            case AUTOCAST_NAME:
            case DURATION_NAME:
            case DESCRIPTION_NAME:
                result = VoteDataType.valueOf(type.toString().replace("_NAME", "")).getName();
                break;
            default:
                result = "读取数据遇到错误.(未知变量)";
        }

        locale.debug("&7目标变量值: &c" + result);
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static String check(String text, Vote data) {
        String result = text;
        locale.debug("&7调用 (PlaceholderUtil) check 方法, 目标文本: &c" + result);
        for(Placeholder placeholder : Placeholder.values()) {
            String placeholderName = "%" + placeholder.toString() + "%";
            if(result.contains(placeholderName)) {
                locale.debug("&7目标文本包含变量: &7" + placeholderName);
                String placeholderResult = parse(placeholder, data);

                result = result.replace(placeholderName, placeholderResult);
            }
        }
        locale.debug("&7最终返回文本: &c" + result);
        return result;
    }

    public static List<String> checkAll(List<String> list, Vote data) {
        List<String> result = new ArrayList<>();
        for(String text : list) {
            result.add(check(text, data));
        }
        return result;
    }

    public static ItemStack applyPlaceholder(ItemStack item, Vote data) {
        ItemStack result = item.clone();
        if(result.hasItemMeta()) {
            ItemMeta meta = result.getItemMeta();

            if(meta != null) {
                meta.setDisplayName(check(meta.getDisplayName(), data));
                if(meta.hasLore()) {
                    meta.setLore(checkAll(Objects.requireNonNull(meta.getLore()), data));
                }
                result.setItemMeta(meta);
            }
        }
        return result;
    }
}
