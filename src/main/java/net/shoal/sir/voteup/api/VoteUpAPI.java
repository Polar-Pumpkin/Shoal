package net.shoal.sir.voteup.api;

import lombok.Getter;

public class VoteUpAPI {

    private static VoteUpAPI api;

    public static VoteUpAPI get() {
        if (api == null) api = new VoteUpAPI();
        return api;
    }

    @Getter
    private final SoundUtil soundTool;

    public VoteUpAPI() {
        this.soundTool = new SoundUtil();
    }
}
