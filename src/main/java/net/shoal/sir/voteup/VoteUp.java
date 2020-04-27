package net.shoal.sir.voteup;

import lombok.Getter;
import net.shoal.sir.voteup.config.*;
import net.shoal.sir.voteup.listener.InventoryClickListener;
import net.shoal.sir.voteup.listener.PlayerJoinListener;
import org.bukkit.Bukkit;
import org.serverct.parrot.parrotx.PPlugin;

public final class VoteUp extends PPlugin {
    @Override
    protected void registerListener() {
        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), this);
        Bukkit.getPluginManager().registerEvents(new InventoryClickListener(), this);
    }

    @Override
    protected void preload() {
        this.pConfig = new ConfigManager();
        this.pConfig.init();
    }

    @Override
    public void load() {
        SoundManager.getInstance().init();
        CacheManager.getInstance().load();
        ExecutorManager.getInstance().load();
        GuiManager.getInstance().load();
        VoteManager.getInstance().load();
    }

    public enum Perm {
        ALL("VoteUp.*"),
        ADMIN("VoteUp.admin"),
        USER("VoteUp.user"),
        NOTICE("VoteUp.notice"),
        VIEW("VoteUp.view"),
        CREATE("VoteUp.create"),
        CREATE_CUSTOM_ALL("VoteUp.create.*"),
        CREATE_CUSTOM_TITLE("VoteUp.create.title"),
        CREATE_CUSTOM_TYPE("VoteUp.create.type"),
        CREATE_CUSTOM_AMOUNT("VoteUp.create.amount"),
        CREATE_CUSTOM_DESCRIPTION("VoteUp.create.description"),
        CREATE_CUSTOM_DURATION("VoteUp.create.duration"),
        CREATE_CUSTOM_CHOICE("VoteUp.create.choice"),
        CREATE_CUSTOM_AUTOCAST("VoteUp.create.autocast"),
        CREATE_CUSTOM_AUTOCAST_BYPASS("VoteUp.create.autocast.bypass"),
        CREATE_CUSTOM_RESULT("VoteUp.create.result"),
        CREATE_SIMPLE("VoteUp.create.simple"),
        VOTE("VoteUp.vote.*"),
        VOTE_ACCEPT("VoteUp.vote.accept"),
        VOTE_NEUTRAL("VoteUp.vote.neutral"),
        VOTE_REFUSE("VoteUp.vote.refuse"),
        VOTE_REASON("VoteUp.vote.reason");

        @Getter
        private final String node;

        Perm(String type) {
            this.node = type;
        }
    }
}
