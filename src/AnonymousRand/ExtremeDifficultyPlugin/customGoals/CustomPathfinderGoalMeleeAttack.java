package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;

public class CustomPathfinderGoalMeleeAttack extends PathfinderGoalMeleeAttack {

    protected final EntityCreature entity;
    private final double b;
    private final boolean c;
    private PathEntity d;
    private double e;
    private double f;
    private double g;
    private int h;
    private int i;
    private long k;

    public CustomPathfinderGoalMeleeAttack(EntityCreature entitycreature, double speedTowardsTarget, boolean useLongMemory) {
        super(entitycreature, speedTowardsTarget, useLongMemory);
        this.entity = entitycreature;
        this.b = speedTowardsTarget;
        this.c = useLongMemory;
    }

    @Override
    public void e() {
        EntityLiving entityliving = this.entity.getGoalTarget();

        if (entityliving == null) {
            return;
        }

        this.entity.getControllerLook().a(entityliving, 30.0F, 30.0F);
        double d0 = this.entity.g(entityliving.locX(), entityliving.locY(), entityliving.locZ());

        this.h = Math.max(this.h - 1, 0);
        if ((this.c || true) && this.h <= 0 && (this.e == 0.0D && this.f == 0.0D && this.g == 0.0D || entityliving.g(this.e, this.f, this.g) >= 1.0D || this.entity.getRandom().nextFloat() < 0.05F)) { /**no longer requires line of sight to continue attacking*/
            this.e = entityliving.locX();
            this.f = entityliving.locY();
            this.g = entityliving.locZ();
            this.h = 4 + this.entity.getRandom().nextInt(7);
            if (d0 > 1024.0D) {
                this.h += 10;
            } else if (d0 > 256.0D) {
                this.h += 5;
            }

            if (!this.entity.getNavigation().a((Entity) entityliving, this.b)) {
                this.h += 15;
            }
        }

        this.i = Math.max(this.i - 1, 0);
        this.a(entityliving, d0);
    }
}
