package fun.eqad.bestdisplay;

import fun.eqad.bestdisplay.bstats.bStats;
import fun.eqad.bestdisplay.config.ConfigManager;
import fun.eqad.bestdisplay.command.CommandManager;
import fun.eqad.bestdisplay.entity.HitEvent;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class BestDisplay extends JavaPlugin implements Listener {
    private ConfigManager config;
    private HitEvent hitEvent;

    public ConfigManager getConfigManager() { return config; }
    public HitEvent getHitEvent() { return hitEvent; }

    @Override
    public void onEnable() {
        getLogger().info("   ___           _      ___ _           _");
        getLogger().info("  / __\\ ___  ___| |_   /   (_)___ _ __ | | __ _ _   _");
        getLogger().info(" /__\\/// _ \\/ __| __| / /\\ / / __| '_ \\| |/ _` | | | |");
        getLogger().info("/ \\/  \\  __/\\__ \\ |_ / /_//| \\__ \\ |_) | | (_| | |_| |");
        getLogger().info("\\_____/\\___||___/\\__/___,' |_|___/ .__/|_|\\__,_|\\__, |");
        getLogger().info("                                 |_|            |___/");
        getLogger().info("我在世间你");
        getLogger().info("Author: EQAD Network");

        this.config = new ConfigManager(this);
        this.hitEvent = new HitEvent(this);
        new bStats(this, 28325);
        getServer().getPluginManager().registerEvents(hitEvent, this);
        getCommand("bestdisplay").setExecutor(new CommandManager(this));
        getCommand("bestdisplay").setTabCompleter(new CommandManager(this));

        getLogger().info("BestDisplay已成功加载");
    }

    @Override
    public void onDisable() {
        getLogger().info("BestDisplay已成功卸载");
    }
}