package net.shoal.sir.voteup.util

import net.shoal.sir.voteup.VoteUp
import net.shoal.sir.voteup.listener.AsyncPlayerChatListener
import net.shoal.sir.voteup.listener.InventoryClickListener
import org.bukkit.Bukkit
import org.bukkit.event.Listener

object ListenerManager {
    private fun register(listener: Listener) {
        Bukkit.getPluginManager().registerEvents(listener, VoteUp.instance)
    }

    fun register() {
        val listeners = arrayOf(
                AsyncPlayerChatListener(), InventoryClickListener()
        )
        listeners.forEach(::register)
    }
}