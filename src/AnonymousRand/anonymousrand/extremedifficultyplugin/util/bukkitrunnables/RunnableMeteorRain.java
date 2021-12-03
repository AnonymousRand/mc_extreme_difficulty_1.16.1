package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityArrowExploding;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
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
    private CustomEntityArrowExploding explodingArrow;
    private CustomEntityLargeFireball meteorFireball;
    private CustomEntityLightning newLightning;
    private CustomEntityAreaEffectCloud newAEC;
    private final org.bukkit.World bukkitWorld;
    private final World nmsWorld;
    private int cycles;
    private final int maxCycles;
    private Location playerLoc, pos;
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
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        switch (this.type) {
            case 1 -> { //exploding arrows
                for (int i = 0; i < 4; i++) {
                    this.pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, playerLoc.getY() + random.nextDouble() * 40.0 + 15.0, 361.0);
                    this.explodingArrow = new CustomEntityArrowExploding(this.nmsWorld, this.vec, (byte)(random.nextDouble() < 0.25 ? 1 : 0), this.bukkitShooter instanceof LivingEntity ? (LivingEntity)this.bukkitShooter : null, 1.0F); /**25% of arrows are piecing 1*/
                    this.explodingArrow.setPosition(pos.getX(), this.pos.getY(), this.pos.getZ());
                    this.nmsWorld.addEntity(this.explodingArrow);
                }
            }
            case 2 -> { //fireballs
                for (int i = 0; i < 4; i++) {
                    this.pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, playerLoc.getY() + random.nextDouble() * 40.0 + 15.0, 361.0);
                    this.meteorFireball = new CustomEntityLargeFireball(EntityTypes.FIREBALL, this.nmsWorld, 2, new Vec3D(0.0, -2.9, 0.0), this.pos.getX(), this.pos.getY(), this.pos.getZ());
                    this.nmsWorld.addEntity(this.meteorFireball);
                }
            }
            case 3 -> { //lightning
                this.pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.playerLoc.getX(), this.playerLoc.getY(), this.playerLoc.getZ()), random.nextDouble() * this.maxRadius, this.bukkitWorld.getHighestBlockYAt(this.playerLoc), 361.0);
                new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, this.pos, false);
            }
            case 4 -> { //dragon's breath clouds
                this.pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, this.bukkitWorld.getHighestBlockYAt(this.playerLoc), 361.0);
                this.newAEC = new CustomEntityAreaEffectCloud(this.nmsWorld, 4, 120, 15);
                this.newAEC.setParticle(Particles.DRAGON_BREATH);
                this.newAEC.addEffect(new MobEffect(MobEffects.HARM, 1, 2));
                this.newAEC.setPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
                this.nmsWorld.addEntity(this.newAEC);

                this.newAEC = new CustomEntityAreaEffectCloud(this.nmsWorld, 4, 120, 15);
                this.newAEC.setParticle(Particles.DRAGON_BREATH);
                this.newAEC.addEffect(new MobEffect(MobEffects.HARM, 1, 2));
                this.newAEC.setPosition(this.pos.getX(), this.pos.getY() + 1.0, this.pos.getZ());
                this.nmsWorld.addEntity(this.newAEC);
            }
        }
    }
}
