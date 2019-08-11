package net.shoal.sir.voteup.config;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Vote;
import net.shoal.sir.voteup.data.VoteChoice;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.enums.VoteDataType;
import net.shoal.sir.voteup.enums.VoteType;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class VoteManager {

    private static LocaleUtil locale;
    private static VoteManager instance;
    public static VoteManager getInstance() {
        if(instance == null) {
            instance = new VoteManager();
        }
        locale = VoteUp.getInstance().getLocale();
        return instance;
    }

    private File dataFolder = new File(VoteUp.getInstance().getDataFolder() + File.separator + "Votes");

    private Map<String, Vote> voteMap = new HashMap<>();
    private Map<String, Map<VoteDataType, Object>> creatingVoteMap = new HashMap<>();

    public void load() {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
            Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7未找到投票记录文件夹, 已自动生成."));
        } else {
            File[] votes = dataFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));

            if(votes != null && votes.length > 1) {
                for(File dataFile : votes) {
                    FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

                    List<String> desc = new ArrayList<>();
                    for(String text : data.getStringList("Description")) {
                        desc.add(CommonUtil.color(text));
                    }

                    Map<ChoiceType, Map<String, String>> participant = new HashMap<>();
                    if(data.isConfigurationSection("Participant")) {
                        Map<String, String> choiceParticipant = new HashMap<>();
                        for(String choice : data.getConfigurationSection("Participant").getKeys(false)) {
                            ChoiceType choiceType = ChoiceType.valueOf(choice.toUpperCase());
                            ConfigurationSection choiceSection = data.getConfigurationSection("Participant." + choice);
                            for(String playerName : choiceSection.getKeys(false)) {
                                choiceParticipant = new HashMap<>();
                                choiceParticipant.put(playerName, CommonUtil.color(Objects.requireNonNull(choiceSection.getString(playerName))));
                            }
                            participant.put(choiceType, choiceParticipant);
                        }
                    }

                    voteMap.put(
                            CommonUtil.getNoExFileName(dataFile.getName()),
                            new Vote(
                                    CommonUtil.getNoExFileName(dataFile.getName()),
                                    data.getBoolean("Status"),
                                    VoteType.valueOf(Objects.requireNonNull(data.getString("Type")).toUpperCase()),
                                    data.getInt("Amount"),
                                    CommonUtil.color(Objects.requireNonNull(data.getString("Title"))),
                                    desc,
                                    new VoteChoice(data.getConfigurationSection("Choice")),
                                    data.getString("Starter"),
                                    data.getLong("StartTime"),
                                    data.getString("Duration"),
                                    participant,
                                    data.getStringList("AutoCast")
                            )
                    );
                }
                Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7共加载 &c" + voteMap.size() + " &7项投票记录."));
            } else {
                Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7没有投票纪录可供加载."));
            }
        }
    }

    public Vote getVote(String id) {
        if(voteMap.containsKey(id)) {
            return voteMap.get(id);
        }
        return null;
    }

    public Vote startCreateVote(String starter) {
        Map<VoteDataType, Object> newVote = new HashMap<>();
        String id = starter + "." + (voteMap.size() + 1);

        newVote.put(VoteDataType.ID, id);
        newVote.put(VoteDataType.STATUS, true);
        newVote.put(VoteDataType.TYPE, VoteType.NORMAL);
        newVote.put(VoteDataType.AMOUNT, 0);
        newVote.put(VoteDataType.TITLE, starter + "的投票");
        newVote.put(VoteDataType.DESCRIPTION, new ArrayList<>());
        newVote.put(VoteDataType.CHOICE, new VoteChoice((ConfigurationSection) null));
        newVote.put(VoteDataType.STARTER, starter);
        newVote.put(VoteDataType.STARTTIME, System.currentTimeMillis());
        newVote.put(VoteDataType.DURATION, "1d");
        newVote.put(VoteDataType.AUTOCAST, new ArrayList<>());

        creatingVoteMap.put(starter, newVote);
        return new Vote(newVote);
    }

    public boolean setCreatingVoteData(String playerName, VoteDataType type, Object value) {
        locale.debug("&7调用 setCreatingVoteData 方法, 目标玩家: &c" + playerName);
        if(creatingVoteMap.containsKey(playerName)) {
            locale.debug("&7目标玩家存在待发布的投票.");
            Map<VoteDataType, Object> voteData = creatingVoteMap.get(playerName);
            voteData.put(type, value);
            locale.debug("&7已覆盖值: &c" + type.toString() + " &9-> " + value);
            creatingVoteMap.put(playerName, voteData);
            CommonUtil.message(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7设置" + type.getName() + "成功: &c" + value.toString()), playerName);
            SoundManager.getInstance().success(playerName);
            return true;
        }
        CommonUtil.message(locale.buildMessage(VoteUp.LOCALE, MessageType.ERROR, "&7设置" + type.getName() + "失败, 您的 ID 下没有待发布的投票."), playerName);
        SoundManager.getInstance().fail(playerName);
        locale.debug("&7设置值失败, 目标玩家不存在待发布的投票, 操作无效.");
        return false;
    }

    public Vote getCreatingVote(String playerName) {
        if(creatingVoteMap.containsKey(playerName)) {
            return new Vote(creatingVoteMap.get(playerName));
        }
        return null;
    }

    public void save(Map<VoteDataType, Object> data) {
        String id = (String) data.get(VoteDataType.ID);
        voteMap.put(id, new Vote(data));
        creatingVoteMap.remove(id);
        saveToFile(data);
    }

    public void saveToFile(Map<VoteDataType, Object> data) {
        String id = (String) data.get(VoteDataType.ID);
        File dataFile = new File(dataFolder.getAbsolutePath() + File.separator + id + ".yml");
        FileConfiguration voteData = YamlConfiguration.loadConfiguration(dataFile);

        voteData.set("Status", data.get(VoteDataType.STATUS));
        voteData.set("Type", data.get(VoteDataType.TYPE).toString());
        voteData.set("Amount", data.get(VoteDataType.AMOUNT));
        voteData.set("Title", ((String) data.get(VoteDataType.TITLE)).replace("§", "&"));

        List<String> desc = (List<String>) data.get(VoteDataType.DESCRIPTION);
        desc.replaceAll(s -> s.replace("§", "&"));
        voteData.set("Description", desc);

        Map<ChoiceType, String> choices = ((VoteChoice) data.get(VoteDataType.CHOICE)).getChoices();
        for(ChoiceType key : choices.keySet()) {
            voteData.set("Choice." + key.toString(), choices.get(key).replace("§", "&"));
        }

        voteData.set("Starter", data.get(VoteDataType.STARTER));
        voteData.set("StartTime", data.get(VoteDataType.STARTTIME));
        voteData.set("Duration", data.get(VoteDataType.DURATION));

        List<String> casts = (List<String>) data.get(VoteDataType.AUTOCAST);
        casts.replaceAll(s -> s.replace("§", "&"));
        voteData.set("AutoCast", casts);

        try {
            voteData.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
