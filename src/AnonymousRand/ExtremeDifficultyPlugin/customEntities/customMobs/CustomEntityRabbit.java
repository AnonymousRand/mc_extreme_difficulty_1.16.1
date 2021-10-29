package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.NewPathfinderGoalCobweb;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntityRabbit extends EntityRabbit {

    public int attacks;
    private boolean a5, a15, a25, die;

    public CustomEntityRabbit(World world) {
        super(EntityTypes.RABBIT, world);
        this.attacks = 0;
        this.a5 = false;
        this.a15 = false;
        this.a25 = false;
        this.die = false;
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
    }

    @Override
    public void setRabbitType(int i) {
        super.setRabbitType(i);

        if (i == 99) {
            this.goalSelector.a(4, new CustomPathfinderGoalKillerRabbitMeleeAttack(this)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
            this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityWolf.class, false));
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.attacks >= 40) { /**after 40 attacks, killer bunnies duplicate when hit and not killed*/
            CustomEntityRabbit newRabbit = new CustomEntityRabbit(this.getWorld());
            newRabbit.setPosition(this.locX(), this.locY(), this.locZ());
            newRabbit.setRabbitType(99);
            this.getWorld().addEntity(newRabbit, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        return super.damageEntity(damagesource, f);
    }

    public double getFollowRange() { /**killer bunnies have 16 block detection range (setting attribute doesn't work) (28 after 5 attacks, 40 after 15 attacks and already detected a target)*/
        return this.attacks < 5 ? 16.0 : this.attacks < 15 ? 28.0 : 40.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getRabbitType() == 99) {
            if (this.attacks == 5 && !this.a5) { /**after 5 attacks, killer bunnies gain speed 3*/
                this.a5 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); /**updates attack range; only happens if/when the mob has a target*/
            }

            if (this.attacks == 15 && !this.a15) { /**after 15 attacks, killer bunnies gain speed 4*/
                this.a15 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 3));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); /**updates attack range; only happens if/when the mob has a target*/
            }

            if (this.attacks == 25 && !this.a25) { /**after 25 attacks, killer bunnies gain speed 5 and 10 max health and health*/
                this.a25 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(10.0);
                this.setHealth(10.0f);
            }

            if (this.getHealth() <= 0.0 && !this.die) {
                this.die = true;

                if (this.attacks >= 25) { /**after 25 attacks, killer bunnies explode when killed*/
                    this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0f, false, Explosion.Effect.DESTROY);
                }
            }

            if (this.ticksLived % 5 == 2) {
                if (this.getGoalTarget() != null) {
                    EntityLiving target = this.getGoalTarget();

                    if (!(target instanceof EntityPlayer) || target.isInvulnerable() || this.d(target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) { /**mobs only target players (in case mob damage listener doesn't register)*/
                        this.setGoalTarget(null);
                    }
                }
            }
        }
    }

    static class CustomPathfinderGoalKillerRabbitMeleeAttack extends CustomPathfinderGoalMeleeAttack {

        public CustomPathfinderGoalKillerRabbitMeleeAttack(EntityRabbit entityrabbit) {
            super(entityrabbit, 1.4D, false);
        }

        @Override
        protected double a(EntityLiving entityliving) {
            return (double)(4.0F + entityliving.getWidth());
        }
    }
}
