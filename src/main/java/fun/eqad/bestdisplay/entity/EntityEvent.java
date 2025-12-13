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

public class EntityEvent implements Listener {
    private final BestDisplay plugin;

    public EntityEvent(BestDisplay plugin) {
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
                        targetName = getEntityDisplayName(event.getEntity().getType());
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
                            targetName = getEntityDisplayName(event.getEntity().getType());
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

    private String getEntityDisplayName(org.bukkit.entity.EntityType entityType) {
        switch (entityType) {
            case ZOMBIE: return "僵尸";
            case SKELETON: return "骷髅";
            case CREEPER: return "苦力怕";
            case SPIDER: return "蜘蛛";
            case ENDERMAN: return "末影人";
            case BLAZE: return "烈焰人";
            case GHAST: return "恶魂";
            case WITHER_SKELETON: return "凋灵骷髅";
            case ENDER_DRAGON: return "末影龙";
            case WITHER: return "凋灵";
            case WITCH: return "女巫";
            case SLIME: return "史莱姆";
            case MAGMA_CUBE: return "岩浆怪";
            case CAVE_SPIDER: return "洞穴蜘蛛";
            case SILVERFISH: return "蠹虫";
            case ENDERMITE: return "末影螨";
            case GUARDIAN: return "守卫者";
            case ELDER_GUARDIAN: return "远古守卫者";
            case SHULKER: return "潜影贝";
            case HUSK: return "尸壳";
            case STRAY: return "流浪者";
            case PHANTOM: return "幻翼";
            case DROWNED: return "溺尸";
            case PILLAGER: return "掠夺者";
            case RAVAGER: return "劫掠兽";
            case VEX: return "恼鬼";
            case EVOKER: return "唤魔者";
            case VINDICATOR: return "卫道士";
            case HOGLIN: return "猪灵蛮兵";
            case PIGLIN: return "猪灵";
            case ZOGLIN: return "僵尸猪灵";
            case ZOMBIFIED_PIGLIN: return "僵尸猪灵";
            case WOLF: return "狼";
            case DOLPHIN: return "海豚";
            case IRON_GOLEM: return "铁傀儡";
            case COW: return "牛";
            case PIG: return "猪";
            case SHEEP: return "羊";
            case CHICKEN: return "鸡";
            case CAT: return "猫";
            case HORSE: return "马";
            case DONKEY: return "驴";
            case MULE: return "骡";
            case BEE: return "蜜蜂";
            case VILLAGER: return "村民";
            case SNOWMAN: return "雪傀儡";
            case SQUID: return "鱿鱼";
            case BAT: return "蝙蝠";
            case OCELOT: return "豹猫";
            case RABBIT: return "兔子";
            case LLAMA: return "羊驼";
            case PARROT: return "鹦鹉";
            case TURTLE: return "海龟";
            case COD: return "鳕鱼";
            case SALMON: return "鲑鱼";
            case PUFFERFISH: return "河豚";
            case TROPICAL_FISH: return "热带鱼";
            case FOX: return "狐狸";
            case PANDA: return "熊猫";
            case AXOLOTL: return "美西螈";
            case STRIDER: return "炽足兽";
            default: return entityType.toString().toLowerCase();
        }
    }
}