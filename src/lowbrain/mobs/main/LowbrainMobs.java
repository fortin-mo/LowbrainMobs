package lowbrain.mobs.main;

import lowbrain.core.main.LowbrainCore;
import lowbrain.library.command.Command;
import lowbrain.library.main.LowbrainLibrary;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Moofy on 28/08/2016.
 */
public class LowbrainMobs extends JavaPlugin {

    public static boolean lowbrainCoreEnabled;

    private HashMap<String,String> blockedMobs;

    /**
     * called when the plugin is initially enabled
     */
    @Override
    public void onEnable() {
        blockedMobs = new HashMap<>();
        saveDefaultConfig();

        List<String> blockedList = getConfig().getStringList("blocked_mobs");
        for (String k : blockedList)
            blockedMobs.put(k,k);

        lowbrainCoreEnabled = Bukkit.getPluginManager().isPluginEnabled("LowbrainCore");

        if(lowbrainCoreEnabled)
            LowbrainCore.getInstance().useLowbrainMoblevel = true;

        Command sub;
        Command onReload;
        LowbrainLibrary.getInstance().getBaseCmdHandler().register("mobs",sub = new Command("mobs") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                who.sendMessage("/lb mobs reload");
                return CommandStatus.VALID;
            }
        });

        sub.register("reload",onReload = new Command("reload") {
            @Override
            public CommandStatus execute(CommandSender who, String[] args, String cmd) {
                reloadConfig();
                return CommandStatus.VALID;
            }
        });
        onReload.addPermission("lb.mobs.reload");


        getServer().getPluginManager().registerEvents(new MobSpawnListener(this), this);
        this.getLogger().info(getDescription().getVersion() + " enabled!");
    }
    @Override
    public void onDisable() {
        Bukkit.getServer().getScheduler().cancelTasks(this);
    }

    public Map<String,String> getBlockedMobs(){
        return this.blockedMobs;
    }

    /**
     * log message
     * @param msg
     */
    public void debugInfo(Object msg){
        if(this.getConfig().getBoolean("debug", false))
            this.getLogger().info("" + msg);

    }
}

