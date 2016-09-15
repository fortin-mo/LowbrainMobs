package lowbrain.mobs.main;

import jdk.nashorn.internal.runtime.ECMAException;
import lowbrain.core.main.LowbrainCore;
import lowbrain.core.rpg.LowbrainPlayer;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * Created by Moofy on 28/08/2016.
 */
public class MobSpawnListener implements Listener {

    public static Main plugin;

    public MobSpawnListener(Main instance) {
        plugin = instance;
    }

    @EventHandler
    public void onCreatureSpawnEvent(CreatureSpawnEvent e){

        LivingEntity mob = e.getEntity();

        if(plugin.getBlockedMobs().containsKey(mob.getName().toLowerCase())){
            return;
        }

        int mobLevel = getMobLevel(e.getEntity());

        ConfigurationSection mobSection = getConfig().getConfigurationSection("mobs." + mob.getName().toLowerCase());
        if(mobSection == null) mobSection = getConfig().getConfigurationSection("mobs.default");
        if(mobSection == null) return;

        int maxLevel = mobSection.getInt("level.max",100);

        float maxDamageMultiplier = (float)mobSection.getDouble("damage_multiplier.max",2.5);
        float minDamageMultiplier = (float)mobSection.getDouble("damage_multiplier.min",0.5);
        float maxHealthMultiplier = (float)mobSection.getDouble("health_multiplier.max",5);
        float minHealthMultiplier = (float)mobSection.getDouble("health_multiplier.min",0.5);
        float maxMovementSpeedMultiplier = (float)mobSection.getDouble("speed_multiplier.max",1.5);
        float minMovementSpeedMultiplier = (float)mobSection.getDouble("speed_multiplier.min",0.5);
        float maxFollowRangeMultiplier = (float)mobSection.getDouble("follow_range_multiplier.max",3);
        float minFollowRangeMultiplier = (float)mobSection.getDouble("follow_range_multiplier.min",0.5);

        try {
            setMobAttackDamage(mob,maxDamageMultiplier,minDamageMultiplier,mobLevel,maxLevel);
            setMobFollowRange(mob,maxFollowRangeMultiplier,minFollowRangeMultiplier,mobLevel,maxLevel);
            setMobMaxHealth(mob,maxHealthMultiplier,minHealthMultiplier,mobLevel,maxLevel);
            setMobMovementSpeed(mob,maxMovementSpeedMultiplier,minMovementSpeedMultiplier,mobLevel,maxLevel);
        } catch (Exception ex){
            // Some creature don't have the generic attributes.
        }


        mob.setCustomName("["+mobLevel+"] " + mob.getName());
        //mob.setCustomNameVisible(true);
    }

    private void setMobAttackDamage(LivingEntity e, float max, float min, int level,int maxLevel){
        float value = getValue(max,min,level,maxLevel,0);
        value = (float)e.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue() * value;
        e.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(value);
    }

    private void setMobMovementSpeed(LivingEntity e, float max, float min, int level,int maxLevel){
        float value = getValue(max,min,level,maxLevel,0);
        value = (float)e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue() * value;
        e.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value);
    }

    private void setMobMaxHealth(LivingEntity e, float max, float min, int level,int maxLevel){
        float value = getValue(max,min,level,maxLevel,0);
        value = (float)e.getMaxHealth() * value;
        e.setMaxHealth(value);
        e.setHealth(e.getMaxHealth());
    }

    private void setMobFollowRange(LivingEntity e, float max, float min, int level, int maxLevel){
        float value = getValue(max,min,level,maxLevel,0);
        value = (float)e.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).getBaseValue() * value;
        e.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(value);
    }

    private int getMobLevel(LivingEntity e){
        int level = 0;

        Location spawn = e.getLocation().getWorld().getSpawnLocation();
        double x = e.getLocation().getX() - spawn.getX();
        double y = e.getLocation().getY() - spawn.getY();

        double distance = Math.pow(x*x + y*y,0.5);

        ConfigurationSection mobSection = getConfig().getConfigurationSection("mobs." + e.getName().toLowerCase());
        if(mobSection == null) mobSection = getConfig().getConfigurationSection("mobs.default");
        if(mobSection == null) return level;

        int maxLevel = mobSection.getInt("level.max",100);
        int levelRange = mobSection.getInt("level.range",1);
        int distanceInterval = mobSection.getInt("level.distance_interval",50);

        level = (int)((distance / distanceInterval) + 1);
        if(levelRange > 0){
            level = randomInt(level - levelRange,level + levelRange);
        }

        if(plugin.lowbrainCoreEnabled && getConfig().getBoolean("enable_average_player_level")){
            int averageLevel = LowbrainCore.getInstance().getPlayerHandler().getAverageLevel();

            level = (int)(level + averageLevel / 2);
        }

        if(level > maxLevel){
            level = maxLevel;
        } else if (level <= 0){
            level = 1;
        }


        return level;
    }

    private FileConfiguration getConfig(){
        return plugin.getConfig();
    }

    /**
     * generate a int float [min,max]
     * @param max max
     * @param min min
     * @return
     */
    private int randomInt(int min, int max){
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }

    private float Slope(float max, float min, float y, int functionType){
        float slope = 0;
        switch (functionType){
            case 1:
                slope = (max - min)/(float)Math.pow(y,2);
                break;
            case 2:
                slope = (max - min)/(float)Math.pow(y,0.5);
                break;
            default:
                slope = (max - min)/y;
                break;
        }
        return slope;
    }

    private float getValue(float max, float min, float x, float y, int functionType){
        float result = 0;
        switch (functionType){
            case 1:
                result = Slope(max,min,y,functionType) * (float)Math.pow(x,2) + min;
                break;
            case 2:
                result = Slope(max,min,y,functionType) * (float)Math.pow(x,0.5) + min;
                break;
            default:
                result = Slope(max,min,y,functionType) * x + min;
                break;
        }
        return result;
    }

}
