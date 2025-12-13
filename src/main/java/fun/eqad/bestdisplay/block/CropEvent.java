package fun.eqad.bestdisplay.block;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class CropEvent implements Listener {
    private final BestDisplay plugin;
    private final Map<Location, ArmorStand> displayMap = new HashMap<>();
    
    public CropEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().shouldCropDisplay()) return;

        Player player = event.getPlayer();

        Location playerLoc = player.getLocation();
        List<Block> nearbyCrops = findAllNearbyCrops(playerLoc, 3);
        
        if (nearbyCrops.isEmpty()) return;
        
        for (Block crop : nearbyCrops) {
            Location cropLocation = crop.getLocation();
            Material material = crop.getType();

            if (isCrop(material)) {
                if (displayMap.containsKey(cropLocation)) {
                    continue;
                }
                
                Ageable ageable = (Ageable) crop.getBlockData();
                int age = ageable.getAge();
                int maxAge = ageable.getMaximumAge();

                int percentage = (int) ((double) age / maxAge * 100);

                String cropName = getCropName(material);

                String displayText;

                if (percentage >= 100) {
                    displayText = cropName + " §7(§a已成熟§7)";
                } else {
                    displayText = cropName + " §7(§f" + percentage + "%§7)";
                }

                Location displayLocation = cropLocation.clone().add(0.5, 1.6, 0.5);
                
                ArmorStand display = crop.getWorld().spawn(displayLocation, ArmorStand.class, armorStand -> {
                    armorStand.setVisible(false);
                    armorStand.setGravity(false);
                    armorStand.setInvulnerable(true);
                    armorStand.setCustomNameVisible(true);
                    armorStand.setMarker(true);
                    armorStand.setSmall(true);
                    armorStand.setCustomName(displayText);
                    armorStand.addScoreboardTag("BestDisplay");
                });
                
                displayMap.put(cropLocation, display);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (displayMap.containsKey(cropLocation)) {
                            displayMap.get(cropLocation).remove();
                            displayMap.remove(cropLocation);
                        }
                    }
                }.runTaskLater(plugin, 20L * 3);
            }
        }
    }
    
    private List<Block> findAllNearbyCrops(Location center, int radius) {
        List<Block> crops = new ArrayList<>();
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
                    if (isCrop(block.getType())) {
                        crops.add(block);
                    }
                }
            }
        }
        
        return crops;
    }
    
    private boolean isCrop(Material material) {
        switch (material) {
            case WHEAT:
            case CARROTS:
            case POTATOES:
            case BEETROOTS:
            case NETHER_WART:
            case COCOA:
            case SWEET_BERRY_BUSH:
            case PUMPKIN_STEM:
            case MELON_STEM:
            case SUGAR_CANE:
            case KELP:
            case TWISTING_VINES:
            case WEEPING_VINES:
            case CAVE_VINES:
            case GLOW_BERRIES:
            case MANGROVE_PROPAGULE:
            case PITCHER_CROP:
            case TORCHFLOWER_CROP:
                return true;
            default:
                return false;
        }
    }
    
    private String getCropName(Material material) {
        switch (material) {
            case WHEAT: break;
            case CARROTS: break;
            case POTATOES: break;
            case BEETROOTS: break;
            case NETHER_WART: break;
            case COCOA: break;
            case SWEET_BERRY_BUSH: break;
            case PUMPKIN_STEM: break;
            case MELON_STEM: break;
            case SUGAR_CANE: break;
            case KELP: break;
            case TWISTING_VINES: break;
            case WEEPING_VINES: break;
            case CAVE_VINES: break;
            case GLOW_BERRIES: break;
            case MANGROVE_PROPAGULE: break;
            case PITCHER_CROP: break;
            case TORCHFLOWER_CROP: break;
            default:
                return material.toString().toLowerCase().replace("_", " ");
        }
        
        return plugin.getNameUtil().getMaterialName(material);
    }
}