package net.shoal.sir.voteup.api;

public class VoteUpAPI {

    public static final SoundUtil SOUND;
    private static VoteUpAPI api;

    static {
        SOUND = new SoundUtil();
    }

    public static VoteUpAPI get() {
        if (api == null) api = new VoteUpAPI();
        return api;
    }
}
