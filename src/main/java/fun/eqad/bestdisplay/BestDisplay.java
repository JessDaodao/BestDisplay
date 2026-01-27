package fun.eqad.bestdisplay;

import fun.eqad.bestdisplay.util.NameUtil;
import fun.eqad.bestdisplay.bstats.bStats;
import fun.eqad.bestdisplay.config.ConfigManager;
import fun.eqad.bestdisplay.command.CommandManager;
import fun.eqad.bestdisplay.entity.*;
import fun.eqad.bestdisplay.block.*;
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
    private FurnaceEvent furnaceEvent;
    private EnchantingTableEvent enchantingTableEvent;
    private VillagerEvent villagerEvent;
    private TNTEvent tntEvent;
    private EndCrystalEvent endCrystalEvent;
    private ChatEvent chatEvent;

    public ConfigManager getConfigManager() { return config; }
    public NameUtil getNameUtil() { return nameUtil; }
    public HitEvent getHitEvent() { return hitEvent; }
    public DropEvent getDropEvent() { return dropEvent; }
    public CropEvent getCropEvent() { return cropEvent; }
    public BeeNestEvent getBeeNestEvent() { return beeNestEvent; }
    public FurnaceEvent getFurnaceEvent() { return furnaceEvent; }
    public EnchantingTableEvent getEnchantingTableEvent() { return enchantingTableEvent; }
    public VillagerEvent getVillagerEvent() { return villagerEvent; }
    public TNTEvent getTNTEvent() { return tntEvent; }
    public EndCrystalEvent getEndCrystalEvent() { return endCrystalEvent; }
    public ChatEvent getChatEvent() { return chatEvent; }

    @Override
    public void onEnable() {
        getLogger().info("________            __________________               ______");
        getLogger().info("___  __ )_____________  /___  __ \\__(_)_________________  /_____ _____  __");
        getLogger().info("__  __  |  _ \\_  ___/  __/_  / / /_  /__  ___/__  __ \\_  /_  __ `/_  / / /");
        getLogger().info("_  /_/ //  __/(__  )/ /_ _  /_/ /_  / _(__  )__  /_/ /  / / /_/ /_  /_/ /");
        getLogger().info("/_____/ \\___//____/ \\__/ /_____/ /_/  /____/ _  .___//_/  \\__,_/ _\\__, /");
        getLogger().info("                                             /_/                 /____/");
        getLogger().info("我在视间你...");
        getLogger().info("Author: EQAD Network");

        this.config = new ConfigManager(this);
        this.nameUtil = new NameUtil(this);
        this.hitEvent = new HitEvent(this);
        this.dropEvent = new DropEvent(this);
        this.cropEvent = new CropEvent(this);
        this.beeNestEvent = new BeeNestEvent(this);
        this.furnaceEvent = new FurnaceEvent(this);
        this.enchantingTableEvent = new EnchantingTableEvent(this);
        this.villagerEvent = new VillagerEvent(this);
        this.tntEvent = new TNTEvent(this);
        this.endCrystalEvent = new EndCrystalEvent(this);
        this.chatEvent = new ChatEvent(this);
        
        new bStats(this, 28325);
        getServer().getPluginManager().registerEvents(hitEvent, this);
        getServer().getPluginManager().registerEvents(endCrystalEvent, this);
        getServer().getPluginManager().registerEvents(chatEvent, this);
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

        if (
                tntEvent != null ||
                villagerEvent != null ||
                beeNestEvent != null ||
                furnaceEvent != null ||
                enchantingTableEvent != null
        ) {
            tntEvent.cleanup();
            villagerEvent.cleanup();
            beeNestEvent.cleanup();
            furnaceEvent.cleanup();
            enchantingTableEvent.cleanup();
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