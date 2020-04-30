package net.shoal.sir.voteup.enums;

public enum BuiltinMsg {
    NO_DESCRIPTION("&7&o这个发起人很懒, 投票简介都没写."),
    REASON_NO_PERM("&7&o对方无权限发表投票看法"),
    REASON_NOT_YET("&7&o对方暂未发表投票看法"),
    VOTE_VALUE_CHANGE_FAIL("&7设置 &c%s &7失败, 您的账户下没有待发布的投票."),
    VOTE_VALUE_CHANGE_SUCCESS("&7更改 &c%s &7成功."),
    VOTE_VALUE_DESCRIPTION("&f&o共有 &c&o%d &f&o行简述:"),
    VOTE_VALUE_AUTOCAST("&f&o共有 &c&o%d &f&o行自动执行命令:"),
    VOTE_VALUE_PARTICIPANT("&f当前 &c%d &f位玩家已参与投票"),
    VOTE_CLICK("&a点击立即查看投票详细信息"),
    ERROR_GET_CHOICE("&c获取选项内容失败"),
    ERROR_PLACEHOLDER_REQUEST("&c获取变量内容失败"),
    ;

    public final String msg;

    BuiltinMsg(String msg) {
        this.msg = msg;
    }
}
