package net.shoal.sir.voteup.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.util.CommonUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

public @Data @AllArgsConstructor class VoteChoice {

    private Map<Vote.Choice, String> choices;

    public VoteChoice(ConfigurationSection section) {
        choices = new HashMap<>();
        if(section != null) {
            for(String key : section.getKeys(false)) {
                choices.put(ChoiceType.valueOf(key.toUpperCase()), CommonUtil.color(section.getString(key)));
            }
        } else {
            choices.put(ChoiceType.ACCEPT, CommonUtil.color("&a&l" + ChoiceType.ACCEPT.getName()));
            choices.put(ChoiceType.NEUTRAL, CommonUtil.color("&e&l" + ChoiceType.NEUTRAL.getName()));
            choices.put(ChoiceType.REFUSE, CommonUtil.color("&c&l" + ChoiceType.REFUSE.getName()));
        }
    }

    public String getChoice(ChoiceType type) {
        if(choices.containsKey(type)) {
            return choices.get(type);
        }
        return CommonUtil.color("&7&o未设置此选项内容.");
    }

}
