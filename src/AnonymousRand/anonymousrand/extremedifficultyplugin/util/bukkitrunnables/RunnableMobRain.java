package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RunnableMobRain extends BukkitRunnable {

    private final Entity entity;
    private final EntityLiving target;
    private final World nmsWorld;
    private final double radius;
    private final int wave;
    private int cycles, index;
    private final int maxCycles;
    private HashMap<Integer, ArrayList<EntityLiving>> entitesToSpawn = new HashMap<>();
    private final BlockPosition blockPosition;
    private Location loc;
    private static final Random random = new Random();

    public RunnableMobRain(Entity entity, @Nullable EntityLiving target, double radius, int wave) {
        this.entity = entity;
        this.target = target;
        this.nmsWorld = entity.getWorld();
        this.radius = radius;
        this.wave = wave;
        this.cycles = 0;
        this.maxCycles = wave == 1 ? 100 : wave == 2 ? 1 : 10;
        this.blockPosition = new BlockPosition(entity.locX(), entity.locY(), entity.locZ());
        this.initArrayLists();
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        if (this.cycles == 0) { /**to reduce lag*/
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=enderman]");
        }

        int loopCounter = 0;
        do { /**mobs can't spawn within an 8 block radius of 0,0 to prevent them from just falling into the end portal*/
            loopCounter++; //to prevent rare infinite loops
            this.loc = CustomMathHelper.coordsFromHypotenuseAndAngle(this.nmsWorld.getWorld(), this.blockPosition, random.nextDouble() * this.radius, this.target != null ? this.target.locY() + 35.0 : this.entity.locY() + 35.0, 361.0);
        } while ((Math.abs(this.loc.getX() - 0.0) < 8.0 || Math.abs(this.loc.getY() - 0.0) < 8.0) && loopCounter < 20);

        this.index = random.nextInt(this.entitesToSpawn.get(this.wave).size());
        new SpawnEntity(this.nmsWorld, this.entitesToSpawn.get(this.wave).get(this.index), 1, CreatureSpawnEvent.SpawnReason.DROWNED, this.loc, true);
        this.entitesToSpawn.get(this.wave).remove(this.index);
    }

    private void initArrayLists() {
        ArrayList<EntityLiving> entitiesToSpawn1, entitiesToSpawn2, entitiesToSpawn3;

        entitiesToSpawn1 = new ArrayList<>(List.of(
                new CustomEntityBat(this.nmsWorld),
                new CustomEntityBat(this.nmsWorld),
                new CustomEntityBat(this.nmsWorld),
                new CustomEntityBee(this.nmsWorld),
                new CustomEntityBee(this.nmsWorld),
                new CustomEntityBlaze(this.nmsWorld),
                new CustomEntityBlaze(this.nmsWorld),
                new CustomEntityCow(this.nmsWorld),
                new CustomEntityCow(this.nmsWorld),
                new CustomEntityCreeper(this.nmsWorld, 20),
                new CustomEntityCreeper(this.nmsWorld, 20),
                new CustomEntityDrowned(this.nmsWorld),
                new CustomEntityEnderman(this.nmsWorld),
                new CustomEntityEnderman(this.nmsWorld),
                new CustomEntityEnderman(this.nmsWorld),
                new CustomEntityEnderman(this.nmsWorld),
                new CustomEntityEnderman(this.nmsWorld),
                new CustomEntityEndermite(this.nmsWorld),
                new CustomEntityEndermite(this.nmsWorld),
                new CustomEntityEndermite(this.nmsWorld),
                new CustomEntityEndermite(this.nmsWorld),
                new CustomEntityEndermite(this.nmsWorld),
                new CustomEntityEndermite(this.nmsWorld),
                new CustomEntityEvoker(this.nmsWorld),
                new CustomEntityGhast(this.nmsWorld),
                new CustomEntityGuardian(this.nmsWorld),
                new CustomEntityHoglin(this.nmsWorld),
                new CustomEntityHoglin(this.nmsWorld),
                new CustomEntityIllagerIllusioner(this.nmsWorld),
                new CustomEntityRabbit(this.nmsWorld),
                new CustomEntityRabbit(this.nmsWorld),
                new CustomEntityRabbit(this.nmsWorld),
                new CustomEntityRabbit(this.nmsWorld),
                new CustomEntityRabbit(this.nmsWorld),
                new CustomEntityLlama(this.nmsWorld),
                new CustomEntityPhantom(this.nmsWorld),
                new CustomEntityPhantom(this.nmsWorld),
                new CustomEntityPhantom(this.nmsWorld),
                new CustomEntityPhantom(this.nmsWorld),
                new CustomEntityPhantom(this.nmsWorld),
                new CustomEntityPhantom(this.nmsWorld),
                new CustomEntityPiglin(this.nmsWorld),
                new CustomEntityPiglin(this.nmsWorld),
                new CustomEntityPiglin(this.nmsWorld),
                new CustomEntityPillager(this.nmsWorld),
                new CustomEntityPillager(this.nmsWorld),
                new CustomEntitySheep(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySilverfish(this.nmsWorld),
                new CustomEntitySkeleton(this.nmsWorld),
                new CustomEntitySkeleton(this.nmsWorld),
                new CustomEntitySkeleton(this.nmsWorld),
                new CustomEntitySlime(this.nmsWorld, 8),
                new CustomEntitySlime(this.nmsWorld, 4),
                new CustomEntitySlime(this.nmsWorld, 4),
                new CustomEntitySlimeMagmaCube(this.nmsWorld, 16),
                new CustomEntitySpider(this.nmsWorld),
                new CustomEntitySpider(this.nmsWorld),
                new CustomEntitySpiderCave(this.nmsWorld),
                new CustomEntitySkeletonStray(this.nmsWorld),
                new CustomEntitySkeletonStray(this.nmsWorld),
                new CustomEntitySkeletonWither(this.nmsWorld),
                new CustomEntitySkeletonWither(this.nmsWorld),
                new CustomEntityVex(this.nmsWorld),
                new CustomEntityVex(this.nmsWorld),
                new CustomEntityVex(this.nmsWorld),
                new CustomEntityVex(this.nmsWorld),
                new EntityVillager(EntityTypes.VILLAGER, this.nmsWorld),
                new EntityVillager(EntityTypes.VILLAGER, this.nmsWorld),
                new CustomEntityVindicator(this.nmsWorld),
                new CustomEntityVindicator(this.nmsWorld),
                new CustomEntityVindicator(this.nmsWorld),
                new CustomEntityVindicator(this.nmsWorld),
                new CustomEntityWitch(this.nmsWorld),
                new CustomEntityZombie(this.nmsWorld),
                new CustomEntityZombie(this.nmsWorld),
                new CustomEntityZombie(this.nmsWorld),
                new CustomEntityZombie(this.nmsWorld),
                new CustomEntityZombie(this.nmsWorld),
                new CustomEntityZombieHusk(this.nmsWorld),
                new CustomEntityZombieHusk(this.nmsWorld),
                new CustomEntityZombieHusk(this.nmsWorld),
                new CustomEntityZombieHusk(this.nmsWorld),
                new CustomEntityZombiePig(this.nmsWorld),
                new CustomEntityZombiePig(this.nmsWorld),
                new CustomEntityZombiePig(this.nmsWorld),
                new CustomEntityZombiePig(this.nmsWorld),
                new CustomEntityZombiePig(this.nmsWorld),
                new CustomEntityZombiePig(this.nmsWorld),
                new CustomEntityZombiePig(this.nmsWorld),
                new CustomEntityZombieVillager(this.nmsWorld),
                new CustomEntityZombieVillager(this.nmsWorld),
                new CustomEntityZombieVillager(this.nmsWorld)
        ));

        entitiesToSpawn2 = new ArrayList<>(List.of(new CustomEntityWither(this.nmsWorld)));

        entitiesToSpawn3 = new ArrayList<>(List.of(
                new CustomEntityGuardianElder(nmsWorld),
                new CustomEntityIllagerIllusioner(nmsWorld),
                new CustomEntityIllagerIllusioner(nmsWorld),
                new CustomEntityIronGolem(nmsWorld),
                new CustomEntityRavager(nmsWorld),
                new CustomEntitySheepAggressive(nmsWorld),
                new CustomEntityShulker(nmsWorld),
                new CustomEntityShulker(nmsWorld),
                new CustomEntityZoglin(nmsWorld),
                new CustomEntityZombieThor(nmsWorld)
        ));

        this.entitesToSpawn.put(1, entitiesToSpawn1);
        this.entitesToSpawn.put(2, entitiesToSpawn2);
        this.entitesToSpawn.put(3, entitiesToSpawn3);
    }
}
