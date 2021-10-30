package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import AnonymousRand.ExtremeDifficultyPlugin.util.CoordsFromHypotenuse;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntitySkeletonStray extends EntitySkeletonStray {

    public boolean spawnMob, spawnExplodingArrow;
    public int attacks;
    private boolean a25, a60;

    private final CustomPathfinderGoalBowShoot<EntitySkeletonAbstract> b = new CustomPathfinderGoalBowShoot<>(this, 1.0D, 20, 22.0F); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/

    public CustomEntitySkeletonStray(World world) {
        super(EntityTypes.STRAY, world);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); //makes sure that it has a bow
        this.spawnMob = false;
        this.spawnExplodingArrow = false;
        this.attacks = 0;
        this.a25 = false;
        this.a60 = false;
    }

    @Override
    protected void initPathfinder() { /**no longer avoids sun and wolves or targets iron golems*/
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /**custom goal that spawns lightning randomly*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportToPlayer(this, this.getFollowRange(), 300, 0.004)); /**custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
    }

    @Override
    public void a(EntityLiving entityliving, float f) {
        this.attacks++;
        double rand = random.nextDouble();

        if (rand < (this.attacks < 25 ? 0.9 : this.attacks < 35 ? 0.85 : this.attacks < 45 ? 0.8 : 0.7)) { /**strays have 4 choices of attack: 70% (50% if more than 15 attacks) chance to shoot 70 normal slowness arrows, 20% (40% if more than 15 attacks) chance to shoot 70 flaming slowness arrows, 5% chance to shoot 10 power 1 exploding arrows, 5% chance to shoot a custom arrow that spawns a mob on impact*/
            boolean fire = random.nextDouble() < (this.attacks < 15 ? 0.222222222 : 0.444444444);
            this.spawnMob = false;
            this.spawnExplodingArrow = false;

            for (int i = 0; i < 75; i++) { /**shoots 75 arrows at a time with increased inaccuracy to seem like a cone*/
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);
                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.e(0.3333333333333333D) - entityarrow.locY();
                double d2 = entityliving.locZ() - this.locZ();
                double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);

                if (this.random.nextDouble() <= 0.02) { /**2% of arrows shot are piercing 1*/
                    entityarrow.setPierceLevel((byte)1);
                }

                if (fire) {
                    entityarrow.setOnFire(50);
                }

                entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float)(40 - this.world.getDifficulty().a() * 4));
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                this.world.addEntity(entityarrow);
            }
        } else if (rand < (this.attacks < 35 ? 0.95 : 0.9)) { /**increase chances of mob and exploding arrows as more attacks go on*/
            for (int i = 0; i < 10; i++) {
                this.spawnExplodingArrow = true;
                this.spawnMob = false;
                ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
                EntityArrow entityarrow = this.b(itemstack, f);

                double d0 = entityliving.locX() - this.locX();
                double d1 = entityliving.e(0.3333333333333333D) - entityarrow.locY();
                double d2 = entityliving.locZ() - this.locZ();

                entityarrow.shoot(d0, d1, d2, 1.6F, (float)(50 - this.world.getDifficulty().a() * 4)); //more inaccuracy
                this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
                this.world.addEntity(entityarrow);
            }
        } else {
            this.spawnMob = true;
            this.spawnExplodingArrow = false;
            ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
            EntityArrow entityarrow = this.b(itemstack, f);

            double d0 = entityliving.locX() - this.locX();
            double d1 = entityliving.e(0.3333333333333333D) - entityarrow.locY();
            double d2 = entityliving.locZ() - this.locZ();

            entityarrow.shoot(d0, d1, d2, 1.6F, 0); /**no inaccuracy for this arrow*/
            this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
            this.world.addEntity(entityarrow);
        }
    }

    @Override
    public void eM() { //"re-registers" the new field "b" since reflection doesn't seem to work
        if (this.world != null && !this.world.isClientSide) {
            this.goalSelector.a((PathfinderGoal)this.b);
            ItemStack itemstack = this.b(ProjectileHelper.a(this, Items.BOW));

            if (itemstack.getItem() == Items.BOW) {
                this.goalSelector.a(4, this.b);
            }
        }
    }

    public double normalGetDistanceSq(Vec3D vec3d1, Vec3D vec3dt) {
        double d0 = vec3dt.getX() - vec3d1.getX(); //skeletons generally still include vertical distance for performance reasons
        double d1 = vec3dt.getY() - vec3d1.getY();
        double d2 = vec3dt.getZ() - vec3d1.getZ();

        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    public double getFollowRange() { /**strays have 22 block detection range (setting attribute doesn't work) (32 after 25 attacks)*/
        return this.attacks < 25 ? 22.0 : 32.0;
    }

    protected CoordsFromHypotenuse coordsFromHypotenuse = new CoordsFromHypotenuse();

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 25 && !this.a25) {
            this.a25 = true;
            this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); /**updates attack range; only happens if/when the mob has a target*/
        }

        if (this.attacks == 60 && !this.a60) { /**after 60 attacks, strays summon 6 vanilla skeletons*/
            this.a60 = true;

            for (int i = 0; i < 6; i++) {
                EntitySkeleton newSkeleton = new EntitySkeleton(EntityTypes.SKELETON, this.getWorld());
                newSkeleton.setPosition(this.locX(), this.locY(), this.locZ());
                newSkeleton.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); //makes sure that it has a bow
                this.getWorld().addEntity(newSkeleton, CreatureSpawnEvent.SpawnReason.DROWNED);
            }
        }

        if (this.ticksLived == 10) { /**strays only have 13.5 health*/
            this.setHealth(13.5f);
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(13.5);
        }

        if (this.ticksLived % 5 == 2) {
            if (this.getGoalTarget() != null) {
                EntityLiving target = this.getGoalTarget();

                if (this.normalGetDistanceSq(this.getPositionVector(), target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) { //deaggro if player out of y-level-included sphere for performance reasons
                    this.setGoalTarget(null);
                }
            }

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
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

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
