package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityCreeper;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;

public class SpawnLivingEntity extends BukkitRunnable {

    private JavaPlugin plugin = null;
    private int maxFuseTicks = 0;
    private final EntityLiving firstEntityToSpawn;
    private final int numToSpawn;
    private final CreatureSpawnEvent.SpawnReason spawnReason;
    private org.bukkit.entity.Entity bukkitOriginalEntity;
    private final boolean removeOriginal, equipBoots;
    private Entity entityToSpawn;
    private World nmsWorld;
    private Location pos;
    private static org.bukkit.inventory.ItemStack boots;

    static {
        boots = new ItemStack(Material.LEATHER_BOOTS);
        boots.addEnchantment(Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back*/
        boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);
    }

    public SpawnLivingEntity(World nmsWorld, EntityLiving firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nullable org.bukkit.entity.Entity bukkitOriginalEntity, @Nullable Entity nmsOriginalEntity, boolean removeOriginal, boolean equipBoots) {
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = bukkitOriginalEntity == null ? nmsOriginalEntity.getBukkitEntity() : bukkitOriginalEntity;
        this.removeOriginal = removeOriginal;
        this.equipBoots = equipBoots;
        this.pos = this.bukkitOriginalEntity.getLocation();
    }

    public SpawnLivingEntity(JavaPlugin plugin, World nmsWorld, EntityLiving firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nullable org.bukkit.entity.Entity bukkitOriginalEntity, @Nullable Entity nmsOriginalEntity, boolean removeOriginal, boolean equipBoots) {
        this.plugin = plugin;
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = bukkitOriginalEntity == null ? nmsOriginalEntity.getBukkitEntity() : bukkitOriginalEntity;
        this.removeOriginal = removeOriginal;
        this.equipBoots = equipBoots;
        this.pos = this.bukkitOriginalEntity.getLocation();
    }

    public SpawnLivingEntity(int maxFuseTicks, World nmsWorld, EntityLiving firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nullable org.bukkit.entity.Entity bukkitOriginalEntity, @Nullable Entity nmsOriginalEntity, boolean removeOriginal, boolean equipBoots) {
        this.maxFuseTicks = maxFuseTicks;
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = bukkitOriginalEntity == null ? nmsOriginalEntity.getBukkitEntity() : bukkitOriginalEntity;
        this.removeOriginal = removeOriginal;
        this.equipBoots = equipBoots;
        this.pos = this.bukkitOriginalEntity.getLocation();
    }

    public SpawnLivingEntity(World nmsWorld, EntityLiving firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nonnull Location loc, boolean equipBoots) {
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = null;
        this.removeOriginal = false;
        this.equipBoots = equipBoots;
        this.pos = loc;
    }

    public SpawnLivingEntity(JavaPlugin plugin, World nmsWorld, EntityLiving firstEntityToSpawn, int numToSpawn, @Nullable CreatureSpawnEvent.SpawnReason spawnReason, @Nonnull Location loc, boolean equipBoots) {
        this.plugin = plugin;
        this.nmsWorld = nmsWorld;
        this.firstEntityToSpawn = firstEntityToSpawn;
        this.numToSpawn = numToSpawn;
        this.spawnReason = spawnReason == null ? CreatureSpawnEvent.SpawnReason.NATURAL : spawnReason;
        this.bukkitOriginalEntity = null;
        this.removeOriginal = false;
        this.equipBoots = equipBoots;
        this.pos = loc;
    }

    public void run() {
        for (int i = 0; i < this.numToSpawn; i++) {
            if (i > 0) {
                try {
                    if (this.firstEntityToSpawn instanceof CustomEntityCreeper) {
                        this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(World.class, int.class).newInstance(this.nmsWorld, this.maxFuseTicks);
                    } else if (this.firstEntityToSpawn.getClass().getName().toLowerCase().contains("custom")) {
                        try {
                            this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(World.class).newInstance(this.nmsWorld); //create a new instance of the same class as the first entity if spawning multiple
                        } catch (NoSuchMethodException e) {
                            this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(World.class, JavaPlugin.class).newInstance(this.nmsWorld, this.plugin);
                        }
                    } else {
                        this.entityToSpawn = this.firstEntityToSpawn.getClass().getConstructor(EntityTypes.class, World.class).newInstance(this.firstEntityToSpawn.getEntityType(), this.nmsWorld); //for vanilla mobs that use a different constructor: create a new instance of the same class as the first entity if spawning multiple
                    }
                } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            } else {
                this.entityToSpawn = this.firstEntityToSpawn;
            }

            this.entityToSpawn.setPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
            this.nmsWorld.addEntity(this.entityToSpawn, this.spawnReason);

            if (this.equipBoots) {
                ((LivingEntity)this.entityToSpawn.getBukkitEntity()).getEquipment().setBoots(boots);
            }
        }

        if (this.removeOriginal) {
            this.bukkitOriginalEntity.remove();
        }
    }
}
