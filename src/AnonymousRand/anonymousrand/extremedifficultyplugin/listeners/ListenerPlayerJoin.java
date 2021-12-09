package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobRain;
import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.EntityVillager;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListenerPlayerJoin implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();

        if (bukkitPlayer.getWorld().getDifficulty() == Difficulty.PEACEFUL) {
            bukkitPlayer.getWorld().setDifficulty(Difficulty.EASY);
        }

        if (bukkitPlayer.getServer().getOnlinePlayers().size() == 1) { /**remove projectiles etc to reduce lag if this is the first player to join the server*/
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=arrow]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=small_fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=falling_block]");
        } else { /**when players join, they get blindness 255, slowness 255 and weakness 255 for 15 seconds to prevent exploits*/
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 300, 255));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 255));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 300, 255));
        }
    }
}
