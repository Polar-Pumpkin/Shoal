package net.shoal.sir.voteup.api

import net.shoal.sir.voteup.config.ConfPath
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.serverct.parrot.parrotx.PPlugin
import org.serverct.parrot.parrotx.utils.EnumUtil
import java.util.*

class SoundUtil {
    private val plugin = PPlugin.getInstance()
    fun success(@NonNull user: Player) {
        play(user.uniqueId, plugin.pConfig.config.getString(ConfPath.Path.SOUND_ACTION_SUCCESS.path, "ENTITY_VILLAGER_YES")!!)
    }

    fun fail(@NonNull user: Player) {
        play(user.uniqueId, plugin.pConfig.config.getString(ConfPath.Path.SOUND_ACTION_FAILURE.path, "BLOCK_ANVIL_DESTROY")!!)
    }

    fun ding(@NonNull user: Player) {
        play(user.uniqueId, plugin.pConfig.config.getString(ConfPath.Path.SOUND_ACTION_START.path, "ENTITY_ARROW_HIT_PLAYER")!!)
    }

    fun voteEvent(isStart: Boolean) {
        Bukkit.getOnlinePlayers().forEach { player: Player ->
            play(player.uniqueId,
                    if (isStart) plugin.pConfig.config.getString(ConfPath.Path.SOUND_VOTE_START.path, "ENTITY_ARROW_HIT_PLAYER")!! else plugin.pConfig.config.getString(ConfPath.Path.SOUND_VOTE_END.path, "BLOCK_ANVIL_HIT")!!
            )
        }
    }

    fun play(uuid: UUID?, sound: String) {
        play(uuid, EnumUtil.valueOf(Sound::class.java, sound.toUpperCase()))
    }

    fun play(uuid: UUID?, sound: Sound?) {
        val user = Bukkit.getPlayer(uuid!!)
        user?.playSound(user.location, sound!!, 1f, 1f)
    }
}