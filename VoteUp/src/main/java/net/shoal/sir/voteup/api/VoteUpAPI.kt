package net.shoal.sir.voteup.api

import net.shoal.sir.voteup.config.CacheManager
import net.shoal.sir.voteup.config.GuiManager
import net.shoal.sir.voteup.config.VoteManager

object VoteUpAPI {
    var SOUND: SoundUtil? = null
    var VOTE_MANAGER: VoteManager? = null
    var GUI_MANAGER: GuiManager? = null
    var CACHE_MANAGER: CacheManager? = null

    init {
        SOUND = SoundUtil()
        VOTE_MANAGER = VoteManager()
        GUI_MANAGER = GuiManager()
        CACHE_MANAGER = CacheManager()
    }
}