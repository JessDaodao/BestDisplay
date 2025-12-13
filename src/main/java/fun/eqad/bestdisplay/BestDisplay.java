package fun.eqad.bestdisplay;

import fun.eqad.bestdisplay.bstats.bStats;
import fun.eqad.bestdisplay.config.ConfigManager;
import fun.eqad.bestdisplay.command.CommandManager;
import fun.eqad.bestdisplay.entity.*;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;

public final class BestDisplay extends JavaPlugin implements Listener {
    private ConfigManager config;
    private HitEvent hitEvent;
    private DropEvent dropEvent;

    public ConfigManager getConfigManager() { return config; }
    public HitEvent getHitEvent() { return hitEvent; }
    public DropEvent getDropEvent() { return dropEvent; }

    @Override
    public void onEnable() {
        getLogger().info("   ___           _      ___ _           _");
        getLogger().info("  / __\\ ___  ___| |_   /   (_)___ _ __ | | __ _ _   _");
        getLogger().info(" /__\\/// _ \\/ __| __| / /\\ / / __| '_ \\| |/ _` | | | |");
        getLogger().info("/ \\/  \\  __/\\__ \\ |_ / /_//| \\__ \\ |_) | | (_| | |_| |");
        getLogger().info("\\_____/\\___||___/\\__/___,' |_|___/ .__/|_|\\__,_|\\__, |");
        getLogger().info("                                 |_|            |___/");
        getLogger().info("我在世间你...");
        getLogger().info("Author: EQAD Network");

        this.config = new ConfigManager(this);
        this.hitEvent = new HitEvent(this);
        this.dropEvent = new DropEvent(this);
        
        new bStats(this, 28325);
        getServer().getPluginManager().registerEvents(hitEvent, this);
        getServer().getPluginManager().registerEvents(dropEvent, this);
        getCommand("bestdisplay").setExecutor(new CommandManager(this));
        getCommand("bestdisplay").setTabCompleter(new CommandManager(this));

        if (!new File(getDataFolder(), "item.json").exists()) {
            saveResource("item.json", false);
        }
        
        if (!new File(getDataFolder(), "entity.json").exists()) {
            saveResource("entity.json", false);
        }

        NameUtil.loadItemNames(this);
        cleanupArmorStands();

        getLogger().info("BestDisplay已成功加载");
    }

    @Override
    public void onDisable() {
        cleanupArmorStands();
        getLogger().info("BestDisplay已成功卸载");
    }
    
    private void cleanupArmorStands() {
        int cleanedCount = 0;
        for (org.bukkit.World world : getServer().getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof org.bukkit.entity.ArmorStand && 
                    entity.getScoreboardTags().contains("BestDisplay")) {
                    entity.remove();
                    cleanedCount++;
                }
            }
        }
    }
}