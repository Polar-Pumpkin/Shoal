package net.shoal.sir.voteup.api;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.config.CacheManager;
import net.shoal.sir.voteup.config.GuiManager;
import net.shoal.sir.voteup.config.VoteManager;
import net.shoal.sir.voteup.config.VoteUpConfig;

public class VoteUpAPI {
    public static final VoteManager VOTE_MANAGER;
    public static final GuiManager GUI_MANAGER;
    public static final CacheManager CACHE_MANAGER;
    public static final VoteUpConfig CONFIG;

    static {
        VOTE_MANAGER = new VoteManager();
        GUI_MANAGER = new GuiManager();
        CACHE_MANAGER = new CacheManager();
        CONFIG = (VoteUpConfig) VoteUp.getInstance().pConfig;
    }
}
