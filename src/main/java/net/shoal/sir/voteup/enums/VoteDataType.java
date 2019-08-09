package net.shoal.sir.voteup.enums;

public enum VoteDataType {
    ID("投票ID"),
    STATUS("投票状态"),
    TYPE("投票类型"),
    AMOUNT("需求同意人数"),
    TITLE("投票标题"),
    DESCRIPTION("投票简述"),
    STARTER("发起者"),
    STARTTIME("发起时间"),
    DURATION("持续时间"),
    PARTICIPANT("参加者"),
    AUTOCAST("自动执行");

    private final String name;
    VoteDataType(String type) {
        this.name = type;
    }
    public String getName() {
        return name;
    }
}
