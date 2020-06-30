package net.shoal.sir.voteup

import net.shoal.sir.voteup.commands.SubCommandHandler
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.sct.easylib.EasyLibAPI
import org.sct.easylib.util.plugin.Metrics

class VoteUp : JavaPlugin() {
    override fun onEnable() {
        Metrics(this, 7972)
        instance = this
        Bukkit.getPluginCommand("voteup")!!.setExecutor(SubCommandHandler(instance, "voteup"))
    }

    companion object {
        @JvmStatic
        lateinit var instance: VoteUp
            private set
        @JvmStatic
        lateinit var easyLibAPI: EasyLibAPI
            private set
    }

}