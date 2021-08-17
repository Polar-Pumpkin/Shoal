package net.shoal.parrot.prefixme;

import lombok.Getter;
import org.serverct.parrot.parrotx.PPlugin;

public final class PrefixMe extends PPlugin {

    @Getter
    private static PrefixMe inst;

    @Override
    protected void preload() {
        inst = this;
        pConfig = Conf.getInst();
    }

    @Override
    protected void load() {
        PrefixManager.getInst().init();

        super.registerCommand(new Command());

        registerExpansion(new PrefixMeExpansion());

        listen(pluginManager -> pluginManager.registerEvents(new PlayerInteractListener(), this));
    }

    @Override
    public void onDisable() {
        PrefixManager.getInst().save();
        super.onDisable();
    }
}
