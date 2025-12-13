package fun.eqad.bestdisplay.block;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class FurnaceEvent implements Listener {
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
        updateTask.runTaskTimer(plugin, 0L, 10L);
    }
    
    private void updateAllDisplays() {
        Map<Location, List<ArmorStand>> displayMapCopy = new HashMap<>(displayMap);
        
        for (Map.Entry<Location, List<ArmorStand>> entry : displayMapCopy.entrySet()) {
            Location furnaceLocation = entry.getKey();
            List<ArmorStand> displays = entry.getValue();

            Block block = furnaceLocation.getBlock();
            if (!isFurnace(block.getType())) {
                for (ArmorStand display : displays) {
                    display.remove();
                }
                displayMap.remove(furnaceLocation);
                continue;
            }

            String furnaceName = getFurnaceName(block.getType());
            String smeltingItem = "无";
            int unsmeltedCount = 0;
            
            if (block.getState() instanceof org.bukkit.block.Furnace) {
                org.bukkit.block.Furnace furnaceState = (org.bukkit.block.Furnace) block.getState();
                FurnaceInventory inventory = furnaceState.getInventory();
                
                ItemStack smelting = inventory.getSmelting();
                if (smelting != null && smelting.getType() != Material.AIR) {
                    smeltingItem = getItemName(smelting);
                    unsmeltedCount = smelting.getAmount();
                }
            }

            String topText = furnaceName;
            String middleText = "§7物品: §f" + smeltingItem;
            String bottomText = "§7数量: §f" + unsmeltedCount;

            if (displays.size() >= 3) {
                displays.get(0).setCustomName(topText);
                displays.get(1).setCustomName(middleText);
                displays.get(2).setCustomName(bottomText);
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().shouldFurnaceDisplay()) return;

        Player player = event.getPlayer();
        Location playerLoc = player.getLocation();
        List<Block> nearbyFurnaces = findAllNearbyFurnaces(playerLoc, 4);
        
        if (nearbyFurnaces.isEmpty()) return;
        
        for (Block furnace : nearbyFurnaces) {
            Location furnaceLocation = furnace.getLocation();
            Material material = furnace.getType();

            if (isFurnace(material)) {
                if (displayMap.containsKey(furnaceLocation)) {
                    continue;
                }

                String furnaceName = getFurnaceName(material);
                boolean hasBlockAbove = hasBlockAbove(furnaceLocation);
                boolean hasBlockBelow = hasBlockBelow(furnaceLocation);

                if (hasBlockAbove && hasBlockBelow) {
                    continue;
                }

                String smeltingItem = "无";
                int unsmeltedCount = 0;
                
                if (furnace.getState() instanceof org.bukkit.block.Furnace) {
                    org.bukkit.block.Furnace furnaceState = (org.bukkit.block.Furnace) furnace.getState();
                    FurnaceInventory inventory = furnaceState.getInventory();
                    
                    ItemStack smelting = inventory.getSmelting();
                    if (smelting != null && smelting.getType() != Material.AIR) {
                        smeltingItem = getItemName(smelting);
                        unsmeltedCount = smelting.getAmount();
                    }
                }
                
                String topText = furnaceName;
                String middleText = "§7物品: §f" + smeltingItem;
                String bottomText = "§7数量: §f" + unsmeltedCount;

                Location topDisplayLocation, middleDisplayLocation, bottomDisplayLocation;
                
                if (hasBlockAbove) {
                    topDisplayLocation = furnaceLocation.clone().add(0.5, -0.5, 0.5);
                    middleDisplayLocation = furnaceLocation.clone().add(0.5, -0.8, 0.5);
                    bottomDisplayLocation = furnaceLocation.clone().add(0.5, -1.1, 0.5);
                } else {
                    topDisplayLocation = furnaceLocation.clone().add(0.5, 1.6, 0.5);
                    middleDisplayLocation = furnaceLocation.clone().add(0.5, 1.3, 0.5);
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
                
                ArmorStand middleDisplay = furnace.getWorld().spawn(middleDisplayLocation, ArmorStand.class, armorStand -> {
                    armorStand.setVisible(false);
                    armorStand.setGravity(false);
                    armorStand.setInvulnerable(true);
                    armorStand.setCustomNameVisible(true);
                    armorStand.setMarker(true);
                    armorStand.setSmall(true);
                    armorStand.setCustomName(middleText);
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
                displays.add(middleDisplay);
                displays.add(bottomDisplay);
                displayMap.put(furnaceLocation, displays);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (displayMap.containsKey(furnaceLocation)) {
                            for (ArmorStand display : displayMap.get(furnaceLocation)) {
                                display.remove();
                            }
                            displayMap.remove(furnaceLocation);
                        }
                    }
                }.runTaskLater(plugin, 20L * 3);
            }
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
        return fun.eqad.bestdisplay.Util.NameUtil.getItemName(item);
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