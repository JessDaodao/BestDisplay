package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class VillagerEvent {
    private final BestDisplay plugin;
    private final Map<UUID, List<ArmorStand>> displayMap = new HashMap<>();
    private BukkitRunnable updateTask;
    
    public VillagerEvent(BestDisplay plugin) {
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
        if (!plugin.getConfigManager().shouldVillagerDisplay()) return;

        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        List<Player> playerList = new ArrayList<>(players);

        Map<UUID, List<ArmorStand>> displayMapCopy = new HashMap<>(displayMap);
        for (Map.Entry<UUID, List<ArmorStand>> entry : displayMapCopy.entrySet()) {
            UUID villagerId = entry.getKey();
            boolean hasNearbyPlayer = false;

            Villager villager = null;
            Entity entity = Bukkit.getEntity(villagerId);
            if (entity instanceof Villager) {
                villager = (Villager) entity;
            }

            if (villager == null || villager.isDead() || !villager.isValid()) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(villagerId);
                continue;
            }

            for (Player player : playerList) {
                if (player.getWorld().equals(villager.getWorld()) && 
                    player.getLocation().distance(villager.getLocation()) <= plugin.getConfigManager().getPlayerRadius()) {
                    hasNearbyPlayer = true;
                    break;
                }
            }

            if (!hasNearbyPlayer) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(villagerId);
                continue;
            }

            if (villager.getProfession() == Villager.Profession.NONE || villager.getProfession() == Villager.Profession.NITWIT) {
                for (ArmorStand display : entry.getValue()) {
                    display.remove();
                }
                displayMap.remove(villagerId);
                continue;
            }

            String professionName = getVillagerProfessionName(villager.getProfession());
            String villagerLevel = getVillagerLevel(villager.getVillagerLevel());

            String topText = professionName;
            String bottomText = "§7" + villagerLevel;
            
            List<ArmorStand> displays = entry.getValue();
            if (displays.size() >= 2 && displays.get(0).isValid() && displays.get(1).isValid()) {
                Location currentLocation = villager.getLocation();
                displays.get(0).teleport(currentLocation.clone().add(0, 2.4, 0));
                displays.get(1).teleport(currentLocation.clone().add(0, 2.1, 0));
                
                displays.get(0).setCustomName(topText);
                displays.get(1).setCustomName(bottomText);
            }
        }

        for (Player player : playerList) {
            Location playerLoc = player.getLocation();
            
            for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc,
                    plugin.getConfigManager().getPlayerRadius(),
                    plugin.getConfigManager().getPlayerRadius(),
                    plugin.getConfigManager().getPlayerRadius()
            )) {
                if (!(entity instanceof Villager)) continue;
                
                Villager villager = (Villager) entity;
                UUID villagerId = villager.getUniqueId();

                if (displayMap.containsKey(villagerId)) continue;
                
                displayVillagerInfo(villager);
            }
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
        String bottomText = "§7" + villagerLevel;
        
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
