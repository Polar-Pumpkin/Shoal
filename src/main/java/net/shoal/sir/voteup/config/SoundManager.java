package net.shoal.sir.voteup.config;

import net.shoal.sir.voteup.VoteUp;
import net.shoal.sir.voteup.util.LocaleUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SoundManager {

    private static LocaleUtil locale;
    private static SoundManager instance;
    public static SoundManager getInstance() {
        if(instance == null){
            instance = new SoundManager();
        }
        locale = VoteUp.getInstance().getLocale();
        return instance;
    }

    ConfigurationSection setting;

    public void init() {
        setting = VoteUp.getInstance().getConfig().getConfigurationSection("Sound");
    }

    public void success(String playerName) {
        playSound(playerName, setting.getString("Action.Success"));
    }

    public void fail(String playerName) {
        playSound(playerName, setting.getString("Action.Failure"));
    }

    public void ding(String playerName) {
        playSound(playerName, setting.getString("Action.Start"));
    }

    public void playSound(String playerName, String sound) {
        Player target = Bukkit.getPlayerExact(playerName);
        if(target != null && target.isOnline()) {
            target.playSound(target.getLocation(), Sound.valueOf(sound.toUpperCase()), 1, 1);
        }
    }

}
