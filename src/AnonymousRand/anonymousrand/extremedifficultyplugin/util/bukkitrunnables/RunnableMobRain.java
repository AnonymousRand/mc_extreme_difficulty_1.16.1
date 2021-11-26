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

    private final double y;
    private final EntityLiving target;
    private final World nmsWorld;
    private final org.bukkit.World bukkitWorld;
    private final double radius;
    private final int wave;
    private int cycles, index;
    private final int maxCycles;
    public static HashMap<Integer, ArrayList<EntityLiving>> permanentEntitiesToSpawn = new HashMap<>();
    public HashMap<Integer, ArrayList<EntityLiving>> entitesToSpawn = new HashMap<>();
    private final BlockPosition originPos;
    private Location loc;
    private static final Random random = new Random();

    public RunnableMobRain(Entity entity, @Nullable EntityLiving target, double radius, int wave) {
        this.y = entity.locY();
        this.target = target;
        this.nmsWorld = entity.getWorld();
        this.bukkitWorld = nmsWorld.getWorld();
        this.radius = radius;
        this.wave = wave;
        this.cycles = 0;
        this.maxCycles = wave == 1 ? 100 : 14;
        this.originPos = new BlockPosition(entity.locX(), entity.locY(), entity.locZ());
        this.entitesToSpawn = permanentEntitiesToSpawn;
    }

    public RunnableMobRain(World nmsWorld, double y, BlockPosition originPos, double radius, int wave) {
        this.nmsWorld = nmsWorld;
        this.bukkitWorld = nmsWorld.getWorld();
        this.y = y;
        this.target = null;
        this.originPos = originPos;
        this.radius = radius;
        this.wave = wave;
        this.cycles = 0;
        this.maxCycles = wave == 1 ? 100 : 14;
        this.entitesToSpawn = permanentEntitiesToSpawn;
    }

    @Override
    public void run() {
        if (++this.cycles > this.maxCycles) {
            this.cancel();
            this.entitesToSpawn = permanentEntitiesToSpawn; //reset arraylist for next mob rain
            return;
        }

        if (this.cycles == 0) { /**to reduce lag*/
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "kill @e[type=enderman]");
        }

        int loopCounter = 0;
        do { /**mobs can't spawn within an 8 block radius of 0,0 to prevent them from just falling into the end portal and they can't spawn into the void*/
            loopCounter++; //to prevent rare infinite loops
            this.loc = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, this.originPos, random.nextDouble() * this.radius, this.target != null ? this.target.locY() + 35.0 : this.y + 35.0, 361.0);
        } while ((Math.abs(this.loc.getX() - 0.0) < 8.0 || Math.abs(this.loc.getY() - 0.0) < 8.0 || this.bukkitWorld.getHighestBlockYAt(this.loc) < 10.0) && loopCounter < 20);

        this.index = random.nextInt(this.entitesToSpawn.get(this.wave).size());
        new SpawnEntity(this.nmsWorld, this.entitesToSpawn.get(this.wave).get(this.index), 1, CreatureSpawnEvent.SpawnReason.DROWNED, this.loc, true, true); /**these mobs have nametags so they don't despawn or take up mob cap*/
        this.entitesToSpawn.get(this.wave).remove(this.index);
    }
}
