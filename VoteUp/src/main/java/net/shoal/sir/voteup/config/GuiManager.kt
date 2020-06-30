package net.shoal.sir.voteup.config

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.config.PFolder
import org.serverct.parrot.parrotx.utils.BasicUtil
import java.io.File
import java.util.*

class GuiManager : PFolder(PPlugin.getInstance(), "Guis", "Gui 配置文件夹") {
    private val guiMap: MutableMap<String, FileConfiguration> = HashMap()
    override fun load(@NonNull file: File) {
        guiMap[BasicUtil.getNoExFileName(file.name)] = YamlConfiguration.loadConfiguration(file)
    }

    override fun releaseDefaultData() {
        for (key in GuiKey.values()) if (!File(folder, key.filename + ".yml").exists()) plugin.saveResource("Guis/" + key.filename + ".yml", false)
    }

    operator fun get(filename: String?): FileConfiguration {
        return guiMap.getOrDefault(filename, null)
    }

    enum class GuiKey(val filename: String, val guiname: String) {
        MAIN_MENU("MainMenu", "VoteUp 主界面"), VOTE_CREATE("VoteCreate", "投票创建菜单"), VOTE_LIST("VoteList", "投票列表"), VOTE_DETAILS("VoteDetails", "投票详细信息界面"), VOTE_PARTICIPANTS("VoteParticipants", "投票参与者列表");

    }
}