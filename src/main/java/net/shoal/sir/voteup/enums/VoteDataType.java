package net.shoal.sir.voteup.enums;

public enum VoteDataType {
    ID("投票ID"),
    STATUS("投票状态"),
    TYPE("投票类型"),
    AMOUNT("需求同意人数"),
    TITLE("投票标题"),
    DESCRIPTION("投票简述"),
    CHOICE("投票选项"),
    STARTER("发起者"),
    STARTTIME("发起时间"),
    DURATION("持续时间"),
    PARTICIPANT("参加者"),
    AUTOCAST("自动执行"),
    PASS("通过时显示内容"),
    REJECT("未通过时显示内容");

    private final String name;
    VoteDataType(String type) {
        this.name = type;
    }
    public String getName() {
        return name;
    }
}
