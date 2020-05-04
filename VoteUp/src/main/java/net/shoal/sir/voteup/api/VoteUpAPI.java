package net.shoal.sir.voteup.api;

import net.shoal.sir.voteup.config.CacheManager;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;

public class VoteUpAPI {

    public static final SoundUtil SOUND;
    public static final VoteManager VOTE_MANAGER;
    public static final GuiManager GUI_MANAGER;
    public static final CacheManager CACHE_MANAGER;

    static {
        SOUND = new SoundUtil();
        VOTE_MANAGER = new VoteManager();
        GUI_MANAGER = new GuiManager();
        CACHE_MANAGER = new CacheManager();
    }
}
