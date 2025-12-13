package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class VillagerEvent implements Listener {
    private final BestDisplay plugin;
    private final Map<UUID, List<ArmorStand>> displayMap = new HashMap<>();
    
    public VillagerEvent(BestDisplay plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onVillagerSpawn(EntitySpawnEvent event) {
        if (!plugin.getConfigManager().shouldVillagerDisplay()) return;
        
        if (!(event.getEntity() instanceof Villager)) return;
        
        Villager villager = (Villager) event.getEntity();
        displayVillagerInfo(villager);
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.getConfigManager().shouldVillagerDisplay()) return;

        Player player = event.getPlayer();
        Location playerLoc = player.getLocation();
        
        for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, 10, 10, 10)) {
            if (!(entity instanceof Villager)) continue;
            
            Villager villager = (Villager) entity;
            UUID villagerId = villager.getUniqueId();
            
            if (displayMap.containsKey(villagerId)) continue;
            
            displayVillagerInfo(villager);
        }
    }
    
    private void displayVillagerInfo(Villager villager) {
        UUID villagerId = villager.getUniqueId();
        
        if (villager.getProfession() == Villager.Profession.NONE || villager.getProfession() == Villager.Profession.NITWIT) {
            return;
        }
        
        if (displayMap.containsKey(villagerId)) {
            for (ArmorStand armorStand : displayMap.get(villagerId)) {
                armorStand.remove();
            }
        }
        
        String professionName = getVillagerProfessionName(villager.getProfession());
        String villagerLevel = getVillagerLevel(villager.getVillagerLevel());

        String topText = professionName;
        String bottomText = "§7(§f" + villagerLevel + "§7)";
        
        Location villagerLocation = villager.getLocation();
        Location topDisplayLocation = villagerLocation.clone().add(0, 2.4, 0);
        Location bottomDisplayLocation = villagerLocation.clone().add(0, 2.1, 0);
        
        ArmorStand topDisplay = villager.getWorld().spawn(topDisplayLocation, ArmorStand.class, armorStand -> {
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setInvulnerable(true);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);
            armorStand.setSmall(true);
            armorStand.setCustomName(topText);
            armorStand.addScoreboardTag("BestDisplay");
        });
        
        ArmorStand bottomDisplay = villager.getWorld().spawn(bottomDisplayLocation, ArmorStand.class, armorStand -> {
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
        displayMap.put(villagerId, displays);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (displayMap.containsKey(villagerId)) {
                    for (ArmorStand armorStand : displayMap.get(villagerId)) {
                        armorStand.remove();
                    }
                    displayMap.remove(villagerId);
                }
            }
        }.runTaskLater(plugin, 20L * 3);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (villager.isDead() || !villager.isValid()) {
                    if (displayMap.containsKey(villagerId)) {
                        for (ArmorStand armorStand : displayMap.get(villagerId)) {
                            armorStand.remove();
                        }
                        displayMap.remove(villagerId);
                    }
                    this.cancel();
                    return;
                }

                if (villager.getProfession() == Villager.Profession.NONE || villager.getProfession() == Villager.Profession.NITWIT) {
                    if (displayMap.containsKey(villagerId)) {
                        for (ArmorStand armorStand : displayMap.get(villagerId)) {
                            armorStand.remove();
                        }
                        displayMap.remove(villagerId);
                    }
                    this.cancel();
                    return;
                }

                if (displayMap.containsKey(villagerId)) {
                    List<ArmorStand> armorStands = displayMap.get(villagerId);
                    if (armorStands.size() >= 2 && armorStands.get(0).isValid() && armorStands.get(1).isValid()) {
                        Location currentLocation = villager.getLocation();
                        armorStands.get(0).teleport(currentLocation.clone().add(0, 2.4, 0));
                        armorStands.get(1).teleport(currentLocation.clone().add(0, 2.1, 0));

                        String currentProfessionName = getVillagerProfessionName(villager.getProfession());
                        String currentVillagerLevel = getVillagerLevel(villager.getVillagerLevel());
                        String currentTopText = currentProfessionName;
                        String currentBottomText = "§7(§f" + currentVillagerLevel + "§7)";
                        
                        armorStands.get(0).setCustomName(currentTopText);
                        armorStands.get(1).setCustomName(currentBottomText);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }
    
    private String getVillagerProfessionName(Villager.Profession profession) {
        switch (profession) {
            case NONE: return "无业";
            case ARMORER: return "盔甲匠";
            case BUTCHER: return "屠夫";
            case CARTOGRAPHER: return "制图师";
            case CLERIC: return "牧师";
            case FARMER: return "农民";
            case FISHERMAN: return "渔夫";
            case FLETCHER: return "弓箭手";
            case LEATHERWORKER: return "皮革匠";
            case LIBRARIAN: return "图书管理员";
            case MASON: return "石匠";
            case NITWIT: return "傻子";
            case SHEPHERD: return "牧羊人";
            case TOOLSMITH: return "工具匠";
            case WEAPONSMITH: return "武器匠";
            default:
                return "未知职业";
        }
    }
    
    private String getVillagerLevel(int level) {
        switch (level) {
            case 1: return "新手";
            case 2: return "学徒";
            case 3: return "熟练";
            case 4: return "专家";
            case 5: return "大师";
            default:
                return "等级 " + level;
        }
    }
    
    public void cleanup() {
        for (List<ArmorStand> armorStands : displayMap.values()) {
            if (armorStands != null) {
                for (ArmorStand armorStand : armorStands) {
                    if (armorStand != null) {
                        armorStand.remove();
                    }
                }
            }
        }
        displayMap.clear();
    }
}
