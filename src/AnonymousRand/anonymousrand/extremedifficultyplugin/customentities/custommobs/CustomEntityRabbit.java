package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityRabbit extends EntityRabbit implements ICommonCustomMethods {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a5, a15, a25, die;

    public CustomEntityRabbit(World world) {
        super(EntityTypes.RABBIT, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.a5 = false;
        this.a15 = false;
        this.a25 = false;
        this.die = false;
        RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones

        this.setRabbitType(99);
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
    }

    @Override
    public void setRabbitType(int i) {
        super.setRabbitType(i);

        if (i == 99) {
            this.goalSelector.a(4, new CustomEntityRabbit.PathfinderGoalKillerRabbitMeleeAttack(this)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityWolf.class, false));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/

            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2)); /**changing attributes don't work on rabbits so killer bunnies have speed 3 and jump boost 1*/
            this.addEffect(new MobEffect(MobEffects.JUMP, Integer.MAX_VALUE, 1));
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.attacks >= 40) { /**after 40 attacks, killer bunnies duplicate when hit and not killed*/
            new SpawnLivingEntity(this.getWorld(), new CustomEntityRabbit(this.getWorld()), 1, null, null, this, false, true);
        }

        return super.damageEntity(damagesource, f);
    }

    public double getFollowRange() { /**killer bunnies have 16 block detection range (setting attribute doesn't work) (28 after 5 attacks, 40 after 15 attacks)*/
        return this.attacks < 5 ? 16.0 : this.attacks < 15 ? 28.0 : 40.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getRabbitType() == 99) {
            if (this.attacks == 5 && !this.a5) { /**after 5 attacks, killer bunnies gain speed 4*/
                this.a5 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 3));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); //updates follow range
            }

            if (this.attacks == 15 && !this.a15) { /**after 15 attacks, killer bunnies gain speed 5*/
                this.a15 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); //updates follow range
            }

            if (this.attacks == 25 && !this.a25) { /**after 25 attacks, killer bunnies gain speed 6 and 10 max health and health*/
                this.a25 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 5));
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(10.0);
                this.setHealth(10.0F);
            }

            if (this.getHealth() <= 0.0 && !this.die) {
                this.die = true;

                if (this.attacks >= 25) { /**after 25 attacks, killer bunnies explode when killed*/
                    this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0F, false, Explosion.Effect.DESTROY);
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

    @Override
    public int bL() { //getMaxFallHeight
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int)(this.getHealth() * 20.0); /**mobs are willing to take 20 times the fall distance (same damage) to reach and do not stop taking falls if it is at less than 33% health*/

            return i + 3;
        }
    }

    static class PathfinderGoalKillerRabbitMeleeAttack extends CustomPathfinderGoalMeleeAttack {

        public PathfinderGoalKillerRabbitMeleeAttack(EntityRabbit entityrabbit) {
            super(entityrabbit, 1.4D, false);
        }

        @Override
        protected double a(EntityLiving entityliving) {
            return (double)(4.0F + entityliving.getWidth());
        }
    }
}
