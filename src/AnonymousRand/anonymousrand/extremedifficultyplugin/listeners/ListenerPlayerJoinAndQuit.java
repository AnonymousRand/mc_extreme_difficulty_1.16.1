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

        if (!ListenerPlayerMovementAndFallDamage.fallHeight.containsKey(bukkitPlayer)) { //initialize players in fall damage calculation hashmap
            ListenerPlayerMovementAndFallDamage.fallHeight.put(bukkitPlayer, 0.0);
        }

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

        muteExplosionPackets(event.getPlayer());
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event){
        Player bukkitPlayer = event.getPlayer();

        ListenerPlayerMovementAndFallDamage.fallHeight.put(bukkitPlayer, 0.0); //delete player from fall damage calculation hashmap

        Channel channel = ((CraftPlayer)bukkitPlayer).getHandle().playerConnection.networkManager.channel; //code from YouTube
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(bukkitPlayer.getName());
            return null;
        });
    }

    private void muteExplosionPackets(Player player) { //code partially from https://www.youtube.com/watch?v=rst3-wE14OQ
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void write(ChannelHandlerContext channelHandlerContext, Object packet, ChannelPromise channelPromise) throws Exception {
                if(packet instanceof PacketPlayOutExplosion){
                    if (firstExplosion) {
                        firstExplosion = false;

                        try {
                            c = PacketPlayOutExplosion.class.getDeclaredField("c");
                            c.setAccessible(true);
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }

                        defaultVolume = c.getDouble(packet);
                    }

                    c.setDouble(packet, defaultVolume * (0.16 * Math.max(explosionVolumeMultiplier, 1.0) + 0.84)); //volume is effectively 0 at a value of 0.84, so scale accordingly
                }

                super.write(channelHandlerContext, packet, channelPromise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer)player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName(), channelDuplexHandler);
    }
}
