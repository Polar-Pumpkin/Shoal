package net.shoal.sir.voteup.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class AsyncPlayerChatListener : Listener {
    @EventHandler
    fun onChat(e : AsyncPlayerChatEvent) {

    }

    enum class Type {
        SET_TITLE,
        MODIFY_DESCRIPTION,
        SET_DURATION,
        SET_CHOICE,
        MODIFY_AUTOCAST,
        SET_RESULT
    }
}