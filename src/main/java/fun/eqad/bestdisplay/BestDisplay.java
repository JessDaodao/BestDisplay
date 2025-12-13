package fun.eqad.bestdisplay;

import fun.eqad.bestdisplay.bstats.bStats;
import fun.eqad.bestdisplay.config.ConfigManager;
import fun.eqad.bestdisplay.command.CommandManager;
import fun.eqad.bestdisplay.entity.EntityEvent;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;

public final class BestDisplay extends JavaPlugin implements Listener {
    private ConfigManager config;
    private EntityEvent playerEvent;

    public ConfigManager getConfigManager() { return config; }
    public EntityEvent getPlayerEvent() { return playerEvent; }

    @Override
    public void onEnable() {
        getLogger().info("   ___           _                    _ _   _");
        getLogger().info("  / __\\ ___  ___| |_  /\\  /\\___  __ _| | |_| |__");
        getLogger().info(" /__\\/// _ \\/ __| __|/ /_/ / _ \\/ _` | | __| '_ \\");
        getLogger().info("/ \\/  \\  __/\\__ \\ |_/ __  /  __/ (_| | | |_| | | |");
        getLogger().info("\\_____/\\___||___/\\__\\/ /_/ \\___|\\__,_|_|\\__|_| |_|");
        getLogger().info("");
        getLogger().info("怎么办? 只有杀");
        getLogger().info("Author: EQAD Network");

        this.config = new ConfigManager(this);
        this.playerEvent = new EntityEvent(this);
        new bStats(this, 28285);
        getServer().getPluginManager().registerEvents(playerEvent, this);
        getCommand("bestdisplay").setExecutor(new CommandManager(this));
        getCommand("bestdisplay").setTabCompleter(new CommandManager(this));

        getLogger().info("BestDisplay已成功加载");
    }

    @Override
    public void onDisable() {
        getLogger().info("BestDisplay已成功卸载");
    }
}