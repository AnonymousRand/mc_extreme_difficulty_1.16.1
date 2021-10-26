package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntityRabbit extends EntityRabbit {

    public int attacks;
    private boolean a15, a45, a70;

    public CustomEntityRabbit(World world) {
        super(EntityTypes.RABBIT, world);
        this.attacks = 0;
        this.a15 = false;
        this.a45 = false;
        this.a70 = false;
    }

    @Override
    public void setRabbitType(int i) {
        super.setRabbitType(i);

        if (i == 99) {
            this.goalSelector.a(4, new CustomPathfinderGoalKillerRabbitMeleeAttack(this)); /**uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal); this custom goal also allows the spider to continue attacking regardless of light level*/
            this.targetSelector.a(0, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement); this custom goal also allows the spider to continue attacking regardless of light level*/
            this.targetSelector.a(1, new PathfinderGoalNearestAttackableTarget<>(this, EntityWolf.class, false));
        }
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0 && this.attacks >= 100) { /**after 100 attacks, killer bunnies duplicate when hit and not killed*/
            CustomEntityRabbit newRabbit = new CustomEntityRabbit(this.getWorld());
            newRabbit.setPositionRotation(this.locX(), this.locY(), this.locZ(), this.yaw, this.pitch);
            newRabbit.setRabbitType(99);
            this.getWorld().addEntity(newRabbit, CreatureSpawnEvent.SpawnReason.NATURAL);
        }

        return super.damageEntity(damagesource, f);
    }

    public double getFollowRange() {
        return this.attacks < 15 ? 16.0 : this.attacks < 45 ? 24.0 : 32.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getRabbitType() == 99) {
            if (this.attacks == 15 && !this.a15) { /**after 15 attacks, killer bunnies gain speed 3*/
                this.a15 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2));
            }

            if (this.attacks == 45 && !this.a45) { /**after 45 attacks, killer bunnies gain speed 4*/
                this.a45 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 3));
            }

            if (this.attacks == 70 && !this.a70) { /**after 70 attacks, killer bunnies gain speed 5 and 9 max health*/
                this.a70 = true;
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 4));
                this.setHealth(9.0f);
                ((LivingEntity)this.getBukkitEntity()).setMaxHealth(9.0);
            }

            if (this.ticksLived == 2400) { /**rabbits die after 2 minutes, generating a power 1 explosion*/
                this.die();
                this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), 1.0f, false, Explosion.Effect.DESTROY); /**explodes with power 1 after 20 seconds or when killed*/
            }

            if (this.ticksLived % 40 == 10) { /**killer bunnies have 16 block detection range (setting attribute doesn't work) (24 after 15 attacks, 32 after 45 attacks)*/
                EntityPlayer player = this.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), this, this.locX(), this.locY(), this.locZ(), this.getBoundingBox().grow(this.getFollowRange(), 128.0, this.getFollowRange())); //get closest player within bounding box
                if (player != null && !player.isInvulnerable() && this.getGoalTarget() == null) {
                    this.setGoalTarget(player);
                }

                if (this.getGoalTarget() != null) {
                    EntityLiving target = this.getGoalTarget();

                    if (target.isInvulnerable() || this.d(target.getPositionVector()) > Math.pow(this.getFollowRange(), 2)) {
                        this.setGoalTarget(null);
                    }
                }
            }

            Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
            if (thisLoc.getBlock().getType() == org.bukkit.Material.COBWEB) { /**non-player mobs gain Speed 11 while in a cobweb (approx original speed)*/
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 2, 10));
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
