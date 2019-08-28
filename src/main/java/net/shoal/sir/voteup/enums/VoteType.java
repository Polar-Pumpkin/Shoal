package net.shoal.sir.voteup.enums;

public enum VoteType {
    NORMAL("投票结束时同意人数大于反对人数."),
    REACHAMOUNT("投票结束前同意人数需达到指定数量."),
    LEASTNOT("投票结束前反对人数不超过指定数量.");

    private final String desc;
    VoteType(String type) {
        this.desc = type;
    }

    public String getDesc() {
        return desc;
    }
}
