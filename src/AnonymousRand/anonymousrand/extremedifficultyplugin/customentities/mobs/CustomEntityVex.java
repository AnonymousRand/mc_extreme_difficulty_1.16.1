package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityVex extends EntityVex implements ICustomHostile, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a20, a30, a45, a60;

    public CustomEntityVex(World world) {
        super(EntityTypes.VEX, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.moveController = new CustomEntityVex.ControllerMoveVex(this);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a20 = false;
        this.a30 = false;
        this.a45 = false;
        this.a60 = false;
        this.getBukkitEntity().setCustomName("Am I worse than phantoms?");
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(2.0); /* vexes only have 12 health and do 2 damage */
        this.setHealth(12.0F);
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(12.0);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target */
    }

    @Override
    public boolean damageEntity(DamageSource damageSource, float damageAmount) {
        boolean tookDamage = super.damageEntity(damageSource, damageAmount);
        if (tookDamage && damageSource.getEntity() instanceof EntityPlayer && !this.killed && random.nextDouble() < 0.75) { /* vexes have a 75% chance to duplicate when hit by player and not killed */
            new SpawnEntity(this.getWorld(), new CustomEntityVex(this.getWorld()), 1, null, null, this, false, false);
        }

        return tookDamage;
    }

    public double getDetectionRange() { /* vexes have 32 bock detection range */
        return 32.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at y=256, mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
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

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSqToNearestPlayer
                        > (double) randomDespawnDistSq && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double x, double y, double z) {
        double distX = this.locX() - x;
        double distZ = this.locZ() - z;
        return distX * distX + distZ * distZ;
    }

    @Override
    public double d(Vec3D vec3d) {
        double distX = this.locX() - vec3d.x;
        double distZ = this.locZ() - vec3d.z;
        return distX * distX + distZ * distZ;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThresholds() {
//        return this.attackLevelingController.getAttacksThresholds();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 20 && !this.a20) { /* after 20 attacks, vexes summon a bat */
            this.a20 = true;
            new SpawnEntity(this.getWorld(), new CustomEntityBat(this.getWorld()), 1, null, null, this, false, false);
        }

        if (this.attacks == 30 && !this.a30) { /* after 30 attacks, vexes heal itself and all other vexes within 16 blocks horizontally to full health */
            this.a30 = true;
            this.setHealth(11.0F);

            this.getWorld().getEntities(this, this.getBoundingBox().grow(16.0, 128.0, 16.0), entity -> entity instanceof CustomEntityVex).forEach(entity -> ((CustomEntityVex)entity).setHealth(11.0F));
        }

        if (attacks == 45 && !this.a45) { /* after 45 attacks, vexes heal itself and all other evokers and vexes within 32 blocks horizontally to full health */
            this.a45 = true;
            this.setHealth(11.0F);

            this.getWorld().getEntities(this, this.getBoundingBox().grow(32.0, 128.0, 32.0), entity -> (entity instanceof CustomEntityVex || entity instanceof EntityEvoker)).forEach(entity -> ((EntityLiving) entity).setHealth(11.0F));
        }

        if (this.attacks >= 60 && !this.a60) { /* after 60 attacks, vexes teleport ASAP to their goal target, explode and die */
            if (this.getGoalTarget() != null) {
                this.a60 = true;
                this.setPosition(this.getGoalTarget().locX(), this.getGoalTarget().locY(), this.getGoalTarget().locZ());
                this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.5F, false, Explosion.Effect.NONE);
                this.die();
            }
        }
    }

    class ControllerMoveVex extends ControllerMove {

        public ControllerMoveVex(CustomEntityVex vex) {
            super(vex);
        }

        @Override
        public void a() {
            if (this.h == ControllerMove.Operation.MOVE_TO) {
                Vec3D vec3d = new Vec3D(this.b - CustomEntityVex.this.locX(), this.c - CustomEntityVex.this.locY(), this.d - CustomEntityVex.this.locZ());
                double d0 = vec3d.f();

                if (d0 < CustomEntityVex.this.getBoundingBox().a()) {
                    this.h = ControllerMove.Operation.WAIT;
                    CustomEntityVex.this.setMot(CustomEntityVex.this.getMot().a(0.85 * (CustomEntityVex.this.attacks < 12 ? 1.0 : 1.3))); /* vexes move about 85% faster (110.5% faster after 12 attacks) */
                } else {
                    CustomEntityVex.this.setMot(CustomEntityVex.this.getMot().e(vec3d.a(this.e * 0.085 * (CustomEntityVex.this.attacks < 12 ? 1.0 : 1.3) / d0))); /* vexes move about 85% faster (110.5% faster after 12 attacks) */
                    if (CustomEntityVex.this.getGoalTarget() == null) {
                        Vec3D vec3d1 = CustomEntityVex.this.getMot();

                        CustomEntityVex.this.yaw = -((float) MathHelper.d(vec3d1.x, vec3d1.z)) * 57.295776F;
                    } else {
                        double d1 = CustomEntityVex.this.getGoalTarget().locX() - CustomEntityVex.this.locX();
                        double d2 = CustomEntityVex.this.getGoalTarget().locZ() - CustomEntityVex.this.locZ();

                        CustomEntityVex.this.yaw = -((float) MathHelper.d(d1, d2)) * 57.295776F;
                    }

                    CustomEntityVex.this.aH = CustomEntityVex.this.yaw;
                }
            }
        }
    }
}
