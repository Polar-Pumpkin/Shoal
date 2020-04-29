package net.shoal.sir.voteup.api;

import net.shoal.sir.voteup.config.VoteManager;

public class VoteUpAPI {

    public static final SoundUtil SOUND;
    public static final VoteManager VOTE_MANAGER;

    static {
        SOUND = new SoundUtil();
        VOTE_MANAGER = new VoteManager();
    }
}
