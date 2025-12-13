package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class TNTEvent implements Listener {
    private final BestDisplay plugin;
    private final Map<Entity, ArmorStand> displayMap = new HashMap<>();
    private final Map<Entity, BukkitRunnable> countdownTasks = new HashMap<>();
    
    public TNTEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!plugin.getConfigManager().shouldTNTDisplay()) return;

        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        
        Entity tnt = event.getEntity();
        Location tntLocation = tnt.getLocation();

        Location displayLocation = tntLocation.clone().add(0.0, 1.2, 0.0);
        
        ArmorStand display = tnt.getWorld().spawn(displayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);

            armorStand.setCustomName("§c-");
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        displayMap.put(tnt, display);

        int fuseTicks;
        try {
            if (tnt instanceof org.bukkit.entity.TNTPrimed) {
                fuseTicks = ((org.bukkit.entity.TNTPrimed) tnt).getFuseTicks();
            } else {
                return;
            }
        } catch (Exception e) {
            return;
        }

        int finalFuseTicks = fuseTicks;
        BukkitRunnable countdownTask = new BukkitRunnable() {
            int remainingTicks = finalFuseTicks;

            @Override
            public void run() {
                if (!tnt.isValid() || remainingTicks <= 0) {
                    this.cancel();
                    if (displayMap.containsKey(tnt)) {
                        displayMap.get(tnt).remove();
                        displayMap.remove(tnt);
                    }
                    countdownTasks.remove(tnt);
                    return;
                }

                int remainingSeconds = (int) Math.ceil(remainingTicks / 20.0);
                String displayText = "§c" + remainingSeconds;

                if (remainingSeconds <= 1) {
                    displayText = "§4§lBOOM!";
                } else if (remainingSeconds <= 2) {
                    displayText = "§4" + remainingSeconds;
                }

                display.setCustomName(displayText);

                if (tnt.isValid()) {
                    Location newLocation = tnt.getLocation().clone().add(0.0, 1.2, 0.0);
                    display.teleport(newLocation);
                }

                remainingTicks -= 2;
            }
        };

        countdownTask.runTaskTimer(plugin, 0L, 2L);
        countdownTasks.put(tnt, countdownTask);
    }

    @EventHandler
    public void onTNTExplode(EntityExplodeEvent event) {
        if (event.getEntityType() != EntityType.PRIMED_TNT) {
            return;
        }
        
        Entity tnt = event.getEntity();

        if (displayMap.containsKey(tnt)) {
            displayMap.get(tnt).remove();
            displayMap.remove(tnt);
        }
        
        if (countdownTasks.containsKey(tnt)) {
            countdownTasks.get(tnt).cancel();
            countdownTasks.remove(tnt);
        }
    }
    
    public void cleanup() {
        for (ArmorStand display : displayMap.values()) {
            display.remove();
        }
        displayMap.clear();
        
        for (BukkitRunnable task : countdownTasks.values()) {
            task.cancel();
        }
        countdownTasks.clear();
    }
}
