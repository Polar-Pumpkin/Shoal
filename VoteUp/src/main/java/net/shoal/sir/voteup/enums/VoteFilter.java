package net.shoal.sir.voteup.enums;

public enum VoteFilter {
    TIME("多久以前至现在发布的投票"),
    OWNER("指定投票发起人的投票"),
    VOTER("指定玩家参与的投票"),
    RESULT("指定投票结果的投票"),
    OPEN("是否结束的投票");

    public final String desc;
    VoteFilter(String desc) {
        this.desc = desc;
    }
}
