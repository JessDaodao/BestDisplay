package fun.eqad.bestdisplay.block;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class BeeNestEvent {
    private final BestDisplay plugin;
    private final Map<Location, List<ArmorStand>> displayMap = new HashMap<>();
    private BukkitRunnable updateTask;
    
    public BeeNestEvent(BestDisplay plugin) {
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
        updateTask.runTaskTimer(plugin, 5L, 5L);
    }
    
    private void updateAllDisplays() {
        if (!plugin.getConfigManager().shouldBeeNestDisplay()) return;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        List<Player> playerList = new ArrayList<>(players);

        Map<Location, List<ArmorStand>> displayMapCopy = new HashMap<>(displayMap);
        for (Map.Entry<Location, List<ArmorStand>> entry : displayMapCopy.entrySet()) {
            Location nestLocation = entry.getKey();
            boolean hasNearbyPlayer = false;

            for (Player player : playerList) {
                if (player.getWorld().equals(nestLocation.getWorld()) && 
                    player.getLocation().distance(nestLocation) <= plugin.getConfigManager().getPlayerRadius()) {
                    hasNearbyPlayer = true;
                    break;
                }
            }

            if (!hasNearbyPlayer) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(nestLocation);
            } else {
                Block block = nestLocation.getBlock();
                if (isBeeNest(block.getType())) {
                    updateBeeNestDisplay(block, block.getType(), entry.getValue());
                } else {
                    for (ArmorStand display : entry.getValue()) {
                        display.remove();
                    }
                    displayMap.remove(nestLocation);
                }
            }
        }

        for (Player player : playerList) {
            Location playerLoc = player.getLocation();
            List<Block> nearbyBeeNests = findAllNearbyBeeNests(playerLoc, plugin.getConfigManager().getPlayerRadius());
            
            for (Block beeNest : nearbyBeeNests) {
                Location nestLocation = beeNest.getLocation();

                if (displayMap.containsKey(nestLocation)) {
                    continue;
                }
                
                Material material = beeNest.getType();
                if (isBeeNest(material)) {
                    displayBeeNestInfo(beeNest, material);
                }
            }
        }
    }
    
    private void displayBeeNestInfo(Block beeNest, Material material) {
        Location nestLocation = beeNest.getLocation();
        String nestName = getBeeNestName(material);
        boolean hasBlockAbove = hasBlockAbove(nestLocation);
        boolean hasBlockBelow = hasBlockBelow(nestLocation);
        
        if (hasBlockAbove && hasBlockBelow) {
            return;
        }

        int beeCount = 0;
        int honeyLevel;
        int honeyPercentage = 0;
        
        if (beeNest.getState() instanceof org.bukkit.block.Beehive) {
            org.bukkit.block.Beehive hiveState = (org.bukkit.block.Beehive) beeNest.getState();
            beeCount = hiveState.getEntityCount();
            
            try {
                org.bukkit.block.data.BlockData blockData = beeNest.getBlockData();
                
                if (blockData instanceof org.bukkit.block.data.type.Beehive) {
                    java.lang.reflect.Method getHoneyLevel = blockData.getClass().getMethod("getHoneyLevel");
                    honeyLevel = (int) getHoneyLevel.invoke(blockData);
                    int maxHoneyLevel = 5;
                    honeyPercentage = (int) ((double) honeyLevel / maxHoneyLevel * 100);
                }
            } catch (Exception e) {
                return;
            }
        }
        
        String topText = nestName;
        String middleText = "§7蜜蜂数量: §f" + beeCount;
        String bottomText;
        
        if (honeyPercentage >= 100) {
            bottomText = "§7储蜜量: §a已满";
        } else {
            bottomText = "§7储蜜量: §f" + honeyPercentage + "%";
        }

        Location topDisplayLocation, middleDisplayLocation, bottomDisplayLocation;
        
        if (hasBlockAbove) {
            topDisplayLocation = nestLocation.clone().add(0.5, -0.6, 0.5);
            middleDisplayLocation = nestLocation.clone().add(0.5, -0.9, 0.5);
            bottomDisplayLocation = nestLocation.clone().add(0.5, -1.2, 0.5);
        } else {
            topDisplayLocation = nestLocation.clone().add(0.5, 1.6, 0.5);
            middleDisplayLocation = nestLocation.clone().add(0.5, 1.3, 0.5);
            bottomDisplayLocation = nestLocation.clone().add(0.5, 1.0, 0.5);
        }
        
        ArmorStand topDisplay = beeNest.getWorld().spawn(topDisplayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(topText);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        ArmorStand middleDisplay = beeNest.getWorld().spawn(middleDisplayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(middleText);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        ArmorStand bottomDisplay = beeNest.getWorld().spawn(bottomDisplayLocation, ArmorStand.class, armorStand -> {
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
        displayMap.put(nestLocation, displays);
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
    
    private List<Block> findAllNearbyBeeNests(Location center, int radius) {
        List<Block> beeNests = new ArrayList<>();
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
                    if (isBeeNest(block.getType())) {
                        beeNests.add(block);
                    }
                }
            }
        }
        
        return beeNests;
    }
    
    private boolean isBeeNest(Material material) {
        return material == Material.BEE_NEST || material == Material.BEEHIVE;
    }
    
    private String getBeeNestName(Material material) {
        if (material == Material.BEE_NEST) {
            return "蜂巢";
        } else if (material == Material.BEEHIVE) {
            return "蜂箱";
        }
        return material.toString().toLowerCase().replace("_", " ");
    }
    
    private boolean hasBlockAbove(Location location) {
        Location above = location.clone().add(0, 1, 0);
        return above.getBlock().getType() != Material.AIR;
    }
    
    private void updateBeeNestDisplay(Block beeNest, Material material, List<ArmorStand> displays) {
        String nestName = getBeeNestName(material);
        
        int beeCount = 0;
        int honeyLevel;
        int honeyPercentage = 0;
        
        if (beeNest.getState() instanceof org.bukkit.block.Beehive) {
            org.bukkit.block.Beehive hiveState = (org.bukkit.block.Beehive) beeNest.getState();
            beeCount = hiveState.getEntityCount();
            
            try {
                org.bukkit.block.data.BlockData blockData = beeNest.getBlockData();
                
                if (blockData instanceof org.bukkit.block.data.type.Beehive) {
                    java.lang.reflect.Method getHoneyLevel = blockData.getClass().getMethod("getHoneyLevel");
                    honeyLevel = (int) getHoneyLevel.invoke(blockData);
                    int maxHoneyLevel = 5;
                    honeyPercentage = (int) ((double) honeyLevel / maxHoneyLevel * 100);
                }
            } catch (Exception e) {
                return;
            }
        }
        
        String topText = nestName;
        String middleText = "§7蜜蜂数量: §f" + beeCount;
        String bottomText;
        
        if (honeyPercentage >= 100) {
            bottomText = "§7储蜜量: §a已满";
        } else {
            bottomText = "§7储蜜量: §f" + honeyPercentage + "%";
        }

        if (displays.size() >= 3) {
            displays.get(0).setCustomName(topText);
            displays.get(1).setCustomName(middleText);
            displays.get(2).setCustomName(bottomText);
        }
    }
    
    private boolean hasBlockBelow(Location location) {
        Location below = location.clone().add(0, -1, 0);
        return below.getBlock().getType() != Material.AIR;
    }
}