package net.shoal.sir.voteup.config;

import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.enums.CacheLogType;
import net.shoal.sir.voteup.enums.VoteUpPerm;
import net.shoal.sir.voteup.util.ChatAPIUtil;
import net.shoal.sir.voteup.util.PlaceholderUtil;
import net.shoal.sir.voteup.util.TimeUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private final File dataFile = new File(VoteUp.getInstance().getDataFolder() + File.separator + "cache.yml");
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

    public void log(CacheLogType type, String voteID, String playerName) {
        ConfigurationSection section = data.getConfigurationSection(type.toString());
        if(section == null) {
            section = data.createSection(type.toString());
        }

        switch (type) {
            case VOTE_END:
                section.set(voteID, System.currentTimeMillis());
                break;
            case VOTE_VOTED:
                section.set(voteID + "." + playerName, System.currentTimeMillis());
                break;
            default:
                break;
        }

        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void report(CacheLogType type, Player user) {
        ConfigurationSection section = data.getConfigurationSection(type.toString());
        if(section != null) {
            TextComponent text = ChatAPIUtil.build(PlaceholderUtil.check(plugin.lang.getMessage(plugin.localeKey, I18n.Type.INFO, "Vote", "Report." + type.toString()), null));
            StringBuilder hover = new StringBuilder();
            List<String> voteIDList = new ArrayList<>(section.getKeys(true));

            if (voteIDList.isEmpty()) {
                return;
            }

            List<String> targetVoteList = new ArrayList<>();
            for(String voteID : voteIDList) {
                String[] dataSet = voteID.split("_");
                if (dataSet.length < 2) {
                    continue;
                }
                if (user.getName().equals(dataSet[0])) {
                    targetVoteList.add(voteID);
                }
            }

            if (targetVoteList.isEmpty()) {
                return;
            }

            switch (type) {
                case VOTE_END:
                    /*
                    VOTE_END:
                      EntityParrot_:
                        1: 记录时间
                     */
                    if(user.hasPermission(VoteUpPerm.NOTICE.perm())) {
                        for (int index = 0; index < voteIDList.size(); index++) {
                            String key = voteIDList.get(index);
                            Vote targetVote = VoteManager.getInstance().getVote(key);
                            if (targetVote != null) {
                                hover.append(PlaceholderUtil.check(
                                        "&a▶ &7%TITLE%&7(发起人: &a%STARTER%&7) &9-> &c%RESULT%&7(&c%LogTime%&7)"
                                                .replace("%LogTime%", TimeUtil.getDescriptiveTime(section.getLong(key))),
                                        targetVote
                                ));
                                if (!(index == voteIDList.size() - 1)) {
                                    hover.append("\n");
                                }
                            }
                        }
                        data.set(type.toString(), null);
                        break;
                    }

                    for (int index = 0; index < targetVoteList.size(); index++) {
                        String key = targetVoteList.get(index);
                        Vote targetVote = VoteManager.getInstance().getVote(key);
                        if (targetVote != null) {
                            hover.append(PlaceholderUtil.check(
                                    "&a▶ &7%TITLE% &9-> &c%RESULT%&7(&c%LogTime%&7)"
                                            .replace("%LogTime%", TimeUtil.getDescriptiveTime(section.getLong(key))),
                                    targetVote
                            ));
                            if (!(index == voteIDList.size() - 1)) {
                                hover.append("\n");
                            }
                        }
                        section.set(key, null);
                    }
                    break;

                case VOTE_VOTED:
                    /*
                    VOTE_VOTED:
                      EntityParrot_:
                        1:
                          cat: 记录时间
                     */
                    for(String voteID : targetVoteList) {
                        Vote targetVote = VoteManager.getInstance().getVote(voteID);
                        ConfigurationSection targetLogSection = section.getConfigurationSection(voteID);

                        if(targetVote != null) {
                            hover.append(CommonUtil.color("&7投票: &c" + targetVote.getTitle() + "\n"));

                            if(targetLogSection != null) {
                                List<String> logPlayerList = new ArrayList<>(targetLogSection.getKeys(false));
                                for (int index = 0; index < logPlayerList.size(); index++) {
                                    hover.append(PlaceholderUtil.check(
                                            "&b▶ &c%Voter%&7投了&c%Choice%&7一票(&a%LogTime%&7) &9-> &7&o%Reason%"
                                                    .replace("%LogTime%", TimeUtil.getDescriptiveTime(targetLogSection.getLong(voteID)))
                                                    .replace("%Voter%", logPlayerList.get(index))
                                                    .replace("%Choice%", targetVote.getChoices().getChoice(VoteManager.getInstance().getChoice(voteID, user.getName())))
                                                    .replace("%Reason%", VoteManager.getInstance().getReason(voteID, user.getName()))
                                            ,
                                            targetVote
                                    ));
                                    if (index != voteIDList.size() - 1) {
                                        hover.append("\n");
                                    }
                                }
                            }
                        }
                        section.set(voteID, null);
                    }
                    break;
                default:
                    break;
            }

            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(CommonUtil.color(hover.toString()))));
            user.spigot().sendMessage(text);
        }

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
        return -1;
    }

}
