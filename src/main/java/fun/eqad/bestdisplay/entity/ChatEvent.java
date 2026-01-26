package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatEvent implements Listener {
    private final BestDisplay plugin;

    public ChatEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfigManager().shouldChatDisplay()) return;

        Player player = event.getPlayer();
        String message = event.getMessage();

        new BukkitRunnable() {
            @Override
            public void run() {
                showChatAboveHead(player, message);
            }
        }.runTask(plugin);
    }

    private void showChatAboveHead(Player player, String message) {
        Location playerLocation = player.getLocation();
        Location displayLocation = playerLocation.clone().add(0, 2.2, 0);

        ArmorStand chatDisplay = player.getWorld().spawn(displayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(message);
            armorStand.addScoreboardTag("BestDisplay");
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline() || chatDisplay.isDead() || !chatDisplay.isValid()) {
                    chatDisplay.remove();
                    this.cancel();
                    return;
                }

                Location newLocation = player.getLocation().clone().add(0, 2.2, 0);
                chatDisplay.teleport(newLocation);

                if (chatDisplay.getTicksLived() > 60) {
                    chatDisplay.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
}