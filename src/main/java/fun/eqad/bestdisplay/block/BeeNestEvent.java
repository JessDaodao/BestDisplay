package fun.eqad.bestdisplay.block;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class BeeNestEvent implements Listener {
    private final BestDisplay plugin;
    private final Map<Location, List<ArmorStand>> displayMap = new HashMap<>();
    
    public BeeNestEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().shouldBeeNestDisplay()) return;

        Player player = event.getPlayer();

        Location playerLoc = player.getLocation();
        List<Block> nearbyBeeNests = findAllNearbyBeeNests(playerLoc, 4);
        
        if (nearbyBeeNests.isEmpty()) return;
        
        for (Block beeNest : nearbyBeeNests) {
            Location nestLocation = beeNest.getLocation();
            Material material = beeNest.getType();

            if (isBeeNest(material)) {
                if (displayMap.containsKey(nestLocation)) {
                    continue;
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

                String nestName = getBeeNestName(material);

                boolean hasBlockAbove = hasBlockAbove(nestLocation);
                boolean hasBlockBelow = hasBlockBelow(nestLocation);
                
                if (hasBlockAbove && hasBlockBelow) {
                    continue;
                }
                
                String topText = nestName;
                String bottomText;

                if (honeyPercentage >= 100) {
                    bottomText = "§7(§f蜜蜂数量: " + beeCount + "§7| §f储蜜量: §a已满§7)";
                } else {
                    bottomText = "§7(§f蜜蜂数量: " + beeCount + "§7| §f储蜜量: " + honeyPercentage + "%§7)";
                }

                Location topDisplayLocation, bottomDisplayLocation;
                
                if (hasBlockAbove) {
                    topDisplayLocation = nestLocation.clone().add(0.5, -0.8, 0.5);
                    bottomDisplayLocation = nestLocation.clone().add(0.5, -1.1, 0.5);
                } else {
                    topDisplayLocation = nestLocation.clone().add(0.5, 1.4, 0.5);
                    bottomDisplayLocation = nestLocation.clone().add(0.5, 1.1, 0.5);
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
                displays.add(bottomDisplay);
                displayMap.put(nestLocation, displays);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (displayMap.containsKey(nestLocation)) {
                            for (ArmorStand display : displayMap.get(nestLocation)) {
                                display.remove();
                            }
                            displayMap.remove(nestLocation);
                        }
                    }
                }.runTaskLater(plugin, 20L * 3);
            }
        }
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
    
    private boolean hasBlockBelow(Location location) {
        Location below = location.clone().add(0, -1, 0);
        return below.getBlock().getType() != Material.AIR;
    }
}
