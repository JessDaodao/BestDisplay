package fun.eqad.bestdisplay.entity;

import fun.eqad.bestdisplay.BestDisplay;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class VillagerEvent {
    private final BestDisplay plugin;
    private final Set<UUID> lastVisibleVillagers = new HashSet<>();
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
        updateTask.runTaskTimer(plugin, 0L, 5L);
    }
    
    private void updateAllDisplays() {
        if (!plugin.getConfigManager().shouldVillagerDisplay()) return;

        Set<UUID> currentVisibleVillagers = new HashSet<>();
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        double radius = plugin.getConfigManager().getPlayerRadius();

        for (Player player : players) {
            Location playerLoc = player.getLocation();
            
            for (Entity entity : playerLoc.getWorld().getNearbyEntities(playerLoc, radius, radius, radius)) {
                if (!(entity instanceof Villager)) continue;
                
                Villager villager = (Villager) entity;
                if (currentVisibleVillagers.contains(villager.getUniqueId())) continue;

                if (!villager.isValid() || villager.isDead()) continue;
                
                if (villager.getProfession() == Villager.Profession.NONE || villager.getProfession() == Villager.Profession.NITWIT) {
                    continue;
                }

                currentVisibleVillagers.add(villager.getUniqueId());
                updateVillagerDisplay(villager);
            }
        }

        for (UUID uuid : lastVisibleVillagers) {
            if (!currentVisibleVillagers.contains(uuid)) {
                Entity entity = Bukkit.getEntity(uuid);
                if (entity instanceof Villager && entity.isValid()) {
                    entity.setCustomNameVisible(false);
                }
            }
        }

        lastVisibleVillagers.clear();
        lastVisibleVillagers.addAll(currentVisibleVillagers);
    }
    
    private void updateVillagerDisplay(Villager villager) {
        String professionName = getVillagerProfessionName(villager.getProfession());
        String villagerLevel = getVillagerLevel(villager.getVillagerLevel());

        String displayName = professionName + " §8| §7" + villagerLevel;
        
        villager.setCustomName(displayName);
        villager.setCustomNameVisible(true);
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
        if (updateTask != null) {
            updateTask.cancel();
        }
    }
}