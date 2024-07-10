package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityCreeper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityPhantom;
import net.minecraft.server.v1_16_R1.Entity;
import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

public class SpawnEntity extends BukkitRunnable {

    private int maxFuseTicksOrPhantomSize = 0;
    private final Entity firstEntityToSpawn;
    private final int numToSpawn;
    private final CreatureSpawnEvent.SpawnReason spawnReason;
    private final org.bukkit.entity.Entity bukkitOriginalEntity;
    private final boolean phantomDuplicate,removeOriginal, equipBoots, setNametag;
    private Entity entityToSpawn;
    private final World nmsWorld;
    private final Location bukkitLoc;
    private static final org.bukkit.inventory.ItemStack boots;

    static {
        boots = new ItemStack(Material.LEATHER_BOOTS);
        boots.addEnchantment(Enchantment.DEPTH_STRIDER, 3); /* most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
        boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);
    }

    public SpawnEntity(World nmsWorld, Entity firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nullable org.bukkit.entity.Entity bukkitOriginalEntity, @Nullable Entity nmsOriginalEntity, boolean removeOriginal, boolean equipBoots) {
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = bukkitOriginalEntity == null ? nmsOriginalEntity.getBukkitEntity() : bukkitOriginalEntity;
        this.phantomDuplicate = false;
        this.removeOriginal = removeOriginal;
        this.equipBoots = equipBoots;
        this.setNametag = false;
        this.bukkitLoc = this.bukkitOriginalEntity.getLocation();
        this.run();
    }

    public SpawnEntity(World nmsWorld, int maxFuseTicksOrPhantomSize, Entity firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nullable org.bukkit.entity.Entity bukkitOriginalEntity, @Nullable Entity nmsOriginalEntity, boolean removeOriginal, boolean equipBoots) {
        this.maxFuseTicksOrPhantomSize = maxFuseTicksOrPhantomSize;
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = bukkitOriginalEntity == null ? nmsOriginalEntity.getBukkitEntity() : bukkitOriginalEntity;
        this.phantomDuplicate = false;
        this.removeOriginal = removeOriginal;
        this.equipBoots = equipBoots;
        this.setNametag = false;
        this.bukkitLoc = this.bukkitOriginalEntity.getLocation();
        this.run();
    }

    public SpawnEntity(World nmsWorld, int maxFuseTicksOrPhantomSize, boolean phantomDuplicate, Entity firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nullable org.bukkit.entity.Entity bukkitOriginalEntity, @Nullable Entity nmsOriginalEntity, boolean removeOriginal, boolean equipBoots) {
        this.maxFuseTicksOrPhantomSize = maxFuseTicksOrPhantomSize;
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = bukkitOriginalEntity == null ? nmsOriginalEntity.getBukkitEntity() : bukkitOriginalEntity;
        this.phantomDuplicate = phantomDuplicate;
        this.removeOriginal = removeOriginal;
        this.equipBoots = equipBoots;
        this.setNametag = false;
        this.bukkitLoc = this.bukkitOriginalEntity.getLocation();
        this.run();
    }

    public SpawnEntity(World nmsWorld, Entity firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nonnull Location bukkitLoc, boolean equipBoots) {
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = null;
        this.phantomDuplicate = false;
        this.removeOriginal = false;
        this.equipBoots = equipBoots;
        this.setNametag = false;
        this.bukkitLoc = bukkitLoc;
        this.run();
    }

    public SpawnEntity(World nmsWorld, Entity firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nonnull Location bukkitLoc, boolean equipBoots, boolean setNametag) {
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = null;
        this.phantomDuplicate = false;
        this.removeOriginal = false;
        this.equipBoots = equipBoots;
        this.setNametag = setNametag;
        this.bukkitLoc = bukkitLoc;
        this.run();
    }

    public SpawnEntity(World nmsWorld, int maxFuseTicksOrPhantomSize, Entity firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nonnull Location bukkitLoc, boolean equipBoots) {
        this.maxFuseTicksOrPhantomSize = maxFuseTicksOrPhantomSize;
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = null;
        this.phantomDuplicate = false;
        this.removeOriginal = false;
        this.equipBoots = equipBoots;
        this.setNametag = false;
        this.bukkitLoc = bukkitLoc;
        this.run();
    }

    @Override
    public void run() {
        for (int i = 0; i < this.numToSpawn; i++) {
            if (i > 0) {
                try {
                    if (this.firstEntityToSpawn instanceof CustomEntityCreeper || (this.firstEntityToSpawn instanceof CustomEntityPhantom && !this.phantomDuplicate)) {
                        this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(World.class, int.class).newInstance(this.nmsWorld, this.maxFuseTicksOrPhantomSize);
                    } else if (this.firstEntityToSpawn instanceof CustomEntityPhantom) {
                        this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(World.class, int.class, boolean.class).newInstance(this.nmsWorld, this.maxFuseTicksOrPhantomSize, this.phantomDuplicate);
                    } else if (this.firstEntityToSpawn.getClass().getName().toLowerCase().contains("custom")) {
                        this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(World.class).newInstance(this.nmsWorld); // create a new instance of the same class as the first entity if spawning multiple
                    } else {
                        this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(EntityTypes.class, World.class).newInstance(this.firstEntityToSpawn.getEntityType(), this.nmsWorld); // for vanilla mobs that use a different constructor: create a new instance of the same class as the first entity if spawning multiple
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                this.entityToSpawn = this.firstEntityToSpawn;
            }

            if (this.removeOriginal) {
                this.bukkitOriginalEntity.remove();
            }

            if (this.entityToSpawn != null) {
                this.entityToSpawn.setPosition(this.bukkitLoc.getX(), this.bukkitLoc.getY(), this.bukkitLoc.getZ());
                this.nmsWorld.addEntity(this.entityToSpawn, this.spawnReason);

                if (this.setNametag) {
                    this.entityToSpawn.getBukkitEntity().setCustomName("Won't despawn");
                }

                if (this.equipBoots) {
                    ((LivingEntity) this.entityToSpawn.getBukkitEntity()).getEquipment().setBoots(boots);
                }
            }
        }
    }
}