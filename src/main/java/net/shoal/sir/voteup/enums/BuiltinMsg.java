package net.shoal.sir.voteup.enums;

public enum BuiltinMsg {
    NO_DESCRIPTION("&7&o这个发起人很懒，投票简介都没写。"),
    REASON_NO_PERM("&7&o对方无权限发表投票看法"),
    REASON_NOT_YET("&7&o对方暂未发表投票看法"),
    VOTE_VALUE_CHANGE_FAIL("设置 &c%s &7失败, 您的账户下没有待发布的投票."),
    VOTE_VALUE_CHANGE_SUCCESS("更改 &c%s &7成功."),
    ;

    public final String msg;

    BuiltinMsg(String msg) {
        this.msg = msg;
    }
}
