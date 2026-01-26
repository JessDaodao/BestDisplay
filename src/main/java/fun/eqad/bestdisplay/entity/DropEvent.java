package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class DropEvent {
    private final BestDisplay plugin;
    private final Set<UUID> lastVisibleItems = new HashSet<>();
    private BukkitRunnable updateTask;

    public DropEvent(BestDisplay plugin) {
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
        if (!plugin.getConfigManager().shouldDropDisplay()) return;

        Set<UUID> currentVisibleItems = new HashSet<>();
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        double radius = plugin.getConfigManager().getPlayerRadius();

        for (Player player : players) {
            Location playerLoc = player.getLocation();

            for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
                if (!(entity instanceof Item)) continue;

                Item item = (Item) entity;
                if (currentVisibleItems.contains(item.getUniqueId())) continue;

                if (!item.isValid() || item.isDead()) continue;

                currentVisibleItems.add(item.getUniqueId());
                updateItemDisplay(item);
            }
        }

        for (UUID uuid : lastVisibleItems) {
            if (!currentVisibleItems.contains(uuid)) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity instanceof Item && entity.isValid()) {
                    entity.setCustomNameVisible(false);
                }
            }
        }

        lastVisibleItems.clear();
        lastVisibleItems.addAll(currentVisibleItems);
    }

    private void updateItemDisplay(Item item) {
        ItemStack itemStack = item.getItemStack();
        String itemName = plugin.getNameUtil().getItemName(itemStack);
        int amount = itemStack.getAmount();

        int remainingTicks = 6000 - item.getTicksLived();
        if (remainingTicks < 0) remainingTicks = 0;
        int totalSeconds = remainingTicks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String timeStr = String.format("%02d:%02d", minutes, seconds);

        String displayName = (amount > 1 ? itemName + " §7x" + amount : itemName) + " §8[§7" + timeStr + "§8]";
        
        item.setCustomName(displayName);
        item.setCustomNameVisible(true);
    }
}