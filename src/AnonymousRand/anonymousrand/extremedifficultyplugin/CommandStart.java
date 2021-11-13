/*package AnonymousRand.ExtremeDifficultyPlugin; //this class not needed as no commands are being executed

import net.minecraft.server.v1_16_R1.BiomeBase;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitScheduler;

import static org.bukkit.Bukkit.getServer;

public class CommandStart implements CommandExecutor {
    private ExtremeDifficultyPlugin plugin;

    public CommandStart(ExtremeDifficultyPlugin plugin){
        plugin = plugin;
    }

    @Override
    public boolean onCommand (CommandSender sender, Command command, String label, String[] args) { //nothing happends when the command is executed
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
            }
        }, 0L, 1L); //loop

        return true;
    }
}*/
