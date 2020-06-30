package net.shoal.sir.voteup.command;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.api.VoteUpPerm;
import net.shoal.sir.voteup.command.subcommands.CreateCmd;
import net.shoal.sir.voteup.command.subcommands.ModifyCmd;
import net.shoal.sir.voteup.command.subcommands.ViewCmd;
import org.serverct.parrot.parrotx.command.CommandHandler;
import org.serverct.parrot.parrotx.command.subcommands.HelpCommand;
import org.serverct.parrot.parrotx.command.subcommands.ReloadCommand;

public class VoteUpCmd extends CommandHandler {

    /*private String[] helpList = {
            "&6&lVote Up &8&o(v1.4-SNAPSHOT)",
            "",
            "&7&l作者: &cEntityParrot_",
            "&7&l命令列表:",
            "  &d/vote help &9- &7查看插件帮助.",
            "",
            "  &d/vote create ... &9- &b创建投票&7相关指令.",
            "    &a&l&o-> &d&o无参数 &9- &7开始创建投票.",
            "    &a&l&o-> &dback &9- &7返回上一个未发布的投票草稿.",
            "",
            "  &d/vote modify &7<&cdesc&7/&cautocast&7> ... &9- &b修改投票描述/自动执行&7相关指令(一般不需要手打).",
            "    &a&l&o-> &dadd &7[&c行号&7] &9- &7添加一条内容(或在指定行插入内容).",
            "    &a&l&o-> &dset &7<&c行号&7> &9- &7设置指定行内容.",
            "    &a&l&o-> &ddel &7[&c行号&7] &9- &7删除最后一条内容(或删除指定行).",
            "",
            "  &d/vote view &7<&c投票ID&7> &9- &7查看指定投票的详细信息.",
            "",
            "  &d/vote reload &9- &7重载插件配置文件.",
            "  &d/vote debug &9- &7切换 Debug 模式."
    };*/

    public VoteUpCmd() {
        super(VoteUp.getInstance(), "voteup");
        registerSubCommand("help", new HelpCommand(plugin, null, this));
        registerSubCommand("create", new CreateCmd());
        registerSubCommand("reload", new ReloadCommand(plugin, VoteUpPerm.ADMIN.node));
        registerSubCommand("modify", new ModifyCmd());
        registerSubCommand("view", new ViewCmd());
    }
}
