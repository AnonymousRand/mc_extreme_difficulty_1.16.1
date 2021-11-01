package AnonymousRand.ExtremeDifficultyPlugin.CustomEntities.CustomMobs;

import AnonymousRand.ExtremeDifficultyPlugin.CustomGoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class CustomEntityZombie extends EntityZombie {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private Field bA;

    public CustomEntityZombie(World world) {
        super(EntityTypes.ZOMBIE, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.attacks = 0;

        try {
            this.bA = EntityZombie.class.getDeclaredField("bA");
            this.bA.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() { /**no longer targets iron golems*/
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false));
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (super.damageEntity(damagesource, f)) {
            EntityLiving entityliving = this.getGoalTarget();

            if (entityliving == null) {
                entityliving = (EntityLiving) damagesource.getEntity();
            }

            if (!(entityliving instanceof EntityPlayer)) { /**only player damage can trigger reinforcement spawning*/
                return true;
            }

            if ((double)this.random.nextFloat() < this.b(GenericAttributes.SPAWN_REINFORCEMENTS)) { /**zombies can now spawn reinforcements on any difficulty*/
                for (int ii = 0; ii < (this.random.nextDouble() < 0.985 ? 1 : 30); ii++) { /**1.5% chance to make 30 spawn attempts instead of 1 (on average, about half of them succeed)*/
                    int i = MathHelper.floor(this.locX());
                    int j = MathHelper.floor(this.locY());
                    int k = MathHelper.floor(this.locZ());
                    CustomEntityZombie newZombie = new CustomEntityZombie(this.getWorld());

                    for (int l = 0; l < 50; ++l) {
                        int i1 = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                        int j1 = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                        int k1 = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                        BlockPosition blockposition = new BlockPosition(i1, j1, k1);
                        EntityTypes<?> entitytypes = newZombie.getEntityType();
                        EntityPositionTypes.Surface entitypositiontypes_surface = EntityPositionTypes.a(entitytypes);

                        if (SpawnerCreature.a(entitypositiontypes_surface, (IWorldReader) this.getWorld(), blockposition, entitytypes) && EntityPositionTypes.a(entitytypes, this.getWorld(), EnumMobSpawn.REINFORCEMENT, blockposition, this.getWorld().random)) {
                            newZombie.setPosition((double) i1, (double) j1, (double) k1);
                            if (!this.getWorld().isPlayerNearby((double) i1, (double) j1, (double) k1, 7.0D) && this.getWorld().i(newZombie) && this.getWorld().getCubes(newZombie) && !this.getWorld().containsLiquid(newZombie.getBoundingBox())) {
                                this.getWorld().addEntity(newZombie);
                                newZombie.prepare(this.getWorld(), this.getWorld().getDamageScaler(newZombie.getChunkCoordinates()), EnumMobSpawn.REINFORCEMENT, (GroupDataEntity) null, (NBTTagCompound) null);
                                this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement caller charge", -0.125D, AttributeModifier.Operation.ADDITION)); /**zombies experience a 12.5% decrease in reinforcement summon chance instead of 5% if summoned reinforcements or was summoned as reinforcement*/
                                newZombie.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).addModifier(new AttributeModifier("Zombie reinforcement callee charge", -0.125D, AttributeModifier.Operation.ADDITION));
                                break;
                            }
                        }
                    }
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public double getFollowRange() { /**zombies have 40 block detection range (setting attribute doesn't work)*/
        return 40.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClientSide && this.isAlive() && !this.isNoAI()) { /**converts to drowned 4 times faster*/
            if (this.isDrownConverting()) {
                this.drownedConversionTime -= 3;
                if (this.drownedConversionTime < 0) {
                    this.eQ();
                }
            } else if (this.eO()) {
                try {
                    if (this.a((Tag) TagsFluid.WATER)) {
                        this.bA.setInt(this, this.bA.getInt(this) + 3);
                    } else {
                        this.bA.setInt(this, -1);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        if (this.ticksLived % 60 == 0 && this.getGoalTarget() != null) { /**every 3 seconds that the zombie has a target, it breaks the block that it is looking at*/
            Block block = ((LivingEntity)this.getBukkitEntity()).getTargetBlock(null, (int)this.getFollowRange());
            org.bukkit.Material type = block.getType();

            if (type != org.bukkit.Material.BEDROCK && type != org.bukkit.Material.END_GATEWAY && type != org.bukkit.Material.END_PORTAL && type != org.bukkit.Material.END_PORTAL_FRAME && type != org.bukkit.Material.NETHER_PORTAL && type != org.bukkit.Material.COMMAND_BLOCK  && type != org.bukkit.Material.COMMAND_BLOCK_MINECART && type != org.bukkit.Material.STRUCTURE_BLOCK && type != org.bukkit.Material.JIGSAW && type != org.bukkit.Material.BARRIER && type != org.bukkit.Material.SPAWNER && type != org.bukkit.Material.COBWEB && type != org.bukkit.Material.OBSIDIAN && type != org.bukkit.Material.CRYING_OBSIDIAN && type != org.bukkit.Material.ANCIENT_DEBRIS && type != org.bukkit.Material.NETHERITE_BLOCK && type != org.bukkit.Material.WATER && type != org.bukkit.Material.LAVA) { //as long as it isn't one of these blocks
                block.setType(org.bukkit.Material.AIR);
            } else if (type == org.bukkit.Material.OBSIDIAN || type == org.bukkit.Material.CRYING_OBSIDIAN || type == org.bukkit.Material.ANCIENT_DEBRIS || type == org.bukkit.Material.NETHERITE_BLOCK) { //50% chance to break these blocks
                if (this.random.nextDouble() < 0.5) {
                    block.setType(org.bukkit.Material.AIR);
                }
            }
        }

        if (this.ticksLived == 10) { /**zombies are always babies, move 3x faster, and have a 50% chance to summon a reinforcement when hit by a player, but only have 12 health*/
            this.setBaby(true);
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.69);
            this.getAttributeInstance(GenericAttributes.SPAWN_REINFORCEMENTS).setValue(0.5);
            this.setHealth(12.0F);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(12.0);
        }

        if (this.ticksLived % 5 == 2) {
            if (this.getLastDamager() != null) {
                EntityLiving target = this.getLastDamager();

                if (!(target instanceof EntityPlayer)) { /**mobs only target players (in case mob damage listener doesn't register)*/
                    this.setLastDamager(null);
                }
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);*/
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }
}
