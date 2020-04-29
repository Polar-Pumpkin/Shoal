package net.shoal.sir.voteup.util;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.CacheManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.CacheLogType;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.Placeholder;
import net.shoal.sir.voteup.enums.VoteDataType;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlaceholderUtil {

    private static final LocaleUtil locale = VoteUp.getInstance().getLocale();

    public static String parse(Placeholder type, Vote data) {
        String result = "";

        switch (type) {
            case ID:
                result = data.getId();
                break;
            case STATUS:
                result = data.isStatus() ? "&a&l进行中" : "&c&l已关闭";
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
            case AMOUNT:
                result = String.valueOf(data.getAmount());
                break;
            case TITLE:
                result = data.getTitle();
                break;
            case DESCRIPTION:
                if(!data.getDescription().isEmpty()) {
                    result = "暂不支持此类信息显示.";
                } else {
                    result = "无.";
                }
                break;
            case CHOICE_ACCEPT:
            case CHOICE_NEUTRAL:
            case CHOICE_REFUSE:
                result = data.getChoices().getChoice(ChoiceType.valueOf(type.toString().replace("CHOICE_", "")));
                break;
            case CHOICE_ACCEPT_NAME:
            case CHOICE_NEUTRAL_NAME:
            case CHOICE_REFUSE_NAME:
                result = ChoiceType.valueOf(
                        type.toString()
                                .replace("CHOICE_", "")
                                .replace("_NAME", "")
                ).getName();
                break;
            case STARTER:
                result = data.getStarter();
                break;
            case STARTTIME:
                result = TimeUtil.getDescriptiveTime(data.getStartTime());
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
            case PARTICIPANT_ACCEPT_AMOUNT:
            case PARTICIPANT_NEUTRAL_AMOUNT:
            case PARTICIPANT_REFUSE_AMOUNT:
                result = String.valueOf(
                        data.getParticipant().get(
                                ChoiceType.valueOf(
                                        type.toString()
                                                .replace("PARTICIPANT_", "")
                                                .replace("_AMOUNT", "")
                                )
                        ).size()
                );
                break;
            case ID_NAME:
            case STATUS_NAME:
            case TYPE_NAME:
            case AMOUNT_NAME:
            case TITLE_NAME:
            case DESCRIPTION_NAME:
            case CHOICE_NAME:
            case STARTER_NAME:
            case STARTTIME_NAME:
            case DURATION_NAME:
            case PARTICIPANT_NAME:
            case AUTOCAST_NAME:
                result = VoteDataType.valueOf(type.toString().replace("_NAME", "")).getName();
                break;

            case RESULT:
                result = VoteManager.getInstance().isPassed(data.getId()) ? data.getPass() : data.getReject();
                break;
            case RESULT_NAME:
                result = "投票结果显示内容";
                break;
            case RESULT_PASS:
                result = data.getPass();
                break;
            case RESULT_PASS_NAME:
                result = VoteDataType.PASS.getName();
                break;
            case RESULT_REJECT:
                result = data.getReject();
                break;
            case RESULT_REJECT_NAME:
                result = VoteDataType.REJECT.getName();
                break;

            case CACHE_LOG_AMOUNT_VOTE_END:
                result = String.valueOf(CacheManager.getInstance().getLogAmount(CacheLogType.valueOf(type.toString().replace("CACHE_LOG_AMOUNT_", ""))));
                break;
            default:
                result = "读取数据遇到错误.(未知变量)";
        }

//        plugin.lang.debug("&7目标变量值: &c" + result);
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    public static String check(String text, Vote data) {
        String result = text;
//        plugin.lang.debug("&7调用 (PlaceholderUtil) check 方法, 目标文本: &c" + result);
        for(Placeholder placeholder : Placeholder.values()) {
            String placeholderName = "%" + placeholder.toString() + "%";
            if(result.contains(placeholderName)) {
//                plugin.lang.debug("&7目标文本包含变量: &7" + placeholderName);
                String placeholderResult = parse(placeholder, data);

                result = result.replace(placeholderName, placeholderResult);
            }
        }
//        plugin.lang.debug("&7最终返回文本: &c" + result);
        return result;
    }

    public static List<String> checkAll(List<String> list, Vote data) {
        List<String> result = new ArrayList<>();
        for(String text : list) {
            if(text.contains("%DESCRIPTION%") || text.contains("%AUTOCAST%")) {
                result.add(text);

                boolean isDescription;
                if(text.contains("%DESCRIPTION%")) {
                    isDescription = true;
                } else if(text.contains("%AUTOCAST%")) {
                    isDescription = false;
                } else {
                    result.add(check(text, data));
                    continue;
                }

                int targetIndex = result.indexOf(text);
                String prefix = isDescription ? "&b▶ &7&o" : "";
                boolean isFirstLine = true;
                List<String> content  = isDescription ? data.getDescription() : data.getAutoCast();

                for(int index = content.size() - 1; index >= 0; index--) {
                    if(isFirstLine) {
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
