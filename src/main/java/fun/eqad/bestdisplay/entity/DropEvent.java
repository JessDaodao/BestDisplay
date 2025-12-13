package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class DropEvent implements Listener {
    private final BestDisplay plugin;
    private final Map<UUID, ArmorStand> displayMap = new HashMap<>();

    public DropEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemDrop(EntityDropItemEvent event) {
        if (!plugin.getConfigManager().shouldDropDisplay()) return;
        
        Item droppedItem = event.getItemDrop();
        displayItemInfo(droppedItem);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        if (!plugin.getConfigManager().shouldDropDisplay()) return;
        
        Item spawnedItem = event.getEntity();
        displayItemInfo(spawnedItem);
    }

    private void displayItemInfo(Item item) {
        ItemStack itemStack = item.getItemStack();
        String itemName = NameUtil.getItemName(itemStack);
        int amount = itemStack.getAmount();
        
        String displayName = amount > 1 ? itemName + " §7x" + amount : itemName;
        
        Location itemLocation = item.getLocation();
        Location displayLocation = itemLocation.clone().add(0, 0.5, 0);
        
        UUID itemId = item.getUniqueId();

        if (displayMap.containsKey(itemId)) {
            displayMap.get(itemId).remove();
        }
        
        ArmorStand display = item.getWorld().spawn(displayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(false);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            
            armorStand.setCustomName(displayName);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        displayMap.put(itemId, display);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (item.isDead() || !item.isValid()) {
                    if (displayMap.containsKey(itemId)) {
                        displayMap.get(itemId).remove();
                        displayMap.remove(itemId);
                    }
                    this.cancel();
                    return;
                }
                
                if (displayMap.containsKey(itemId)) {
                    ArmorStand armorStand = displayMap.get(itemId);
                    if (armorStand.isValid()) {
                        Location currentLocation = item.getLocation();
                        armorStand.teleport(currentLocation.clone().add(0, 0.5, 0));
                        
                        ItemStack currentItemStack = item.getItemStack();
                        String currentItemName = NameUtil.getItemName(currentItemStack);
                        int currentAmount = currentItemStack.getAmount();
                        String currentDisplayName = currentAmount > 1 ? currentItemName + " §7x" + currentAmount : currentItemName;
                        armorStand.setCustomName(currentDisplayName);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (displayMap.containsKey(itemId)) {
                    ArmorStand armorStand = displayMap.get(itemId);
                    if (armorStand.isValid()) {
                        armorStand.setCustomNameVisible(true);
                    }
                }
            }
        }.runTaskLater(plugin, 10L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (displayMap.containsKey(itemId)) {
                    displayMap.get(itemId).remove();
                    displayMap.remove(itemId);
                }
            }
        }.runTaskLater(plugin, 20L * 120);
    }
}
