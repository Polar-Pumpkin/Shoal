package net.shoal.sir.voteup.command.subcommands;

import net.shoal.sir.voteup.command.Subcommand;
import net.shoal.sir.voteup.util.CommonUtil;
import org.bukkit.command.CommandSender;

public class Help implements Subcommand {

    private String[] helpList = {
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
    };

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        for(String text : helpList) {
            sender.sendMessage(CommonUtil.color(text));
        }
        return true;
    }
}
