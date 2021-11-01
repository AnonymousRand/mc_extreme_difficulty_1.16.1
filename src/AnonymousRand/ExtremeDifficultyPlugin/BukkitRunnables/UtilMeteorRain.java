package AnonymousRand.ExtremeDifficultyPlugin.BukkitRunnables;

import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomProjectiles.CustomEntityArrowExploding;
import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomProjectiles.CustomEntityLargeFireball;
import AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.misc.CustomEntityLightning;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.BlockPosition;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.Vec3D;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class UtilMeteorRain extends BukkitRunnable {

    private final Player player;
    private final int type;
    private CustomEntityArrowExploding explodingArrow;
    private CustomEntityLargeFireball meteorFireball;
    private CustomEntityLightning newLightning;
    private final org.bukkit.World bukkitWorld;
    private final World nmsWorld;
    private int cycles;
    private final int maxCycles;
    private Location playerLoc, pos;
    private final Vector vec;
    private final CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();
    private final Random random = new Random();

    public UtilMeteorRain(Player player, int type, int maxCycles) {
        this.player = player;
        this.type = type;
        this.maxCycles = maxCycles;
        this.bukkitWorld = player.getWorld();
        this.nmsWorld = ((CraftWorld)player.getWorld()).getHandle();
        this.playerLoc = player.getLocation();
        this.vec = new Vector(0.0, -1.0, 0.0); //todo: delete if no need for wither skulls
    }

    public void run() {
        if (++this.cycles >= this.maxCycles) {
            this.cancel();
        }

        switch (type) {
            case 1 -> { //exploding arrows
                for (int i = 0; i < 4; i++) {
                    pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), this.random.nextDouble() * 71, playerLoc.getY() + this.random.nextDouble() * 20.0 + 20.0, 361.0); //gets coords for a random angle (0-360) with fixed hypotenuse to summon projectile at

                    this.explodingArrow = new CustomEntityArrowExploding(this.nmsWorld, this.vec, (byte)(this.random.nextDouble() < 0.25 ? 1 : 0), this.player, 1.0F); /**25% of arrows are piecing 1*/
                    this.explodingArrow.setPosition(pos.getX(), pos.getY(), pos.getZ());
                    this.nmsWorld.addEntity(this.explodingArrow);
                }
            }
            case 2 -> { //fireballs
                for (int i = 0; i < 4; i++) {
                    pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(playerLoc.getX(), playerLoc.getY(), playerLoc.getZ()), this.random.nextDouble() * 71, playerLoc.getY() + this.random.nextDouble() * 40.0 + 15.0, 361.0);

                    this.meteorFireball = new CustomEntityLargeFireball(EntityTypes.FIREBALL, this.nmsWorld, 2, new Vec3D(0.0, -2.9, 0.0), pos.getX(), pos.getY(), pos.getZ());
                    this.nmsWorld.addEntity(this.meteorFireball);
                }
            }
            case 3 -> { //lightning
                pos = coordsFromHypotenuse.CoordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.playerLoc.getX(), this.playerLoc.getY(), this.playerLoc.getZ()), this.random.nextDouble() * 71, this.bukkitWorld.getHighestBlockYAt(this.playerLoc), -361.0);

                this.newLightning = new CustomEntityLightning(this.nmsWorld);
                this.newLightning.setLocation(pos.getX(), pos.getY(), pos.getZ(), 0.0F, 0.0F);
                this.nmsWorld.addEntity(this.newLightning);
            }
        }
    }
}
