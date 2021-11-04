package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class CustomPathfinderGoalArrowAttack extends PathfinderGoal {

    private final EntityInsentient entity;
    private final IRangedEntity entityButIRangedEntity;
    private EntityLiving attackTarget;
    private int d;
    private final double e;
    private int f;
    private final int g;
    private final int h;
    private final float i;
    private final float j;

    public CustomPathfinderGoalArrowAttack(IRangedEntity irangedentity, double d0, int i, float f) {
        this(irangedentity, d0, i, i, f);
    }

    public CustomPathfinderGoalArrowAttack(IRangedEntity irangedentity, double d0, int i, int j, float f) {
        this.d = -1;
        if (!(irangedentity instanceof EntityLiving)) {
            throw new IllegalArgumentException("ArrowAttackGoal requires Mob implements RangedAttackMob");
        } else {
            this.entityButIRangedEntity = irangedentity;
            this.entity = (EntityInsentient) irangedentity;
            this.e = d0;
            this.g = i;
            this.h = j;
            this.i = f;
            this.j = f * f;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
        }
    }

    @Override
    public boolean a() {
        EntityLiving entityliving = this.entity.getGoalTarget();

        if (entityliving != null && entityliving.isAlive()) {
            this.attackTarget = entityliving;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean b() {
        return this.a() || !this.entity.getNavigation().m();
    }

    @Override
    public void d() {
        this.attackTarget = null;
        this.f = 0;
        this.d = -1;
    }

    @Override
    public void e() {
        double d0 = this.entity.g(this.attackTarget.locX(), this.attackTarget.locY(), this.attackTarget.locZ());
        boolean flag = true; /***breaking line of sight does not stop the mob from attacking*/

        if (flag) {
            ++this.f;
        } else {
            this.f = 0;
        }

        if (d0 <= (double)this.j && this.f >= 5) {
            this.entity.getNavigation().o();
        } else {
            this.entity.getNavigation().a((Entity) this.attackTarget, this.e);
        }

        this.entity.getControllerLook().a(this.attackTarget, 30.0F, 30.0F);
        float f;

        if (--this.d == 0) {
            if (!flag) {
                return;
            }

            f = MathHelper.sqrt(d0) / this.i;
            float f1 = MathHelper.a(f, 0.1F, 1.0F);

            this.entityButIRangedEntity.a(this.attackTarget, f1);
            this.d = MathHelper.d(f * (float) (this.h - this.g) + (float) this.g);
        } else if (this.d < 0) {
            f = MathHelper.sqrt(d0) / this.i;
            this.d = MathHelper.d(f * (float) (this.h - this.g) + (float) this.g);
        }
    }
}
