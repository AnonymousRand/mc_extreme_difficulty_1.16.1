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
import java.util.List;

public class ListenerPlayerJoin implements Listener {

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player bukkitPlayer = event.getPlayer();

        bukkitPlayer.getWorld().setDifficulty(Difficulty.EASY); //todo temp
        if (bukkitPlayer.getServer().getOnlinePlayers().size() == 1) { /**remove projectiles etc to reduce lag if this is the first player to join the server*/
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=arrow]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=small_fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=fireball]");
            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "kill @e[type=falling_block]");
        } else { /**when players join, they get blindness 255, slowness 255 and weakness 255 for 10 seconds to prevent exploits*/
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 255));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 200, 255));
            bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 200, 255));
        }

        if (RunnableMobRain.permanentEntitiesToSpawn.isEmpty()) { //init here to avoid lag spikes as filling in the arraylists takes quite a while
            initMobRainArrayLists(((CraftWorld)bukkitPlayer.getWorld()).getHandle());
        }
    }

    public static void initMobRainArrayLists(World nmsWorld) {
        ArrayList<EntityLiving> entitiesToSpawn1, entitiesToSpawn2;

        entitiesToSpawn1 = new ArrayList<>(List.of(
                new CustomEntityBat(nmsWorld),
                new CustomEntityBat(nmsWorld),
                new CustomEntityBat(nmsWorld),
                new CustomEntityBee(nmsWorld),
                new CustomEntityBee(nmsWorld),
                new CustomEntityBlaze(nmsWorld),
                new CustomEntityBlaze(nmsWorld),
                new CustomEntityCow(nmsWorld),
                new CustomEntityCow(nmsWorld),
                new CustomEntityCreeper(nmsWorld, 20),
                new CustomEntityCreeper(nmsWorld, 20),
                new CustomEntityDrowned(nmsWorld),
                new CustomEntityEnderman(nmsWorld),
                new CustomEntityEnderman(nmsWorld),
                new CustomEntityEnderman(nmsWorld),
                new CustomEntityEnderman(nmsWorld),
                new CustomEntityEnderman(nmsWorld),
                new CustomEntityEndermite(nmsWorld),
                new CustomEntityEndermite(nmsWorld),
                new CustomEntityEndermite(nmsWorld),
                new CustomEntityEndermite(nmsWorld),
                new CustomEntityEndermite(nmsWorld),
                new CustomEntityEndermite(nmsWorld),
                new CustomEntityEvoker(nmsWorld),
                new CustomEntityGhast(nmsWorld),
                new CustomEntityGuardian(nmsWorld),
                new CustomEntityHoglin(nmsWorld),
                new CustomEntityHoglin(nmsWorld),
                new CustomEntityIllusioner(nmsWorld),
                new CustomEntityRabbit(nmsWorld),
                new CustomEntityRabbit(nmsWorld),
                new CustomEntityRabbit(nmsWorld),
                new CustomEntityRabbit(nmsWorld),
                new CustomEntityRabbit(nmsWorld),
                new CustomEntityLlama(nmsWorld),
                new CustomEntityPhantom(nmsWorld),
                new CustomEntityPhantom(nmsWorld),
                new CustomEntityPhantom(nmsWorld),
                new CustomEntityPhantom(nmsWorld),
                new CustomEntityPhantom(nmsWorld),
                new CustomEntityPhantom(nmsWorld),
                new CustomEntityPiglin(nmsWorld),
                new CustomEntityPiglin(nmsWorld),
                new CustomEntityPiglin(nmsWorld),
                new CustomEntityPillager(nmsWorld),
                new CustomEntityPillager(nmsWorld),
                new CustomEntitySheep(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySilverfish(nmsWorld),
                new CustomEntitySkeleton(nmsWorld),
                new CustomEntitySkeleton(nmsWorld),
                new CustomEntitySkeleton(nmsWorld),
                new CustomEntitySlime(nmsWorld, 8),
                new CustomEntitySlime(nmsWorld, 4),
                new CustomEntitySlime(nmsWorld, 4),
                new CustomEntitySlimeMagmaCube(nmsWorld, 16),
                new CustomEntitySpider(nmsWorld),
                new CustomEntitySpider(nmsWorld),
                new CustomEntitySpiderCave(nmsWorld),
                new CustomEntitySkeletonStray(nmsWorld),
                new CustomEntitySkeletonStray(nmsWorld),
                new CustomEntitySkeletonWither(nmsWorld),
                new CustomEntitySkeletonWither(nmsWorld),
                new CustomEntityVex(nmsWorld),
                new CustomEntityVex(nmsWorld),
                new CustomEntityVex(nmsWorld),
                new CustomEntityVex(nmsWorld),
                new EntityVillager(EntityTypes.VILLAGER, nmsWorld),
                new EntityVillager(EntityTypes.VILLAGER, nmsWorld),
                new CustomEntityVindicator(nmsWorld),
                new CustomEntityVindicator(nmsWorld),
                new CustomEntityVindicator(nmsWorld),
                new CustomEntityVindicator(nmsWorld),
                new CustomEntityWitch(nmsWorld),
                new CustomEntityZombie(nmsWorld),
                new CustomEntityZombie(nmsWorld),
                new CustomEntityZombie(nmsWorld),
                new CustomEntityZombie(nmsWorld),
                new CustomEntityZombie(nmsWorld),
                new CustomEntityZombieHusk(nmsWorld),
                new CustomEntityZombieHusk(nmsWorld),
                new CustomEntityZombieHusk(nmsWorld),
                new CustomEntityZombieHusk(nmsWorld),
                new CustomEntityZombiePig(nmsWorld),
                new CustomEntityZombiePig(nmsWorld),
                new CustomEntityZombiePig(nmsWorld),
                new CustomEntityZombiePig(nmsWorld),
                new CustomEntityZombiePig(nmsWorld),
                new CustomEntityZombiePig(nmsWorld),
                new CustomEntityZombiePig(nmsWorld),
                new CustomEntityZombieVillager(nmsWorld),
                new CustomEntityZombieVillager(nmsWorld),
                new CustomEntityZombieVillager(nmsWorld)
        ));

        entitiesToSpawn2 = new ArrayList<>(List.of(
                new CustomEntityGuardianElder(nmsWorld),
                new CustomEntityIllusioner(nmsWorld),
                new CustomEntityIllusioner(nmsWorld),
                new CustomEntityIronGolem(nmsWorld),
                new CustomEntityRavager(nmsWorld),
                new CustomEntitySheepAggressive(nmsWorld),
                new CustomEntityShulker(nmsWorld),
                new CustomEntityShulker(nmsWorld),
                new CustomEntitySpider(nmsWorld),
                new CustomEntitySpider(nmsWorld),
                new CustomEntitySpider(nmsWorld),
                new CustomEntitySpider(nmsWorld),
                new CustomEntityZoglin(nmsWorld),
                new CustomEntityZombieThor(nmsWorld)
        ));

        RunnableMobRain.permanentEntitiesToSpawn.put(1, entitiesToSpawn1);
        RunnableMobRain.permanentEntitiesToSpawn.put(2, entitiesToSpawn2);
    }
}
