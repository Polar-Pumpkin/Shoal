package net.shoal.sir.voteup.config;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.CacheLogType;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.util.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class CacheManager {

    private static LocaleUtil locale;
    private static CacheManager instance;
    public static CacheManager getInstance() {
        if(instance == null) {
            instance = new CacheManager();
        }
        locale = VoteUp.getInstance().getLocale();
        return instance;
    }

    private File dataFile = new File(VoteUp.getInstance().getDataFolder() + File.separator + "cache.yml");
    private FileConfiguration data;

    public void load() {
        if(!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        data = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void log(CacheLogType type, String voteID) {
        ConfigurationSection section = data.getConfigurationSection(type.toString());
        if(section == null) {
            section = data.createSection(type.toString());
        }
        section.set(voteID, System.currentTimeMillis());
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void report(Player user) {
        ConfigurationSection section = data.getConfigurationSection(CacheLogType.VOTE_END.toString());
        if(section != null) {
            TextComponent text = ChatAPIUtil.build(PlaceholderUtil.check(locale.getMessage(VoteUp.LOCALE, MessageType.INFO, "Vote", "Report"), null));
            StringBuilder hover = new StringBuilder();
            for(String key : section.getKeys(true)) {
                Vote targetVote = VoteManager.getInstance().getVote(key);
                if(targetVote != null) {
                    hover.append(PlaceholderUtil.check(
                            "&a▶ &7%TITLE%&7(发起人: &a%STARTER%&7) &9-> &c%RESULT%&7(&c%LOGTIME%&7)\n"
                                    .replace("%LOGTIME%", TimeUtil.getDescriptiveTime(section.getLong(key))),
                            targetVote
                    ));
                }
            }
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color(hover.toString()))));
            user.spigot().sendMessage(text);
        }
        data.set(CacheLogType.VOTE_END.toString(), null);
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLogAmount(CacheLogType type) {
        ConfigurationSection section = data.getConfigurationSection(type.toString());
        if(section != null) {
            return section.getKeys(true).size();
        }
        return 0;
    }

}
