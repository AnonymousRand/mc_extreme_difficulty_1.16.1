package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import io.netty.channel.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Field;

public class ListenerPlayerJoinAndQuit implements Listener {

    public static double explosionVolumeMultiplier, defaultVolume;
    public static boolean firstExplosion;
    private static Field c;

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();

        if (!ListenerPlayerMovementAndFallDamage.fallHeight.containsKey(bukkitPlayer)) { // initialize players in fall damage calculation hashmap
            ListenerPlayerMovementAndFallDamage.fallHeight.put(bukkitPlayer, 0.0);
        }

        if (bukkitPlayer.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            bukkitPlayer.getWorld().setDifficulty(Difficulty.EASY);
        }

        if (bukkitPlayer.getServer().getOnlinePlayers().size() == 1) { /* remove projectiles etc. to reduce lag if this is the first player to join the server */
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=arrow]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=small_fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=falling_block]");
        }
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event){
        Player bukkitPlayer = event.getPlayer();

        ListenerPlayerMovementAndFallDamage.fallHeight.put(bukkitPlayer, 0.0); // delete player from fall damage calculation hashmap
    }
}
