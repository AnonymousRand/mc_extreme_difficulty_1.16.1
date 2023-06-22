package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityShulkerBullet;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.EnumSet;

public class CustomEntityShulker extends EntityShulker implements ICustomMob, IAttackLevelingMob {

    private int attacks;
    private boolean a10, a21, a40;
    private static NewPathfinderGoalSpawnBlocksEntitiesOnMob goal1;
    private static NewPathfinderGoalSpawnBlocksEntitiesOnMob goal2;

    public CustomEntityShulker(World world) {
        super(EntityTypes.SHULKER, world);
        this.attacks = 0;
        this.a10 = false;
        this.a21 = false;
        this.a40 = false;
        CustomEntityAreaEffectCloud newAEC = new CustomEntityAreaEffectCloud(this.getWorld(), 2.0F, 20, 0);
        newAEC.addEffect(new MobEffect(MobEffects.LEVITATION, 160));
        goal1 = new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, newAEC, 19);
        newAEC = new CustomEntityAreaEffectCloud(this.getWorld(), 3.0F,20, 0);
        newAEC.addEffect(new MobEffect(MobEffects.LEVITATION, 6, 49));
        goal2 = new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, newAEC, 19);
        this.getAttributeInstance(GenericAttributes.ARMOR).setValue(12.0); /** shulkers have 12 armor points, and even more when it is closed */
    }

    @Override
    public void initPathfinder() {
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 8.0F));
        this.goalSelector.a(4, new CustomEntityShulker.PathfinderGoalShulkerBulletAttack());
        this.goalSelector.a(7, new CustomEntityShulker.PathfinderGoalShulkerPeek());
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, (new CustomPathfinderGoalHurtByTarget(this, new Class[0])).a(CustomEntityShulker.class));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        boolean b = super.damageEntity(damagesource, f);

        if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5D && this.random.nextInt(2) == 0) { /** shulkers now have a 50% chance to teleport instead of 25% chance when damaged below half health */
            this.eL();
        }

        return b;
    }

    @Override
    public void die() {
        super.die();
        new RunnableShulkerDeathBullets(this).run(); /** shulkers explode into 9 shulker bullets that seek out the player when killed */
    }

    public double getFollowRange() { /** shulkers have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void incrementAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /** after 10 attacks, shulkers get 15 armor points and regen 2 */
            this.a10 = true;
            this.getAttributeInstance(GenericAttributes.ARMOR).setValue(15.0);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 21 && !this.a21) { /** after 21 attacks, shulkers summon area effect clouds wherever it is that give levitation 1 for 8 seconds */
            this.a21 = true;
            this.goalSelector.a(1, goal1);
        }

        if (this.attacks == 40 && !this.a40) { /** after 40 attacks, shulkers get 20 armor points and their area effect clouds' radius is increased to 3 and they are changed to give levitation 50 for 6 ticks */
            this.a40 = true;
            this.getAttributeInstance(GenericAttributes.ARMOR).setValue(20.0);
            this.goalSelector.a(goal1);
            this.goalSelector.a(1, goal2);
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

    class PathfinderGoalShulkerBulletAttack extends PathfinderGoal {

        private int b;

        public PathfinderGoalShulkerBulletAttack() {
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }

        @Override
        public boolean a() {
            EntityLiving entityLiving = CustomEntityShulker.this.getGoalTarget();

            return entityLiving != null && entityLiving.isAlive() ? CustomEntityShulker.this.world.getDifficulty() != EnumDifficulty.PEACEFUL : false;
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
            if (CustomEntityShulker.this.world.getDifficulty() != EnumDifficulty.PEACEFUL) {
                --this.b;
                EntityLiving entityLiving = CustomEntityShulker.this.getGoalTarget();

                CustomEntityShulker.this.getControllerLook().a(entityLiving, 180.0F, 180.0F);
                double d0 = CustomEntityShulker.this.h((Entity)entityLiving);

                if (d0 < 400.0D) {
                    if (this.b <= 0) {
                        CustomEntityShulker.this.attacks++;

                        this.b = 10 + CustomEntityShulker.this.random.nextInt(10) * 10; /** shulker takes on average 10 less ticks to shoot */

                        for (int i = 0; i < (CustomEntityShulker.this.attacks < 35 ? 1 : CustomEntityShulker.this.random.nextDouble() < 0.5 ? 1 : 2); i++) { /** after 35 attacks, shulkers have a 50% to 2 bullets at a time */
                            CustomEntityShulker.this.world.addEntity(new CustomEntityShulkerBullet(CustomEntityShulker.this.world, CustomEntityShulker.this, entityLiving, CustomEntityShulker.this.eM().n()));
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
