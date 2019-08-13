package net.shoal.sir.voteup.config;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.enums.ChoiceType;
import net.shoal.sir.voteup.enums.ExecutorType;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.itemexecutor.createmenu.*;
import net.shoal.sir.voteup.itemexecutor.votedetail.VoteAction;
import net.shoal.sir.voteup.util.LocaleUtil;

import java.util.HashMap;
import java.util.Map;

public class ExecutorManager {

    private static LocaleUtil locale;
    private static ExecutorManager instance;
    public static ExecutorManager getInstance() {
        if(instance == null) {
            instance = new ExecutorManager();
        }
        locale = VoteUp.getInstance().getLocale();
        return instance;
    }

    private Map<ExecutorType, MenuItemExecutor> executorMap = new HashMap<>();

    private void registerExecutor(ExecutorType type, MenuItemExecutor executor) {
        executorMap.put(type, executor);
        locale.debug("&7注册执行器: &c" + type.toString());
    }

    public void load() {
        registerExecutor(ExecutorType.SET_TITLE, new SetTitle());
        registerExecutor(ExecutorType.SWITCH_TYPE, new SwitchType());
        registerExecutor(ExecutorType.MODIFY_DESCRIPTION, new ModifyDescription());
        registerExecutor(ExecutorType.SET_DURATION, new SetDuration());
        registerExecutor(ExecutorType.SET_CHOICE, new SetChoice());
        registerExecutor(ExecutorType.SET_AMOUNT, new SetAmount());
        registerExecutor(ExecutorType.MODIFY_AUTOCAST, new ModifyAutocast());
        registerExecutor(ExecutorType.SET_RESULT, new SetResult());
        registerExecutor(ExecutorType.VOTE_START, new StartVote());

        registerExecutor(ExecutorType.VOTE_ACCEPT, new VoteAction(ChoiceType.ACCEPT));
        registerExecutor(ExecutorType.VOTE_NEUTRAL, new VoteAction(ChoiceType.NEUTRAL));
        registerExecutor(ExecutorType.VOTE_REFUSE, new VoteAction(ChoiceType.REFUSE));
    }

    public MenuItemExecutor getExecutor(ExecutorType type) {
        locale.debug("&7调用 getExecutor 方法.");
        if(executorMap.containsKey(type)) {
            locale.debug("&7目标执行器已注册: &c" + type.toString());
            return executorMap.get(type);
        }
        locale.debug("&7目标执行器未注册, 返回 &4null&7: &c" + type.toString());
        return null;
    }
}
