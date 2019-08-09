package net.shoal.sir.voteup.config;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.enums.ExecutorType;
import net.shoal.sir.voteup.itemexecutor.MenuItemExecutor;
import net.shoal.sir.voteup.itemexecutor.createmenu.ModifyDescription;
import net.shoal.sir.voteup.itemexecutor.createmenu.SetTitle;
import net.shoal.sir.voteup.itemexecutor.createmenu.SwitchType;
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
