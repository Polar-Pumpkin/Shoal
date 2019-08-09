package net.shoal.sir.voteup.enums;

public enum VoteType {
    NORMAL("在投票结束时同意人数要大于反对人数."),
    REACHAMOUNT("在投票结束前同意人数需达到指定数量.");

    private final String desc;
    VoteType(String type) {
        this.desc = type;
    }

    public String getDesc() {
        return desc;
    }
}
