package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class HitEvent implements Listener {
    private final BestDisplay plugin;

    public HitEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player attacker = (Player) event.getDamager();

            double damage = event.getFinalDamage();

            boolean isCritical = isCriticalHit(attacker, event);

            String targetName;
            String displayMessage;

            if (event.getEntity() instanceof Player) {
                Player victim = (Player) event.getEntity();
                double remainingHealth = victim.getHealth() - damage;
                if (remainingHealth < 0) remainingHealth = 0;
                double maxHealth = victim.getMaxHealth();

                String healthBar = generateHealthBar(remainingHealth, maxHealth);

                if (victim.getCustomName() != null) {
                    targetName = victim.getCustomName();
                } else {
                    targetName = victim.getName();
                }

                if (remainingHealth <= 0) {
                    targetName = "§8§m" + targetName + "§r";
                }

                String damageColor = isCritical ? "§4" : "§c";
                displayMessage = targetName + "§7 " + healthBar +
                        " §7(§f" + String.format("%.1f", remainingHealth) + "§7/§f" + String.format("%.1f", maxHealth) + ")" +
                        " §7- " + damageColor + String.format("%.1f", damage) + " ❤";

                showDamageAboveHead(victim, damage, isCritical);
            } else {
                try {
                    org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) event.getEntity();

                    double remainingHealth = livingEntity.getHealth() - damage;
                    if (remainingHealth < 0) remainingHealth = 0;
                    double maxHealth = livingEntity.getMaxHealth();

                    String healthBar = generateHealthBar(remainingHealth, maxHealth);

                    if (event.getEntity().getCustomName() != null) {
                        targetName = event.getEntity().getCustomName();
                    } else {
                        targetName = NameUtil.getEntityName(event.getEntity().getType());
                    }

                    if (remainingHealth <= 0) {
                        targetName = "§8§m" + targetName + "§r";
                    }

                    String damageColor = isCritical ? "§4" : "§c";
                    displayMessage = targetName + "§7 " + healthBar +
                            " §7(§f" + String.format("%.1f", remainingHealth) + "§7/§f" + String.format("%.1f", maxHealth) + ")" +
                            " §7- " + damageColor + String.format("%.1f", damage) + " ❤";

                    showDamageAboveHead(livingEntity, damage, isCritical);
                } catch (ClassCastException e) {
                    return;
                }
            }

            if (plugin.getConfigManager().shouldHealthActionBar()) {
                attacker.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent(displayMessage)
                );
            }
        }
    }

    @EventHandler
    public void onArrowHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();
            ProjectileSource shooter = arrow.getShooter();

            if (shooter instanceof Player) {
                Player attacker = (Player) shooter;

                double damage = event.getFinalDamage();

                String targetName;
                String displayMessage;

                if (event.getEntity() instanceof Player) {
                    Player victim = (Player) event.getEntity();
                    double remainingHealth = victim.getHealth() - damage;
                    if (remainingHealth < 0) remainingHealth = 0;
                    double maxHealth = victim.getMaxHealth();

                    String healthBar = generateHealthBar(remainingHealth, maxHealth);

                    if (victim.getCustomName() != null) {
                        targetName = victim.getCustomName();
                    } else {
                        targetName = victim.getName();
                    }

                    if (remainingHealth <= 0) {
                        targetName = "§8§m" + targetName + "§r";
                    }

                    String damageColor = "§c";
                    displayMessage = targetName + "§7 " + healthBar +
                            " §7(§f" + String.format("%.1f", remainingHealth) + "§7/§f" + String.format("%.1f", maxHealth) + ")" +
                            " §7- " + damageColor + String.format("%.1f", damage) + " ❤";
                } else {
                    try {
                        org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) event.getEntity();

                        double remainingHealth = livingEntity.getHealth() - damage;
                        if (remainingHealth < 0) remainingHealth = 0;
                        double maxHealth = livingEntity.getMaxHealth();

                        String healthBar = generateHealthBar(remainingHealth, maxHealth);

                        if (event.getEntity().getCustomName() != null) {
                            targetName = event.getEntity().getCustomName();
                        } else {
                            targetName = NameUtil.getEntityName(event.getEntity().getType());
                        }

                        if (remainingHealth <= 0) {
                            targetName = "§8§m" + targetName + "§r";
                        }

                        String damageColor = "§c";
                        displayMessage = targetName + "§7 " + healthBar +
                                " §7(§f" + String.format("%.1f", remainingHealth) + "§7/§f" + String.format("%.1f", maxHealth) + ")" +
                                " §7- " + damageColor + String.format("%.1f", damage) + " ❤";
                    } catch (ClassCastException e) {
                        return;
                    }
                }

                if (plugin.getConfigManager().shouldHealthActionBar()) {
                    attacker.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent(displayMessage)
                    );
                }

                if (plugin.getConfigManager().shouldArrowSound()) {
                    attacker.playSound(attacker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent damageByEntityEvent = (EntityDamageByEntityEvent) event;
            if (damageByEntityEvent.getDamager() instanceof Player) {
                return;
            }
        }

        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity entity = (org.bukkit.entity.LivingEntity) event.getEntity();
            double damage = event.getFinalDamage();

            showDamageAboveHead(entity, damage, false);
        }
    }

    @EventHandler
    public void onEntityRegainHealth(org.bukkit.event.entity.EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof org.bukkit.entity.LivingEntity) {
            org.bukkit.entity.LivingEntity entity = (org.bukkit.entity.LivingEntity) event.getEntity();
            double amount = event.getAmount();

            showHealingAboveHead(entity, amount);
        }
    }

    private void showHealingAboveHead(org.bukkit.entity.Entity entity, double amount) {
        if (!plugin.getConfigManager().shouldHealingAbove()) return;

        Location entityLocation = entity.getLocation();

        Random random = new Random();
        double offsetX = (random.nextDouble() - 0.5) * 0.6;
        double offsetZ = (random.nextDouble() - 0.5) * 0.6;

        Location healLocation = entityLocation.clone().add(offsetX, 2, offsetZ);

        ArmorStand healDisplay = entity.getWorld().spawn(healLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);

            armorStand.setCustomName("§a+" + String.format("%.1f", amount) + "❤");
            armorStand.addScoreboardTag("BestDisplay");
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                healDisplay.teleport(healDisplay.getLocation().add(0, 0.01, 0));

                if (healDisplay.getTicksLived() > 20) {
                    healDisplay.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private boolean isCriticalHit(Player attacker, EntityDamageByEntityEvent event) {
        if (attacker.isOnGround()) {
            return false;
        }

        if (attacker.getVelocity().getY() >= 0) {
            return false;
        }

        return event.getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.ENTITY_ATTACK;
    }

    private void showDamageAboveHead(org.bukkit.entity.Entity entity, double damage, boolean isCritical) {
        if (!plugin.getConfigManager().shouldDamageAbove()) return;

        Location entityLocation = entity.getLocation();

        Random random = new Random();
        double offsetX = (random.nextDouble() - 0.5) * 0.6;
        double offsetZ = (random.nextDouble() - 0.5) * 0.6;

        Location damageLocation = entityLocation.clone().add(offsetX, 2, offsetZ);

        ArmorStand damageDisplay = entity.getWorld().spawn(damageLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);

            String color = isCritical ? "§4" : "§c";
            armorStand.setCustomName(color + "-" + String.format("%.1f", damage) + "❤");
            armorStand.addScoreboardTag("BestDisplay");
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                damageDisplay.teleport(damageDisplay.getLocation().add(0, 0.01, 0));

                if (damageDisplay.getTicksLived() > 20) {
                    damageDisplay.remove();
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private String generateHealthBar(double currentHealth, double maxHealth) {
        if (maxHealth <= 0) return "§c❤❤❤❤❤❤❤❤❤❤";

        double healthPercentage = (currentHealth / maxHealth) * 100;
        int filledHearts = (int) Math.ceil((healthPercentage / 100) * 10);

        StringBuilder healthBar = new StringBuilder();

        for (int i = 0; i < filledHearts; i++) {
            healthBar.append("§c❤");
        }

        for (int i = filledHearts; i < 10; i++) {
            healthBar.append("§7❤");
        }

        return healthBar.toString();
    }
}