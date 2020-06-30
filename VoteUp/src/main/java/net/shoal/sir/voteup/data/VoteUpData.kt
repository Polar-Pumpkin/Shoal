package net.shoal.sir.voteup.data

import net.shoal.sir.voteup.listener.AsyncPlayerChatListener
import org.bukkit.entity.Player

object VoteUpData {
    val chatType = HashMap<Player, AsyncPlayerChatListener.Type>()
}