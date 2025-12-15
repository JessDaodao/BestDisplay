package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class DropEvent {
    private final BestDisplay plugin;
    private final Map<UUID, ArmorStand> displayMap = new HashMap<>();
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
        updateTask.runTaskTimer(plugin, 0L, 1L);
    }

    private void updateAllDisplays() {
        if (!plugin.getConfigManager().shouldDropDisplay()) return;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        List<Player> playerList = new ArrayList<>(players);

        Map<UUID, ArmorStand> displayMapCopy = new HashMap<>(displayMap);
        for (Map.Entry<UUID, ArmorStand> entry : displayMapCopy.entrySet()) {
            UUID itemId = entry.getKey();
            boolean hasNearbyPlayer = false;

            Item item = null;
            Entity entity = Bukkit.getEntity(itemId);
            if (entity instanceof Item) {
                item = (Item) entity;
            }

            if (item == null || item.isDead() || !item.isValid()) {
                entry.getValue().remove();
                displayMap.remove(itemId);
                continue;
            }

            for (Player player : playerList) {
                if (player.getWorld().equals(item.getWorld()) &&
                        player.getLocation().distance(item.getLocation()) <= plugin.getConfigManager().getPlayerRadius()) {
                    hasNearbyPlayer = true;
                    break;
                }
            }

            if (!hasNearbyPlayer) {
                entry.getValue().remove();
                displayMap.remove(itemId);
                continue;
            }

            ArmorStand display = entry.getValue();
            if (display.isValid()) {
                Location currentLocation = item.getLocation();
                display.teleport(currentLocation.clone().add(0, 0.5, 0));

                ItemStack currentItemStack = item.getItemStack();
                String currentItemName = plugin.getNameUtil().getItemName(currentItemStack);
                int currentAmount = currentItemStack.getAmount();
                String currentDisplayName = currentAmount > 1 ? currentItemName + " §7x" + currentAmount : currentItemName;
                display.setCustomName(currentDisplayName);
                display.setCustomNameVisible(true);
            }
        }

        for (Player player : playerList) {
            Location playerLoc = player.getLocation();

            for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc,
                    plugin.getConfigManager().getPlayerRadius(),
                    plugin.getConfigManager().getPlayerRadius(),
                    plugin.getConfigManager().getPlayerRadius()
            )) {
                if (!(entity instanceof Item)) continue;

                Item item = (Item) entity;
                UUID itemId = item.getUniqueId();

                if (displayMap.containsKey(itemId)) continue;

                displayItemInfo(item);
            }
        }
    }

    private void displayItemInfo(Item item) {
        UUID itemId = item.getUniqueId();
        ItemStack itemStack = item.getItemStack();
        String itemName = plugin.getNameUtil().getItemName(itemStack);
        int amount = itemStack.getAmount();

        String displayName = amount > 1 ? itemName + " §7x" + amount : itemName;

        Location itemLocation = item.getLocation();
        Location displayLocation = itemLocation.clone().add(0, 0.5, 0);

        ArmorStand display = item.getWorld().spawn(displayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(false);
            armorStand.setMarker(true);
            armorStand.setSmall(true);

            armorStand.setCustomName(displayName);
            armorStand.addScoreboardTag("BestDisplay");
        });

        displayMap.put(itemId, display);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (displayMap.containsKey(itemId)) {
                    ArmorStand armorStand = displayMap.get(itemId);
                    if (armorStand.isValid()) {
                        armorStand.setCustomNameVisible(true);
                    }
                }
            }
        }.runTaskLater(plugin, 10L);
    }
}