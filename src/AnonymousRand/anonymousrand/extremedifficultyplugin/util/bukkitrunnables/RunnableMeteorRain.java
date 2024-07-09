package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityArrowExploding;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.projectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class RunnableMeteorRain extends BukkitRunnable {

    private final org.bukkit.entity.Entity bukkitShooter;
    private final int type;
    private final double maxRadius;
    private final org.bukkit.World bukkitWorld;
    private final World nmsWorld;
    private int cycles;
    private final int maxCycles;
    private Location playerLoc;
    private final Vector vec;
    private static final Random random = new Random();

    public RunnableMeteorRain(Player player, int type, double maxRadius, int maxCycles) {
        this.bukkitShooter = player;
        this.playerLoc = player.getLocation();
        this.type = type;
        this.maxRadius = maxRadius;
        this.maxCycles = maxCycles;
        this.bukkitWorld = player.getWorld();
        this.nmsWorld = ((CraftWorld)player.getWorld()).getHandle();
        this.vec = new Vector(0.0, -1.0, 0.0);
    }

    public RunnableMeteorRain(Entity nmsEntity, int type, double maxRadius, int maxCycles) {
        this.bukkitShooter = nmsEntity.getBukkitEntity();
        this.type = type;
        this.maxRadius = maxRadius;
        this.maxCycles = maxCycles;
        this.nmsWorld = nmsEntity.getWorld();
        this.bukkitWorld = this.nmsWorld.getWorld();
        this.playerLoc = new Location(this.bukkitWorld, nmsEntity.locX(), nmsEntity.locY(), nmsEntity.locZ());
        this.vec = new Vector(0.0, -1.0, 0.0);
    }

    @Override
    public void run() {
        this.cycles++;
        if (this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        Location pos;
        switch (this.type) {
            case 1: // exploding arrows
                for (int i = 0; i < 4; i++) {
                    pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, playerLoc.getY() + random.nextDouble() * 40.0 + 15.0, 361.0);
                    CustomEntityArrowExploding explodingArrow = new CustomEntityArrowExploding(this.nmsWorld, this.vec, (byte) (random.nextDouble() < 0.25 ? 1 : 0), this.bukkitShooter instanceof LivingEntity ? (LivingEntity) this.bukkitShooter : null, 1.0F); /* 25% of arrows are piecing 1 */
                    explodingArrow.setPosition(pos.getX(), pos.getY(), pos.getZ());
                    this.nmsWorld.addEntity(explodingArrow);
                }
                break;
            case 2: // fireballs
                for (int i = 0; i < 4; i++) {
                    pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, playerLoc.getY() + random.nextDouble() * 40.0 + 15.0, 361.0);
                    CustomEntityLargeFireball meteorFireball = new CustomEntityLargeFireball(EntityTypes.FIREBALL, this.nmsWorld, 2, new Vec3D(0.0, -2.9, 0.0), pos.getX(), pos.getY(), pos.getZ());
                    this.nmsWorld.addEntity(meteorFireball);
                }
                break;
            case 3: // lightning
                pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.playerLoc.getX(), this.playerLoc.getY(), this.playerLoc.getZ()), random.nextDouble() * this.maxRadius, this.bukkitWorld.getHighestBlockYAt(this.playerLoc), 361.0);
                new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, pos, false);
                break;
            case 4: // dragon's breath clouds
                pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, this.bukkitWorld.getHighestBlockYAt(this.playerLoc), 361.0);

                for (int i = 0; i < 3; i++) {
                    CustomEntityAreaEffectCloud newAEC = new CustomEntityAreaEffectCloud(this.nmsWorld, random.nextInt(2) + 4, 120, 15);
                    newAEC.setParticle(Particles.DRAGON_BREATH);
                    newAEC.addEffect(new MobEffect(MobEffects.HARM, 1, 2));
                    newAEC.setPosition(pos.getX(), pos.getY() + i, pos.getZ());
                    this.nmsWorld.addEntity(newAEC);
                }

                break;
        }
    }
}
