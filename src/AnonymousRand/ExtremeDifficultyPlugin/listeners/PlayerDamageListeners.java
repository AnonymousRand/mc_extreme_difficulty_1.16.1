package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityPiglin;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntitySkeleton;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityZombie;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Random;

import static org.bukkit.entity.EntityType.PLAYER;

public class PlayerDamageListeners implements Listener {

    Random rand = new Random();

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {
        if (event.getEntityType() == PLAYER && event.getCause().equals(EntityDamageEvent.DamageCause.DROWNING)) { //drowning spawns a pufferfish per damage tick, with 25% chance to also spawn a guardian and a 5% chance to spawn an elder guardian
            Player player = (Player)event.getEntity();
            Location loc = player.getLocation();

            if (rand.nextDouble() < 0.25) {
                CustomEntitySkeleton skeleton = new CustomEntitySkeleton(((CraftWorld)player.getWorld()).getHandle()); //todo change to guardian
                skeleton.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                ((CraftWorld)player.getWorld()).getHandle().addEntity(skeleton, CreatureSpawnEvent.SpawnReason.NATURAL);
            } else if (rand.nextDouble() < 0.05) {
                CustomEntityZombie zombie = new CustomEntityZombie(((CraftWorld)player.getWorld()).getHandle()); //todo change to elder guardian
                zombie.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                ((CraftWorld)player.getWorld()).getHandle().addEntity(zombie, CreatureSpawnEvent.SpawnReason.NATURAL);
            } else {
                CustomEntityPiglin piglin = new CustomEntityPiglin(((CraftWorld)player.getWorld()).getHandle()); //todo change to pufferfish
                piglin.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                ((CraftWorld)player.getWorld()).getHandle().addEntity(piglin, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }
    }

    @EventHandler
    public void entityAirChange(EntityAirChangeEvent event) {
        if (event.getEntityType() == PLAYER && event.getAmount() > 1) { //air goes down 3 times as fast
            event.setAmount(event.getAmount() - 2);
        }
    }
}
