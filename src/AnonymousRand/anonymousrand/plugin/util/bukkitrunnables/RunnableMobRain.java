package AnonymousRand.anonymousrand.plugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.*;
import AnonymousRand.anonymousrand.plugin.listeners.ListenerMobSpawnAndReplaceWithCustom;
import AnonymousRand.anonymousrand.plugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nullable;
import java.util.*;

public class RunnableMobRain extends BukkitRunnable {

    private final double y;
    private final EntityLiving target;
    private final World nmsWorld;
    private final org.bukkit.World bukkitWorld;
    private final double radius;
    private final int wave;
    private int cycleCount;
    private final int maxCycleCount;
    public HashMap<Integer, ArrayList<EntityLiving>> entitiesToSpawn = new HashMap<>();
    private final BlockPosition originPos;
    private static final Random random = new Random();

    public RunnableMobRain(Entity entity, @Nullable EntityLiving target, double radius, int wave) {
        this.y = entity.locY();
        this.target = target;
        this.nmsWorld = entity.getWorld();
        this.bukkitWorld = nmsWorld.getWorld();
        this.radius = radius;
        this.wave = wave;
        this.cycleCount = 0;
        this.maxCycleCount = wave == 1 ? 100 : 12;
        this.originPos = new BlockPosition(entity.locX(), entity.locY(), entity.locZ());
        this.initMobRainArrayLists(this.nmsWorld);
    }

    public RunnableMobRain(World nmsWorld, double y, BlockPosition originPos, double radius, int wave) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.y = y;
        this.target = null;
        this.originPos = originPos;
        this.radius = radius;
        this.wave = wave;
        this.cycleCount = 0;
        this.maxCycleCount = wave == 1 ? 100 : 12;
        this.initMobRainArrayLists(this.nmsWorld);
    }

    @Override
    public void run() {
        if (++this.cycleCount > this.maxCycleCount) {
            this.cancel();
            return;
        }

        if (this.cycleCount == 1) { /* to reduce lag */
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=enderman]");
        }

        int loopCounter = 0;
        Location bukkitLoc;
        do { /* mobs can't spawn within an 8 block radius of 0,0 to prevent them from just falling into the end portal and they can't spawn into the void */
            loopCounter++; // to prevent rare infinite loops
            bukkitLoc = CustomMathHelper.coordsFromHypotAndAngle(this.bukkitWorld, this.originPos, random.nextDouble() * this.radius, this.target != null ? this.target.locY() + 35.0 : this.y + 35.0, 361.0);
        } while ((Math.abs(bukkitLoc.getX() - 0.0) < 8.0 || Math.abs(bukkitLoc.getY() - 0.0) < 8.0 || this.bukkitWorld.getHighestBlockYAt(bukkitLoc) < 10.0) && loopCounter < 20);

        int index = random.nextInt(this.entitiesToSpawn.get(this.wave).size());
        new SpawnEntity(this.nmsWorld, this.entitiesToSpawn.get(this.wave).get(index), 1, CreatureSpawnEvent.SpawnReason.DROWNED, bukkitLoc, true, true); /* these mobs have nametags so they don't despawn or take up mob cap */
        this.entitiesToSpawn.get(this.wave).remove(index);
    }

    public void initMobRainArrayLists(World nmsWorld) {
        ArrayList<EntityLiving> entitiesToSpawn1, entitiesToSpawn2;

        entitiesToSpawn1 = new ArrayList<>(Arrays.asList(
                new CustomEntityBat(nmsWorld),
                new CustomEntityBat(nmsWorld),
                new CustomEntityBee(nmsWorld),
                new CustomEntityBee(nmsWorld),
                new CustomEntityBlaze(nmsWorld),
                new CustomEntityCow(nmsWorld),
                new CustomEntityCow(nmsWorld),
                new CustomEntityCreeper(nmsWorld),
                new CustomEntityCreeper(nmsWorld),
                new CustomEntityDrowned(nmsWorld),
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
                new CustomEntityLlama(nmsWorld),
                new CustomEntityPhantom(nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize),
                new CustomEntityPhantom(nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize),
                new CustomEntityPhantom(nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize),
                new CustomEntityPhantom(nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize),
                new CustomEntityPhantom(nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize),
                new CustomEntityPhantom(nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize),
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
                new CustomEntitySlime(nmsWorld, 8),
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

        entitiesToSpawn2 = new ArrayList<>(Arrays.asList(
                new CustomEntityGuardianElder(nmsWorld),
                new CustomEntityIllusioner(nmsWorld),
                new CustomEntityIronGolem(nmsWorld),
                new CustomEntityRavager(nmsWorld),
                new CustomEntitySheepAggressive(nmsWorld),
                new CustomEntityShulker(nmsWorld),
                new CustomEntityShulker(nmsWorld),
                new CustomEntitySpider(nmsWorld),
                new CustomEntitySpider(nmsWorld),
                new CustomEntityZoglin(nmsWorld),
                new CustomEntityZombieThor(nmsWorld)
        ));

        this.entitiesToSpawn.put(1, entitiesToSpawn1);
        this.entitiesToSpawn.put(2, entitiesToSpawn2);
    }
}
