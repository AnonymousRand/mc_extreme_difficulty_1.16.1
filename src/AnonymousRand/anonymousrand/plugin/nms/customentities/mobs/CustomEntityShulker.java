package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles.CustomEntityShulkerBullet;
import AnonymousRand.anonymousrand.plugin.nms.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.plugin.util.NmsUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class CustomEntityShulker extends EntityShulker implements ICustomHostile, IAttackLevelingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private int attacks;
    private boolean a10, a21, a40;

    public CustomEntityShulker(World world) {
        super(EntityTypes.SHULKER, world);
        this.attacks = 0;
        this.a10 = false;
        this.a21 = false;
        this.a40 = false;
        this.getAttributeInstance(GenericAttributes.ARMOR).setValue(12.0); /* shulkers have 12 armor points, and even more when it is closed */
    }

    @Override
    public void initPathfinder() {
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(4, new CustomEntityShulker.PathfinderGoalShulkerBulletAttack());
        this.goalSelector.a(7, new CustomEntityShulker.PathfinderGoalShulkerPeek());
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this, CustomEntityShulker.class));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean damageSuccess = super.damageEntity(damageSource, damageAmount);

        if (damageSuccess && this.getHealth() < this.getMaxHealth() * 0.5 && this.random.nextInt(2) == 0) { /* shulkers now have a 50% chance to teleport instead of 25% chance when damaged below half health */
            this.eL();
        }

        return damageSuccess;
    }

    @Override
    public void die() {
        super.die();
        new RunnableShulkerDeathBullets(this).run(); /* shulkers explode into 9 shulker bullets that seek out the player when killed */
    }

    public double getDetectionRange() { /* shulkers have 40 block detection range */
        return 40.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height,
                   mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer =
                        Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSq = forceDespawnDist * forceDespawnDist;

                if (distSqToNearestPlayer > (double) forceDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSq = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600
                        && random.nextInt(800) == 0
                        && distSqToNearestPlayer > (double) randomDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
                    this.ticksFarFromPlayer = 0;
                }
            }
        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThreshs() {
//        return this.attackLevelingController.getAttacksThreshs();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /* after 10 attacks, shulkers get 15 armor points and regen 2 */
            this.a10 = true;
            this.getAttributeInstance(GenericAttributes.ARMOR).setValue(15.0);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 21 && !this.a21) { /* after 21 attacks, shulkers summon area effect clouds wherever it is that give levitation 1 for 8 seconds */
            this.a21 = true;
            CustomEntityAreaEffectCloud areaEffectCloud = new CustomEntityAreaEffectCloud(this.world, 2.0F, 20, 0);
            areaEffectCloud.addEffect(new MobEffect(MobEffects.LEVITATION, 160));
            this.goalSelector.a(1, new CustomPathfinderGoalSpawnBlocksEntitiesOnMob(this, areaEffectCloud, 19));
        }

        if (this.attacks == 40 && !this.a40) { /* after 40 attacks, shulkers get 20 armor points and their area effect clouds' radius is increased to 3 and they are changed to give levitation 50 for 6 ticks */
            this.a40 = true;
            this.getAttributeInstance(GenericAttributes.ARMOR).setValue(20.0);
            CustomEntityAreaEffectCloud areaEffectCloud = new CustomEntityAreaEffectCloud(this.world, 3.0F,20, 0);
            areaEffectCloud.addEffect(new MobEffect(MobEffects.LEVITATION, 6, 49));
            for (PathfinderGoal goal : VanillaPathfinderGoalsRemove.getGoals(this.goalSelector.d().collect(Collectors.toSet()), CustomPathfinderGoalSpawnBlocksEntitiesOnMob.class)) {
                ((CustomPathfinderGoalSpawnBlocksEntitiesOnMob) goal).changeSpawnedEntity(areaEffectCloud);
            }
        }
    }

    class PathfinderGoalShulkerBulletAttack extends PathfinderGoal {

        private int b;

        public PathfinderGoalShulkerBulletAttack() {
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving attackTarget = CustomEntityShulker.this.getGoalTarget();

            return attackTarget != null && attackTarget.isAlive() ? CustomEntityShulker.this.getWorld().getDifficulty() != EnumDifficulty.PEACEFUL : false;
        }

        @Override
        public void c() {
            this.b = 20;
            CustomEntityShulker.this.a(100);
        }

        @Override
        public void d() {
            CustomEntityShulker.this.a(0);
        }

        @Override
        public void e() {
            if (CustomEntityShulker.this.getWorld().getDifficulty() != EnumDifficulty.PEACEFUL) {
                --this.b;
                EntityLiving target = CustomEntityShulker.this.getGoalTarget();

                CustomEntityShulker.this.getControllerLook().a(target, 180.0F, 180.0F);
                double distSqToTarget = NmsUtil.distSq(CustomEntityShulker.this, target, true);

                if (distSqToTarget < 400.0) { // todo getfollowrange?
                    if (this.b <= 0) {
                        CustomEntityShulker.this.attacks++;

                        this.b = 10 + CustomEntityShulker.this.random.nextInt(10) * 10; /* shulker takes on average 10 less ticks to shoot */

                        for (int i = 0; i < (CustomEntityShulker.this.attacks < 35 ? 1 : CustomEntityShulker.this.random.nextDouble() < 0.5 ? 1 : 2); i++) { /* after 35 attacks, shulkers have a 50% to 2 bullets at a time */
                            CustomEntityShulker.this.getWorld().addEntity(new CustomEntityShulkerBullet(CustomEntityShulker.this.getWorld(), CustomEntityShulker.this, target, CustomEntityShulker.this.eM().n()));
                        }

                        CustomEntityShulker.this.playSound(SoundEffects.ENTITY_SHULKER_SHOOT, 2.0F, (CustomEntityShulker.this.random.nextFloat() - CustomEntityShulker.this.random.nextFloat()) * 0.2F + 1.0F);
                    }
                } else {
                    CustomEntityShulker.this.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                }

                super.e();
            }
        }
    }

    class PathfinderGoalShulkerPeek extends PathfinderGoal {

        private int b;

        private PathfinderGoalShulkerPeek() {}

        @Override
        public boolean a() {
            return CustomEntityShulker.this.getGoalTarget() == null && CustomEntityShulker.this.random.nextInt(40) == 0;
        }

        @Override
        public boolean b() {
            return CustomEntityShulker.this.getGoalTarget() == null && this.b > 0;
        }

        @Override
        public void c() {
            this.b = 20 * (1 + CustomEntityShulker.this.random.nextInt(3));
            CustomEntityShulker.this.a(30);
        }

        @Override
        public void d() {
            if (CustomEntityShulker.this.getGoalTarget() == null) {
                CustomEntityShulker.this.a(0);
            }
        }

        @Override
        public void e() {
            --this.b;
        }
    }

    static class RunnableShulkerDeathBullets extends BukkitRunnable {

        private final CustomEntityShulker shulker;
        private final World nmsWorld;
        private final Vec3D pos;

        public RunnableShulkerDeathBullets(CustomEntityShulker shulker) {
            this.shulker = shulker;
            this.nmsWorld = shulker.getWorld();
            this.pos = shulker.getPositionVector();
        }

        @Override
        public void run() {
            if (this.shulker.getGoalTarget() != null) {
                EntityLiving target = this.shulker.getGoalTarget();

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        this.nmsWorld.addEntity(new CustomEntityShulkerBullet(this.nmsWorld, this.pos.getX() + x, this.pos.getY(), this.pos.getZ() + z, this.shulker, target));
                    }
                }
            }
        }
    }
}
