package net.shoal.sir.voteup.files

import net.shoal.sir.voteup.VoteUp
import org.bukkit.configuration.file.YamlConfiguration
import org.sct.easylib.util.BasicUtil
import java.io.File

/**
 * @param filePath 包含文件名的路径(相对于插件文件夹)
 * @param fileName 文件名(含后缀)
 */
abstract class AbstractFile(private val filePath: File) {
    lateinit var config: YamlConfiguration
    var file: File = File("${VoteUp.instance.dataFolder}${File.separator}${filePath.path}")
    val fileName = filePath.name

    init {
        load()
    }

    fun load() {
        if (!file.exists()) {
            VoteUp.instance.saveResource(fileName, false)
        }
        config = YamlConfiguration.loadConfiguration(file)
    }

    fun getString(path : String): String {
        load()
        return config.getString(path) ?: "Empty String"
    }

    fun getStringList(path : String): List<String> {
        load()
        return BasicUtil.convert(config.getStringList(path))
    }

    fun getDouble(path : String): Double {
        load()
        return config.getDouble(path)
    }

    fun getInt(path : String): Int {
        return getDouble(path).toInt()
    }

    fun getFloat(path : String): Float {
        return config.getDouble(path).toFloat()
    }
}