package net.shoal.sir.voteup.config;

import lombok.NonNull;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.data.Notice;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.serverct.parrot.parrotx.config.PConfig;
import org.serverct.parrot.parrotx.utils.I18n;
import org.serverct.parrot.parrotx.utils.JsonChatUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CacheManager extends PConfig {

    private final Map<String, Map<Integer, Notice>> notices = new HashMap<>();

    public CacheManager() {
        super(VoteUp.getInstance(), "cache", "缓存日志文件");
    }

    @Override
    public void load(@NonNull File file) {
        ConfigurationSection log = config.getConfigurationSection("Logs");
        if (log == null) return;
        log.getKeys(false).forEach(
                voteID -> {
                    ConfigurationSection section = log.getConfigurationSection(voteID);
                    if (section != null) {
                        section.getKeys(false).forEach(
                                number -> {
                                    ConfigurationSection numberSection = section.getConfigurationSection(number);
                                    if (numberSection != null) {
                                        Map<Integer, Notice> map = notices.getOrDefault(voteID, new HashMap<>());
                                        Notice notice = new Notice(voteID, numberSection);
                                        map.put(notice.number, notice);
                                        notices.put(voteID, map);
                                    }
                                }
                        );
                    }
                }
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void saveDefault() {
        try {
            this.file.createNewFile();
        } catch (IOException e) {
            plugin.lang.logError(I18n.LOAD, getTypeName(), e, null);
        }
    }

    @Override
    public void save() {
        ConfigurationSection log = config.createSection("Logs");
        this.notices.forEach(
                (voteID, map) -> {
                    ConfigurationSection voteIDSection = log.createSection(voteID);
                    map.forEach((number, notice) -> notice.save(voteIDSection.createSection(String.valueOf(number))));
                }
        );
        super.save();
    }

    public void log(Notice.Type type, String voteID, Map<String, Object> params) {
        Map<Integer, Notice> noticeMap = notices.getOrDefault(voteID, new HashMap<>());
        int number = noticeMap.size() + 1;
        while (noticeMap.containsKey(number)) number++;
        noticeMap.put(number, new Notice(type, voteID, number, params));
        notices.put(voteID, noticeMap);
    }

    public void report(Notice.Type type, @NonNull Player user) {
        List<String> content = new ArrayList<>();
        StringBuilder hover = new StringBuilder();
        this.notices.forEach(
                (voteID, map) -> map.forEach(
                        (number, notice) -> {
                            String append = notice.announce(user.getUniqueId());
                            if (append != null) content.add(append);
                        }
                )
        );

        TextComponent text = JsonChatUtil.getFromLegacy(
                plugin.lang.get(plugin.localeKey, I18n.Type.INFO, "Vote", "Notice." + type.name() + ".Head")
                        .replace("%amount%", String.valueOf(content.size()))
        );
        Iterator<String> iterator = content.iterator();
        while (iterator.hasNext()) {
            hover.append(iterator.next());
            if (iterator.hasNext()) hover.append("\n");
        }

        text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(I18n.color(hover.toString()))));
        user.spigot().sendMessage(text);
    }
}
