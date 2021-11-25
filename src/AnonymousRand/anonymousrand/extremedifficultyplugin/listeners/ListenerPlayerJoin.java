package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import net.minecraft.server.v1_16_R1.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ListenerPlayerJoin implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        event.getPlayer().getWorld().setDifficulty(Difficulty.EASY); //todo temp
        if (event.getPlayer().getServer().getOnlinePlayers().size() == 1) { /**remove projectiles etc to reduce lag if this is the first player to join the server*/
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=arrow]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=small_fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=falling_block]");
        } else { /**when players join, they get blindness 255, slowness 255 and weakness 255 for 10 seconds to prevent exploits*/
            Player bukkitPlayer = event.getPlayer();
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 255));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 255));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 255));
        }
    }
}
