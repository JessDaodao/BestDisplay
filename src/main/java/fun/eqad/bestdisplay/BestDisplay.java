package fun.eqad.bestdisplay;

import fun.eqad.bestdisplay.Util.NameUtil;
import fun.eqad.bestdisplay.bstats.bStats;
import fun.eqad.bestdisplay.config.ConfigManager;
import fun.eqad.bestdisplay.command.CommandManager;
import fun.eqad.bestdisplay.entity.*;
import fun.eqad.bestdisplay.block.CropEvent;
import fun.eqad.bestdisplay.block.BeeNestEvent;
import org.bukkit.event.*;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.*;

public final class BestDisplay extends JavaPlugin implements Listener {
    private ConfigManager config;
    private NameUtil nameUtil;
    private HitEvent hitEvent;
    private DropEvent dropEvent;
    private CropEvent cropEvent;
    private BeeNestEvent beeNestEvent;
    private VillagerEvent villagerEvent;
    private TNTEvent tntEvent;

    public ConfigManager getConfigManager() { return config; }
    public NameUtil getNameUtil() { return nameUtil; }
    public HitEvent getHitEvent() { return hitEvent; }
    public DropEvent getDropEvent() { return dropEvent; }
    public CropEvent getCropEvent() { return cropEvent; }
    public BeeNestEvent getBeeNestEvent() { return beeNestEvent; }
    public VillagerEvent getVillagerEvent() { return villagerEvent; }
    public TNTEvent getTNTEvent() { return tntEvent; }

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
        this.nameUtil = new NameUtil(this);
        this.hitEvent = new HitEvent(this);
        this.dropEvent = new DropEvent(this);
        this.cropEvent = new CropEvent(this);
        this.beeNestEvent = new BeeNestEvent(this);
        this.villagerEvent = new VillagerEvent(this);
        this.tntEvent = new TNTEvent(this);
        
        new bStats(this, 28325);
        getServer().getPluginManager().registerEvents(hitEvent, this);
        getServer().getPluginManager().registerEvents(dropEvent, this);
        getServer().getPluginManager().registerEvents(cropEvent, this);
        getServer().getPluginManager().registerEvents(beeNestEvent, this);
        getServer().getPluginManager().registerEvents(villagerEvent, this);
        getServer().getPluginManager().registerEvents(tntEvent, this);
        getCommand("bestdisplay").setExecutor(new CommandManager(this));
        getCommand("bestdisplay").setTabCompleter(new CommandManager(this));

        if (!new File(getDataFolder(), "lang.json").exists()) {
            saveResource("lang.json", false);
        }

        NameUtil.loadItemNames(this);
        cleanupArmorStands();

        getLogger().info("BestDisplay已成功加载");
    }

    @Override
    public void onDisable() {
        cleanupArmorStands();

        if (tntEvent != null || villagerEvent != null) {
            tntEvent.cleanup();
            villagerEvent.cleanup();
        }
        
        getLogger().info("BestDisplay已成功卸载");
    }
    
    private void cleanupArmorStands() {
        for (org.bukkit.World world : getServer().getWorlds()) {
            for (org.bukkit.entity.Entity entity : world.getEntities()) {
                if (entity instanceof org.bukkit.entity.ArmorStand && 
                    entity.getScoreboardTags().contains("BestDisplay")) {
                    entity.remove();
                }
            }
        }
    }
}