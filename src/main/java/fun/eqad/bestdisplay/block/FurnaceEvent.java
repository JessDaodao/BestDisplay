package fun.eqad.bestdisplay.block;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class FurnaceEvent {
    private final BestDisplay plugin;
    private final Map<Location, List<ArmorStand>> displayMap = new HashMap<>();
    private BukkitRunnable updateTask;
    
    public FurnaceEvent(BestDisplay plugin) {
        this.plugin = plugin;
        startUpdateTask();
    }
    
    private void startUpdateTask() {
        updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                updateAllDisplays();
            }
        };
        updateTask.runTaskTimer(plugin, 20L, 5L);
    }
    
    private void updateAllDisplays() {
        if (!plugin.getConfigManager().shouldFurnaceDisplay()) return;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        List<Player> playerList = new ArrayList<>(players);

        Map<Location, List<ArmorStand>> displayMapCopy = new HashMap<>(displayMap);
        for (Map.Entry<Location, List<ArmorStand>> entry : displayMapCopy.entrySet()) {
            Location furnaceLocation = entry.getKey();
            boolean hasNearbyPlayer = false;

            for (Player player : playerList) {
                if (player.getWorld().equals(furnaceLocation.getWorld()) && 
                    player.getLocation().distance(furnaceLocation) <= plugin.getConfigManager().getPlayerRadius()) {
                    hasNearbyPlayer = true;
                    break;
                }
            }

            if (!hasNearbyPlayer) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(furnaceLocation);
                continue;
            }

            Block block = furnaceLocation.getBlock();
            if (!isFurnace(block.getType())) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(furnaceLocation);
                continue;
            }

            updateFurnaceDisplay(block, block.getType(), entry.getValue());
        }

        for (Player player : playerList) {
            Location playerLoc = player.getLocation();
            List<Block> nearbyFurnaces = findAllNearbyFurnaces(playerLoc, plugin.getConfigManager().getPlayerRadius());
            
            for (Block furnace : nearbyFurnaces) {
                Location furnaceLocation = furnace.getLocation();

                if (displayMap.containsKey(furnaceLocation)) {
                    continue;
                }
                
                Material material = furnace.getType();
                if (isFurnace(material)) {
                    displayFurnaceInfo(furnace, material);
                }
            }
        }
    }
    
    private void displayFurnaceInfo(Block furnace, Material material) {
        Location furnaceLocation = furnace.getLocation();
        boolean hasBlockAbove = hasBlockAbove(furnaceLocation);
        boolean hasBlockBelow = hasBlockBelow(furnaceLocation);

        if (hasBlockAbove && hasBlockBelow) {
            return;
        }

        String smeltingItem = "无";
        int unsmeltedCount = 0;
        String fuelItem = "无";
        int fuelCount = 0;
        
        if (furnace.getState() instanceof org.bukkit.block.Furnace) {
            org.bukkit.block.Furnace furnaceState = (org.bukkit.block.Furnace) furnace.getState();
            FurnaceInventory inventory = furnaceState.getInventory();
            
            ItemStack smelting = inventory.getSmelting();
            if (smelting != null && smelting.getType() != Material.AIR) {
                smeltingItem = getItemName(smelting);
                unsmeltedCount = smelting.getAmount();
            }

            ItemStack fuel = inventory.getFuel();
            if (fuel != null && fuel.getType() != Material.AIR) {
                fuelItem = getItemName(fuel);
                fuelCount = fuel.getAmount();
            }
        }
        
        String topText = "§7物品: §f" + smeltingItem + (unsmeltedCount > 1 ? " §7x" + unsmeltedCount : "");
        String bottomText = "§7燃料: §f" + fuelItem + (fuelCount > 1 ? " §7x" + fuelCount : "");

        Location topDisplayLocation, bottomDisplayLocation;
        
        if (hasBlockAbove) {
            topDisplayLocation = furnaceLocation.clone().add(0.5, -0.6, 0.5);
            bottomDisplayLocation = furnaceLocation.clone().add(0.5, -0.9, 0.5);
        } else {
            topDisplayLocation = furnaceLocation.clone().add(0.5, 1.3, 0.5);
            bottomDisplayLocation = furnaceLocation.clone().add(0.5, 1.0, 0.5);
        }
        
        ArmorStand topDisplay = furnace.getWorld().spawn(topDisplayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(topText);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        ArmorStand bottomDisplay = furnace.getWorld().spawn(bottomDisplayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(bottomText);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        List<ArmorStand> displays = new ArrayList<>();
        displays.add(topDisplay);
        displays.add(bottomDisplay);
        displayMap.put(furnaceLocation, displays);
    }
    
    private void updateFurnaceDisplay(Block furnace, Material material, List<ArmorStand> displays) {
        String smeltingItem = "无";
        int unsmeltedCount = 0;
        String fuelItem = "无";
        int fuelCount = 0;
        
        if (furnace.getState() instanceof org.bukkit.block.Furnace) {
            org.bukkit.block.Furnace furnaceState = (org.bukkit.block.Furnace) furnace.getState();
            FurnaceInventory inventory = furnaceState.getInventory();
            
            ItemStack smelting = inventory.getSmelting();
            if (smelting != null && smelting.getType() != Material.AIR) {
                smeltingItem = getItemName(smelting);
                unsmeltedCount = smelting.getAmount();
            }

            ItemStack fuel = inventory.getFuel();
            if (fuel != null && fuel.getType() != Material.AIR) {
                fuelItem = getItemName(fuel);
                fuelCount = fuel.getAmount();
            }
        }
        
        String topText = "§7物品: §f" + smeltingItem + (unsmeltedCount > 1 ? " §7x" + unsmeltedCount : "");
        String bottomText = "§7燃料: §f" + fuelItem + (fuelCount > 1 ? " §7x" + fuelCount : "");

        if (displays.size() >= 2) {
            displays.get(0).setCustomName(topText);
            displays.get(1).setCustomName(bottomText);
        }
    }
    
    public void cleanup() {
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        for (List<ArmorStand> displays : displayMap.values()) {
            for (ArmorStand display : displays) {
                display.remove();
            }
        }
        displayMap.clear();
    }
    
    private List<Block> findAllNearbyFurnaces(Location center, int radius) {
        List<Block> furnaces = new ArrayList<>();
        int minX = center.getBlockX() - radius;
        int maxX = center.getBlockX() + radius;
        int minY = Math.max(center.getBlockY() - radius, 0);
        int maxY = Math.min(center.getBlockY() + radius, center.getWorld().getMaxHeight());
        int minZ = center.getBlockZ() - radius;
        int maxZ = center.getBlockZ() + radius;
        
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = center.getWorld().getBlockAt(x, y, z);
                    if (isFurnace(block.getType())) {
                        furnaces.add(block);
                    }
                }
            }
        }
        
        return furnaces;
    }
    
    private boolean isFurnace(Material material) {
        return material == Material.FURNACE || material == Material.BLAST_FURNACE || material == Material.SMOKER;
    }
    
    private String getFurnaceName(Material material) {
        if (material == Material.FURNACE) {
            return "熔炉";
        } else if (material == Material.BLAST_FURNACE) {
            return "高炉";
        } else if (material == Material.SMOKER) {
            return "烟熏炉";
        }
        return material.toString().toLowerCase().replace("_", " ");
    }
    
    private String getItemName(ItemStack item) {
        return fun.eqad.bestdisplay.util.NameUtil.getItemName(item);
    }
    
    private boolean hasBlockAbove(Location location) {
        Location above = location.clone().add(0, 1, 0);
        return above.getBlock().getType() != Material.AIR;
    }

    private boolean hasBlockBelow(Location location) {
        Location below = location.clone().add(0, -1, 0);
        return below.getBlock().getType() != Material.AIR;
    }
}