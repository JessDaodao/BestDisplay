package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class TNTEvent {
    private final BestDisplay plugin;
    private final Set<UUID> lastVisibleTNTs = new HashSet<>();
    private BukkitRunnable updateTask;

    public TNTEvent(BestDisplay plugin) {
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
        updateTask.runTaskTimer(plugin, 0L, 5L);
    }

    private void updateAllDisplays() {
        if (!plugin.getConfigManager().shouldTNTDisplay()) return;

        Set<UUID> currentVisibleTNTs = new HashSet<>();
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        double radius = plugin.getConfigManager().getPlayerRadius();

        for (Player player : players) {
            Location playerLoc = player.getLocation();
            
            for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
                if (entity.getType() != EntityType.PRIMED_TNT) continue;
                
                TNTPrimed tnt = (TNTPrimed) entity;
                if (currentVisibleTNTs.contains(tnt.getUniqueId())) continue;

                if (!tnt.isValid() || tnt.isDead()) continue;

                currentVisibleTNTs.add(tnt.getUniqueId());
                updateTNTDisplay(tnt);
            }
        }

        for (UUID uuid : lastVisibleTNTs) {
            if (!currentVisibleTNTs.contains(uuid)) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity instanceof TNTPrimed && entity.isValid()) {
                    entity.setCustomNameVisible(false);
                }
            }
        }

        lastVisibleTNTs.clear();
        lastVisibleTNTs.addAll(currentVisibleTNTs);
    }

    private void updateTNTDisplay(TNTPrimed tnt) {
        int fuseTicks = tnt.getFuseTicks();
        int remainingSeconds = (int) Math.ceil(fuseTicks / 20.0);
        
        String displayText = "§c" + remainingSeconds;

        if (remainingSeconds <= 1) {
            displayText = "§4§lBOOM!";
        } else if (remainingSeconds <= 2) {
            displayText = "§4" + remainingSeconds;
        }

        tnt.setCustomName(displayText);
        tnt.setCustomNameVisible(true);
    }
    
    public void cleanup() {
        if (updateTask != null) {
            updateTask.cancel();
        }
    }
}