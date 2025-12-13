package fun.eqad.bestdisplay.config;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private String messagePrefix;
    private boolean damageAbove;
    private boolean healingAbove;
    private boolean healthActionBar;
    private boolean dropDisplay;
    private boolean cropDisplay;
    private boolean tntDisplay;
    private boolean villagerDisplay;
    private boolean beeNestDisplay;
    private boolean furnaceDisplay;
    private boolean arrowSound;

    public ConfigManager(BestDisplay plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void reload() {
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        messagePrefix = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix", "&8[&bBestDisplay&8]&r "));
        damageAbove = config.getBoolean("settings.display.damage_above", true);
        healingAbove = config.getBoolean("settings.display.healing_above", true);
        healthActionBar = config.getBoolean("settings.display.health_action_bar", true);
        dropDisplay = config.getBoolean("settings.display.drop", true);
        cropDisplay = config.getBoolean("settings.display.crop", true);
        tntDisplay = config.getBoolean("settings.display.tnt", true);
        villagerDisplay = config.getBoolean("settings.display.villager", true);
        beeNestDisplay = config.getBoolean("settings.display.bee_nest", true);
        furnaceDisplay = config.getBoolean("settings.display.furnace", true);
        arrowSound = config.getBoolean("settings.sound.arrow", true);
    }

    public String getMessagePrefix() { return messagePrefix; }
    public boolean shouldDamageAbove() { return damageAbove; }
    public boolean shouldHealingAbove() { return healingAbove; }
    public boolean shouldHealthActionBar() { return healthActionBar; }
    public boolean shouldDropDisplay() { return dropDisplay; }
    public boolean shouldCropDisplay() { return cropDisplay; }
    public boolean shouldTNTDisplay() { return tntDisplay; }
    public boolean shouldVillagerDisplay() { return villagerDisplay; }
    public boolean shouldBeeNestDisplay() { return beeNestDisplay; }
    public boolean shouldFurnaceDisplay() { return furnaceDisplay; }
    public boolean shouldArrowSound() { return arrowSound; }
}