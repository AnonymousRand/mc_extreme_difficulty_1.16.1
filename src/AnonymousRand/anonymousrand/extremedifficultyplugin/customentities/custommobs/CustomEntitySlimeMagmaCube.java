package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalBreakBlocksAround;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalSlimeMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableConstantlySpawnBlocksEntities;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntitySlimeMagmaCube extends EntityMagmaCube implements ICustomMob, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a15, a40, deathExplosion;

    public CustomEntitySlimeMagmaCube(World world) {
        super(EntityTypes.MAGMA_CUBE, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.attacks = 0;
        this.a15 = false;
        this.a40 = false;
        this.deathExplosion = false;
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    public CustomEntitySlimeMagmaCube(World world, int size) {
        this(world);
        this.setSize(size, true);
    }

    @Override
    protected void initPathfinder() { /** no longer targets iron golems */
        super.initPathfinder();
        this.goalSelector.a(0, new CustomEntitySlimeMagmaCube.PathfinderGoalMagmaCubeFireAndLava(this)); /** custom goal that allows magma cube to summon fire, magma cubes and/or lava on it depending on attack count */
        this.goalSelector.a(1, new NewPathfinderGoalSlimeMeleeAttack(this, 1.0)); /** uses the custom goal that atstacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); for some reason the magma cubes run away after a while without the extra parameters */
    }

    @Override
    public void setSize(int i, boolean flag) { /** toned down stats a bit to account for potential size 16 magma cubes */
        super.setSize(i, flag);
        this.getAttributeInstance(GenericAttributes.ARMOR).setValue(0.0); /** magma cubes don't have armor */
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(1.0 + ((Math.log10(i) / Math.log10(2.15)) * ((Math.log10(i) + 0.6) / (Math.log10(1.4))))); // approx: 6.58 health for size 2, 15.9 health for size 4, 28.94 health for size 8, 45.72 health for size 16
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(Math.ceil(0.375F + 0.025F * (float)i)); // 0.425 for 2, 0.475 for 4, 0.575 for 8, 0.775 for 16
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(Math.floor(((0.4 + Math.log10(i)) / (Math.log10(2))) * Math.pow(1.03, i))); // 2 for 2, 3 for 4, 5 for 8, 8 for 16
        if (flag) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    protected void j(EntityLiving entityLiving) {} /** magma cubes use the NewPathfinderGoalSlimeMeleeAttack instead of this attack function */

    protected int eK() { /** magma cubes jump faster */
        return random.nextInt(3) + 6;
    }

    public double getFollowRange() { /** magma cubes have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getHealth() <= 0.0 && this.attacks >= 30 && !this.deathExplosion) { /** after 30 attacks, magma cubes explode when killed */
            this.deathExplosion = true;
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), (float)(Math.log10(this.getSize()) / Math.log10(2.0)) / 2.0F, true, Explosion.Effect.DESTROY);
        }

        if (this.attacks == 15 && !this.a15) { /** after 15 attacks, magma cubes increase in size by 2 unless it is already at the largest possible size or is going to exceed it */
            this.a15 = true;

            if (this.getSize() < 15) {
                this.setSize(this.getSize() + 2, true);
            }
        }

        if (this.attacks == 40 && !this.a40) { /** after 40 attacks, magma cubes increase in size by 3 unless it is already at the largest possible size or is going to exceed it */
            this.a40 = true;

            if (this.getSize() < 14) {
                this.setSize(this.getSize() + 3, true);
            }
        }

        if (this.ticksLived == 5) {
            if (this.getSize() > 3) {
                this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 10, this.getSize() / 4 + 1, this.getSize() / 4 + 1, this.getSize() / 4 + 1, this.getSize() / 4 + 1, false)); /** custom goal that breaks blocks around the mob periodically except for diamond blocks, emerald blocks, nertherite blocks, and beacons */
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityHuman.h(this); */
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /** random despawn distance increased to 40 blocks */
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
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
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }

    static class PathfinderGoalMagmaCubeFireAndLava extends PathfinderGoal {

        private final CustomEntitySlimeMagmaCube cube;
        private int firstLand;

        public PathfinderGoalMagmaCubeFireAndLava(CustomEntitySlimeMagmaCube cube) {
            this.cube = cube;
            this.firstLand = 0;
        }

        @Override
        public boolean a() {
            return this.cube.getGoalTarget() != null;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.cube.isOnGround()) { /** magma cube spawns fire on it while on the ground and magma blocks below it */
                int diameter = (int)(Math.floor(this.cube.getBoundingBox().maxX - this.cube.getBoundingBox().minX) + 2);

                if (this.firstLand <= 0) { // to reduce lag, this can only happen every 10 ticks
                    this.firstLand = 10;
                    new RunnableConstantlySpawnBlocksEntities(this.cube, org.bukkit.Material.MAGMA_BLOCK, null, diameter / 2, 0, diameter / 2, -1.0, true).run();
                    return;
                } else if (this.firstLand == 10) { // delay it by 1 tick so the magma blocks have time to place before the fire needs to go underneath
                    new RunnableConstantlySpawnBlocksEntities(this.cube, org.bukkit.Material.FIRE, null, diameter / 2, 0, diameter / 2, 0.0, false).run();
                    return;
                }
            }

            this.firstLand--;
        }
    }
}
