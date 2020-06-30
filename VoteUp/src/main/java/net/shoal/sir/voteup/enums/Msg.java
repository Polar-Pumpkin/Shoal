package net.shoal.sir.voteup.enums;

public enum Msg {
    NO_DESCRIPTION("&7&o这个发起人很懒, 投票简介都没写."),
    REASON_NO_PERM("&7&o对方无权限发表投票看法"),
    REASON_NOT_YET("&7&o对方暂未发表投票看法"),
    VOTE_EDIT_FAIL("&7设置 &c%s &7失败, 您的账户下没有待发布的投票."),
    VOTE_EDIT_SUCCESS("&7更改 &c%s &7成功."),
    VOTE_EDIT("&7请输入新的 &c%s&7."),
    VOTE_EDIT_CANCELLED("&7您已取消更改 &c%s&7."),
    VOTE_EDIT_PASSED("&7您已跳过更改 &c%s&7."),
    VOTE_VALUE_DESCRIPTION("&f&o共有 &c&o%d &f&o行简述:"),
    VOTE_VALUE_AUTOCAST("&f&o共有 &c&o%d &f&o行自动执行命令:"),
    VOTE_VALUE_PARTICIPANT("&f当前 &c%d &f位玩家已参与投票"),
    VOTE_ANONYMOUS_DESC("&7// 丢弃(Q)键匿名投票 (潜行+丢弃 投票并发表看法)"),
    VOTE_ANONYMOUS_ENABLE("&a允许匿名投票"),
    VOTE_ANONYMOUS_DISABLE("&c禁止匿名投票"),
    VOTE_PUBLIC_ENABLE("&a公开投票进度"),
    VOTE_PUBLIC_DISABLE("&c不公开投票进度"),
    VOTE_EDITABLE_ENABLE("&a允许编辑所投票"),
    VOTE_EDITABLE_DISABLE("&c投票后不可修改"),
    VOTE_FEATURE_SWITCH("点击切换状态"),
    VOTE_FEATURE_LOCK("&c此功能已禁止编辑"),
    VOTE_CLICK("&a点击立即查看投票详细信息"),
    VOTE_REASON("&7您为什么选择这个选项呢? 发表一下您的看法吧."),
    AUTOCAST_MODE_WHITELIST("白名单模式"),
    AUTOCAST_MODE_BLACKLIST("黑名单模式"),
    AUTOCAST_MODE_USERMODE("玩家权限模式"),
    AUTOCAST_MODE_WHITELIST_DESC("可以执行的指令"),
    AUTOCAST_MODE_BLACKLIST_DESC("不可以执行的指令"),
    AUTOCAST_MODE_USERMODE_DESC("所有您可以执行的指令"),
    BSTATS_ENABLE("&c&lbStats 统计信息&7已启用, 感谢您的支持!"),
    BSTATS_DISABLE("&c&lbStats 统计信息&7已禁用."),
    ERROR_GET_CHOICE("&c获取选项内容失败"),
    ERROR_GET_VOTE("&7目标投票不存在."),
    ERROR_PLACEHOLDER_REQUEST("&c获取变量内容失败"),
    ERROR_GUI_TITLE("初始化菜单遇到错误"),
    ERROR_EDIT_NO_PERM("&7您没有权限修改该投票或该项内容."),
    ERROR_EDIT_DURATION("&7无法识别您输入的时长."),
    ERROR_EDIT_AUTOCAST_DISABLE("&7自动执行功能被禁用."),
    ERROR_EDIT_AUTOCAST_IGNORE("&7您输入的命令中含有屏蔽关键词."),
    ERROR_COMMAND_NOT_PLAYER("&7控制台无法执行此命令.");

    public final String msg;

    Msg(String msg) {
        this.msg = msg;
    }
}
