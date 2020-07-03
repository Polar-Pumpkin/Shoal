package net.shoal.sir.voteup.config;

import lombok.NonNull;
import net.shoal.sir.voteup.VoteUp;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.serverct.parrot.parrotx.config.PFolder;
import org.serverct.parrot.parrotx.utils.BasicUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GuiManager extends PFolder {

    private final Map<String, FileConfiguration> guiMap = new HashMap<>();

    public GuiManager() {
        super(VoteUp.getInstance(), "Guis", "Gui 配置文件夹");
    }

    @Override
    public void load(@NonNull File file) {
        guiMap.put(BasicUtil.getNoExFileName(file.getName()), YamlConfiguration.loadConfiguration(file));
    }

    @Override
    public void releaseDefaultData() {
        for (GuiKey key : GuiKey.values())
            if (!new File(folder, key.filename + ".yml").exists())
                plugin.saveResource("Guis/" + key.filename + ".yml", false);
    }

    public FileConfiguration get(String filename) {
        return guiMap.get(filename);
    }

    public enum GuiKey {
        MAIN_MENU("MainMenu", "VoteUp 主界面"),
        VOTE_CREATE("VoteCreate", "投票创建菜单"),
        VOTE_LIST("VoteList", "投票列表"),
        VOTE_DETAILS("VoteDetails", "投票详细信息界面"),
        VOTE_PARTICIPANTS("VoteParticipants", "投票参与者列表"),
        ;

        public final String filename;
        public final String guiname;

        GuiKey(String filename, String guiname) {
            this.filename = filename;
            this.guiname = guiname;
        }
    }
}
