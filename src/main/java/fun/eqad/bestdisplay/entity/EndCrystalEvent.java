package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EndCrystalEvent implements Listener {
    private final BestDisplay plugin;

    public EndCrystalEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCrystalDestroy(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().shouldEndCrystalDisplay()) return;
        
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        
        Player player = null;
        if (event.getDamager() instanceof Player) {
            player = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                player = (Player) projectile.getShooter();
            }
        }
        
        if (player == null) return;
        
        World world = player.getWorld();

        if (world.getEnvironment() != World.Environment.THE_END) return;

        if (Math.abs(player.getLocation().getX()) > 500 || Math.abs(player.getLocation().getZ()) > 500) return;
        
        int crystalCount = 0;
        for (Entity entity : world.getEntities()) {
            if (entity instanceof EnderCrystal) {
                if (Math.abs(entity.getLocation().getX()) < 500 && Math.abs(entity.getLocation().getZ()) < 500) {
                    crystalCount++;
                }
            }
        }

        int remaining = Math.max(0, crystalCount - 1);
        
        String message = "§7剩余末影水晶: §f" + remaining;
        
        player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                new TextComponent(message)
        );
    }
}