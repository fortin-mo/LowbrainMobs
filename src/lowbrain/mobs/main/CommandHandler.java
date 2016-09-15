package lowbrain.mobs.main;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

/**
 * Created by Moofy on 04/09/2016.
 */
public class CommandHandler implements CommandExecutor{
    private final Main plugin;

    public CommandHandler(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Called when the plugin receice a command
     */
    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("lbmobs")) {
            return false;
        }

        if(args.length <= 0){
            return false;
        }

        switch (args[0].toLowerCase()){
            case "reload":
                if(sender.isOp() || sender instanceof ConsoleCommandSender || sender.hasPermission("lbmobs.reload")){
                    this.plugin.reloadConfig();
                }
                return true;
            default:
                return false;
        }
    }

}
