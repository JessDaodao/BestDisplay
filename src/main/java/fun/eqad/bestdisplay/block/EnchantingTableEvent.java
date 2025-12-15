package fun.eqad.bestdisplay.block;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class EnchantingTableEvent {
    private final BestDisplay plugin;
    private final Map<Location, List<ArmorStand>> displayMap = new HashMap<>();
    private BukkitRunnable updateTask;
    
    public EnchantingTableEvent(BestDisplay plugin) {
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
        if (!plugin.getConfigManager().shouldEnchantingTableDisplay()) return;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        List<Player> playerList = new ArrayList<>(players);

        Map<Location, List<ArmorStand>> displayMapCopy = new HashMap<>(displayMap);
        for (Map.Entry<Location, List<ArmorStand>> entry : displayMapCopy.entrySet()) {
            Location tableLocation = entry.getKey();
            boolean hasNearbyPlayer = false;

            for (Player player : playerList) {
                if (player.getWorld().equals(tableLocation.getWorld()) && 
                    player.getLocation().distance(tableLocation) <= plugin.getConfigManager().getPlayerRadius()) {
                    hasNearbyPlayer = true;
                    break;
                }
            }

            if (!hasNearbyPlayer) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(tableLocation);
                continue;
            }

            Block block = tableLocation.getBlock();
            if (!isEnchantingTable(block.getType())) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(tableLocation);
                continue;
            }

            updateEnchantingTableDisplay(block, block.getType(), entry.getValue());
        }

        for (Player player : playerList) {
            Location playerLoc = player.getLocation();
            List<Block> nearbyTables = findAllNearbyEnchantingTables(playerLoc, plugin.getConfigManager().getPlayerRadius());
            
            for (Block table : nearbyTables) {
                Location tableLocation = table.getLocation();

                if (displayMap.containsKey(tableLocation)) {
                    continue;
                }
                
                Material material = table.getType();
                if (isEnchantingTable(material)) {
                    displayEnchantingTableInfo(table, material);
                }
            }
        }
    }
    
    private void displayEnchantingTableInfo(Block table, Material material) {
        Location tableLocation = table.getLocation();
        String tableName = getEnchantingTableName(material);
        boolean hasBlockAbove = hasBlockAbove(tableLocation);
        boolean hasBlockBelow = hasBlockBelow(tableLocation);
        
        if (hasBlockAbove && hasBlockBelow) {
            return;
        }

        String maxEnchantLevel = getMaxEnchantLevel(tableLocation);
        
        String topText = tableName;
        String levelColor = maxEnchantLevel.equals("30") ? "§a" : "§f";
        String bottomText = "§7等级: " + levelColor + maxEnchantLevel;

        Location topDisplayLocation, bottomDisplayLocation;
        
        if (hasBlockAbove) {
            topDisplayLocation = tableLocation.clone().add(0.5, -0.5, 0.5);
            bottomDisplayLocation = tableLocation.clone().add(0.5, -0.8, 0.5);
        } else {
            topDisplayLocation = tableLocation.clone().add(0.5, 1.6, 0.5);
            bottomDisplayLocation = tableLocation.clone().add(0.5, 1.3, 0.5);
        }
        
        ArmorStand topDisplay = table.getWorld().spawn(topDisplayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(topText);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        ArmorStand bottomDisplay = table.getWorld().spawn(bottomDisplayLocation, ArmorStand.class, armorStand -> {
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
        displayMap.put(tableLocation, displays);
    }
    
    private void updateEnchantingTableDisplay(Block table, Material material, List<ArmorStand> displays) {
        Location tableLocation = table.getLocation();
        String tableName = getEnchantingTableName(material);
        String maxEnchantLevel = getMaxEnchantLevel(tableLocation);

        String topText = tableName;
        String levelColor = maxEnchantLevel.equals("30") ? "§a" : "§f";
        String bottomText = "§7等级: " + levelColor + maxEnchantLevel;

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
    
    private List<Block> findAllNearbyEnchantingTables(Location center, int radius) {
        List<Block> tables = new ArrayList<>();
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
                    if (isEnchantingTable(block.getType())) {
                        tables.add(block);
                    }
                }
            }
        }
        
        return tables;
    }
    
    private boolean isEnchantingTable(Material material) {
        return material == Material.ENCHANTING_TABLE;
    }
    
    private String getEnchantingTableName(Material material) {
        if (material == Material.ENCHANTING_TABLE) {
            return "附魔台";
        }
        return material.toString().toLowerCase().replace("_", " ");
    }
    
    private String getMaxEnchantLevel(Location tableLocation) {
        int bookshelfCount = countBookshelves(tableLocation);
        
        int effectiveBookshelves = Math.min(15, bookshelfCount);
        int maxLevel = (int) Math.ceil(effectiveBookshelves * 2.0);
        
        return String.valueOf(maxLevel);
    }
    
    private int countBookshelves(Location tableLocation) {
        int count = 0;
        int x = tableLocation.getBlockX();
        int y = tableLocation.getBlockY();
        int z = tableLocation.getBlockZ();
        World world = tableLocation.getWorld();
        
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                    continue;
                }
                
                Block block = world.getBlockAt(x + dx, y, z + dz);
                if (block.getType() == Material.BOOKSHELF) {
                    if (!isBlocked(tableLocation, x + dx, y, z + dz, false)) {
                        count++;
                    }
                }
            }
        }
        
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (Math.abs(dx) < 2 && Math.abs(dz) < 2) {
                    continue;
                }
                
                Block block = world.getBlockAt(x + dx, y + 1, z + dz);
                if (block.getType() == Material.BOOKSHELF) {
                    if (!isBlocked(tableLocation, x + dx, y + 1, z + dz, true)) {
                        count++;
                    }
                }
            }
        }
        
        return count;
    }
    
    private boolean isBlocked(Location tableLocation, int bookX, int bookY, int bookZ, boolean isUpperLayer) {
        int tableX = tableLocation.getBlockX();
        int tableZ = tableLocation.getBlockZ();
        World world = tableLocation.getWorld();
        
        boolean isCornerBookshelf = (Math.abs(bookX - tableX) == 2 && Math.abs(bookZ - tableZ) == 2);
        
        boolean isAdjacentCornerBlocker = (Math.abs(bookX - tableX) == 1 && Math.abs(bookZ - tableZ) == 1);
        
        if (isAdjacentCornerBlocker && !isCornerBookshelf) {
            return false;
        }
        
        if (!isUpperLayer) {
            int dx = bookX - tableX;
            int dz = bookZ - tableZ;
            
            int steps = Math.max(Math.abs(dx), Math.abs(dz));
            for (int i = 1; i < steps; i++) {
                int checkX = tableX + (dx * i / steps);
                int checkZ = tableZ + (dz * i / steps);
                
                boolean checkIsAdjacentCornerBlocker = (Math.abs(checkX - tableX) == 1 && Math.abs(checkZ - tableZ) == 1);
                
                if (!checkIsAdjacentCornerBlocker || isCornerBookshelf) {
                    if (world.getBlockAt(checkX, bookY, checkZ).getType() != Material.AIR) {
                        return true;
                    }
                }
            }
        }
        
        if (isCornerBookshelf) {
            int cornerX = tableX + (bookX > tableX ? 1 : -1);
            int cornerZ = tableZ + (bookZ > tableZ ? 1 : -1);
            
            if (world.getBlockAt(cornerX, bookY, cornerZ).getType() != Material.AIR) {
                return true;
            }
        }
        
        if (!isCornerBookshelf) {
            if (Math.abs(bookX - tableX) == 2 && bookZ == tableZ) {
                int checkX = tableX + (bookX > tableX ? 1 : -1);
                for (int dz = -1; dz <= 1; dz++) {
                    boolean checkIsAdjacentCornerBlocker = (Math.abs(checkX - tableX) == 1 && Math.abs(tableZ + dz - tableZ) == 1);
                    
                    if (!checkIsAdjacentCornerBlocker) {
                        if (world.getBlockAt(checkX, bookY, tableZ + dz).getType() != Material.AIR) {
                            return true;
                        }
                    }
                }
            }
            
            if (Math.abs(bookZ - tableZ) == 2 && bookX == tableX) {
                int checkZ = tableZ + (bookZ > tableZ ? 1 : -1);
                for (int dx = -1; dx <= 1; dx++) {
                    boolean checkIsAdjacentCornerBlocker = (Math.abs(tableX + dx - tableX) == 1 && Math.abs(checkZ - tableZ) == 1);
                    
                    if (!checkIsAdjacentCornerBlocker) {
                        if (world.getBlockAt(tableX + dx, bookY, checkZ).getType() != Material.AIR) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
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