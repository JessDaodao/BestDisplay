package fun.eqad.bestdisplay.block;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class CropEvent {
    private final BestDisplay plugin;
    private final Map<Location, List<ArmorStand>> displayMap = new HashMap<>();
    private BukkitRunnable updateTask;
    
    public CropEvent(BestDisplay plugin) {
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
        updateTask.runTaskTimer(plugin, 0L, 2L);
    }
    
    private void updateAllDisplays() {
        if (!plugin.getConfigManager().shouldCropDisplay()) return;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        List<Player> playerList = new ArrayList<>(players);

        Map<Location, List<ArmorStand>> displayMapCopy = new HashMap<>(displayMap);
        for (Map.Entry<Location, List<ArmorStand>> entry : displayMapCopy.entrySet()) {
            Location cropLocation = entry.getKey();
            boolean hasNearbyPlayer = false;

            for (Player player : playerList) {
                if (player.getWorld().equals(cropLocation.getWorld()) && 
                    player.getLocation().distance(cropLocation) <= plugin.getConfigManager().getPlayerRadius()) {
                    hasNearbyPlayer = true;
                    break;
                }
            }

            if (!hasNearbyPlayer) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(cropLocation);
            }
        }

        for (Player player : playerList) {
            Location playerLoc = player.getLocation();
            List<Block> nearbyCrops = findAllNearbyCrops(playerLoc, plugin.getConfigManager().getPlayerRadius());
            
            for (Block crop : nearbyCrops) {
                Location cropLocation = crop.getLocation();

                if (displayMap.containsKey(cropLocation)) {
                    continue;
                }
                
                Material material = crop.getType();
                if (isCrop(material)) {
                    displayCropInfo(crop, material);
                }
            }
        }
    }
    
    private void displayCropInfo(Block crop, Material material) {
        Location cropLocation = crop.getLocation();
        
        Ageable ageable = (Ageable) crop.getBlockData();
        int age = ageable.getAge();
        int maxAge = ageable.getMaximumAge();

        int percentage = (int) ((double) age / maxAge * 100);

        String cropName = getCropName(material);

        String topText = cropName;
        String bottomText;

        if (percentage >= 100) {
            bottomText = "§a已成熟";
        } else {
            bottomText = "§7" + percentage + "%";
        }

        Location topDisplayLocation = cropLocation.clone().add(0.5, 1.8, 0.5);
        Location bottomDisplayLocation = cropLocation.clone().add(0.5, 1.5, 0.5);
        
        ArmorStand topDisplay = crop.getWorld().spawn(topDisplayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(topText);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        ArmorStand bottomDisplay = crop.getWorld().spawn(bottomDisplayLocation, ArmorStand.class, armorStand -> {
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
        displayMap.put(cropLocation, displays);
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