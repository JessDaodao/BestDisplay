package fun.eqad.bestdisplay.Util;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.*;

public class NameUtil {
    private final BestDisplay plugin;
    private static Map<String, String> itemNames = new HashMap<>();
    private static Map<String, String> entityNames = new HashMap<>();
    private static boolean loaded = false;

    public NameUtil(BestDisplay plugin) {
        this.plugin = plugin;
    }
    
    public static void loadItemNames(JavaPlugin plugin) {
        if (loaded) return;

        File itemsFile = new File(plugin.getDataFolder(), "lang.json");
        if (itemsFile.exists()) {
            try (FileReader reader = new FileReader(itemsFile, StandardCharsets.UTF_8)) {
                com.google.gson.JsonObject jsonObject = new com.google.gson.Gson().fromJson(reader, com.google.gson.JsonObject.class);
                
                for (Map.Entry<String, com.google.gson.JsonElement> entry : jsonObject.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue().getAsString();
                    
                    if (key.startsWith("entity.minecraft.")) {
                        entityNames.put(key, value);
                    } else {
                        itemNames.put(key, value);
                    }
                }
            } catch (IOException ignored) {}
        }
        
        loaded = true;
    }
    
    public static String getItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        
        Material material = item.getType();
        return getMaterialName(material);
    }
    
    public static String getMaterialName(Material material) {
        String itemKey = "item.minecraft." + material.name().toLowerCase();
        String blockKey = "block.minecraft." + material.name().toLowerCase();
        
        if (itemNames.containsKey(itemKey)) {
            return itemNames.get(itemKey);
        }
        
        if (itemNames.containsKey(blockKey)) {
            return itemNames.get(blockKey);
        }

        return material.toString().toLowerCase().replace("_", " ");
    }
    
    public static String getEntityName(EntityType entityType) {
        String key = "entity.minecraft." + entityType.name().toLowerCase();
        
        if (entityNames.containsKey(key)) {
            return entityNames.get(key);
        }

        return entityType.toString().toLowerCase().replace("_", " ");
    }
}