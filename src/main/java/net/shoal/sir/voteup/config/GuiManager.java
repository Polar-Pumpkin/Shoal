package net.shoal.sir.voteup.config;

import lombok.Getter;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.ChestMenu;
import net.shoal.sir.voteup.enums.MessageType;
import net.shoal.sir.voteup.util.CommonUtil;
import net.shoal.sir.voteup.util.InventoryUtil;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.Bukkit;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class GuiManager {

    public static final String CREATE_MENU = "CreateMenu";

    private static LocaleUtil locale;
    private static GuiManager instance;
    public static GuiManager getInstance() {
        if(instance == null) {
            instance = new GuiManager();
        }
        locale = VoteUp.getInstance().getLocale();
        return instance;
    }

    private File dataFolder = new File(VoteUp.getInstance().getDataFolder() + File.separator + "Guis");

    @Getter private Map<String, ChestMenu> guiMap = new HashMap<>();

    public void load() {
        locale.debug("&7开始加载 Gui 配置文件.");
        if(!dataFolder.exists()) {
            dataFolder.mkdirs();
            Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7未找到 Gui 配置文件夹, 已自动生成."));
        }
        checkConfiguration();

        File[] votes = dataFolder.listFiles(pathname -> pathname.getName().endsWith(".yml"));
        locale.debug("&7获取 Gui 配置文件列表.");

        if(votes != null && votes.length > 0) {
            locale.debug("&7Gui 配置文件列表中存在有效配置文件.");
            for (File dataFile : votes) {
                guiMap.put(CommonUtil.getNoExFileName(dataFile.getName()), InventoryUtil.buildChestMenu(dataFile));
                locale.debug("&7加载 Gui 配置: &c" + CommonUtil.getNoExFileName(dataFile.getName()));
            }
            Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.INFO, "&7共加载 &c" + guiMap.size() + " &7项 Gui 配置."));
            locale.debug("&7已加载配置列表: &c" + guiMap.toString());
        } else {
            locale.debug("&7Gui 配置文件列表为空.");
            Bukkit.getLogger().info(locale.buildMessage(VoteUp.LOCALE, MessageType.WARN, "&7没有 Gui 配置可供加载."));
        }
    }

    public void checkConfiguration() {
        if(!new File(dataFolder.getAbsolutePath() + File.separator + CREATE_MENU + ".yml").exists()) {
            VoteUp.getInstance().saveResource("Guis/CreateMenu.yml", false);
            locale.debug("&7释放默认配置文件: &cCreateMenu.yml");
        }
    }

    public ChestMenu checkGui(String inventoryName) {
        for(String key : guiMap.keySet()) {
            ChestMenu target = guiMap.get(key);
            if(inventoryName.equalsIgnoreCase(target.getTitle())) {
                return target;
            }
        }
        return null;
    }

    public ChestMenu getMenu(String key){
        locale.debug("&7调用 getMenu 方法.");
        locale.debug("&7已加载 Gui 配置列表: &c" + guiMap.keySet().toString());
        if(guiMap.containsKey(key)) {
            locale.debug("&7目标菜单已加载: &c" + key + " &9-> &c" + guiMap.get(key).toString());
            return guiMap.get(key);
        }
        /*File dataFile = new File(dataFolder.getAbsolutePath() + File.separator + key + ".yml");
        if(dataFile.exists()) {
            return InventoryUtil.buildChestMenu(dataFile);
        }*/
        locale.debug("&7目标菜单未加载: &c" + key + " &9-> &cnull");
        return InventoryUtil.buildChestMenu(null);
    }

}
