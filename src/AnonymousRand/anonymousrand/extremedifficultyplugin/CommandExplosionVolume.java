package AnonymousRand.anonymousrand.extremedifficultyplugin;

import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.ListenerPlayerJoinAndQuit;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandExplosionVolume implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args[0].equals("set")) {
            double tempMultiplier;

            try {
                tempMultiplier = Double.parseDouble(args[1]);
            } catch (NumberFormatException e) {
                return false;
            }

            ListenerPlayerJoinAndQuit.explosionVolumeMultipier = tempMultiplier;
        } else if (args[0].equals("get")) {
            Bukkit.broadcastMessage("The volume for explosion sounds is currently set at " + Math.max(ListenerPlayerJoinAndQuit.explosionVolumeMultipier, 1.0) * 100.0 + "%");
        } else {
            return false;
        }

        return true;
    }
}
