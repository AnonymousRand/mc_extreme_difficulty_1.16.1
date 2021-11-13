package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityArrowExploding;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class RunnableMeteorRain extends BukkitRunnable {

    private final LivingEntity bukkitShooter;
    private final int type;
    private final double maxRadius;
    private CustomEntityArrowExploding explodingArrow;
    private CustomEntityLargeFireball meteorFireball;
    private CustomEntityLightning newLightning;
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
        this.vec = new Vector(0.0, -1.0, 0.0); //todo: delete if no need for wither skulls
    }

    public RunnableMeteorRain(EntityLiving nmsEntity, int type, double maxRadius, int maxCycles) {
        this.bukkitShooter = (LivingEntity)nmsEntity.getBukkitEntity();
        this.type = type;
        this.maxRadius = maxRadius;
        this.maxCycles = maxCycles;
        this.nmsWorld = nmsEntity.getWorld();
        this.bukkitWorld = this.nmsWorld.getWorld();
        this.playerLoc = new Location(this.bukkitWorld, nmsEntity.locX(), nmsEntity.locY(), nmsEntity.locZ());
        this.vec = new Vector(0.0, -1.0, 0.0); //todo: delete if no need for wither skulls
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            return;
        }

        switch (type) {
            case 1 -> { //exploding arrows
                for (int i = 0; i < 4; i++) {
                    pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, playerLoc.getY() + random.nextDouble() * 20.0 + 20.0, 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to summon projectile at

                    this.explodingArrow = new CustomEntityArrowExploding(this.nmsWorld, this.vec, (byte)(random.nextDouble() < 0.25 ? 1 : 0), this.bukkitShooter, 1.0F); /**25% of arrows are piecing 1*/
                    this.explodingArrow.setPosition(pos.getX(), pos.getY(), pos.getZ());
                    this.nmsWorld.addEntity(this.explodingArrow);
                }
            }
            case 2 -> { //fireballs
                for (int i = 0; i < 4; i++) {
                    pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), random.nextDouble() * this.maxRadius, playerLoc.getY() + random.nextDouble() * 40.0 + 15.0, 361.0);

                    this.meteorFireball = new CustomEntityLargeFireball(EntityTypes.FIREBALL, this.nmsWorld, 2, new Vec3D(0.0, -2.9, 0.0), pos.getX(), pos.getY(), pos.getZ());
                    this.nmsWorld.addEntity(this.meteorFireball);
                }
            }
            case 3 -> { //lightning
                pos = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.playerLoc.getX(), this.playerLoc.getY(), this.playerLoc.getZ()), random.nextDouble() * this.maxRadius, this.bukkitWorld.getHighestBlockYAt(this.playerLoc), -361.0);

                this.newLightning = new CustomEntityLightning(this.nmsWorld);
                this.newLightning.setLocation(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                this.nmsWorld.addEntity(this.newLightning);
            }
        }
    }
}
