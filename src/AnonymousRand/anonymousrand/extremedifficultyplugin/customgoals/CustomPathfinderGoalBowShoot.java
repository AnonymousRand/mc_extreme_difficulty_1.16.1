package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class CustomPathfinderGoalBowShoot<T extends EntityMonster & IRangedEntity> extends PathfinderGoalBowShoot {

    private final T a;
    private final double b;
    private int c;
    private final float d;
    private int e = -1;
    private int f;
    private boolean g;
    private boolean h;
    private int i = -1;

    public CustomPathfinderGoalBowShoot(T t0, double d0, int attackSpeed, float maxAttackDistance) {
        super(t0, d0, attackSpeed, maxAttackDistance);
        this.a = t0;
        this.b = d0;
        this.c = attackSpeed;
        this.d = maxAttackDistance * maxAttackDistance;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public void e() {
        EntityLiving entityliving = this.a.getGoalTarget();

        if (entityliving != null) {
            double d0 = this.a.g(entityliving.locX(), entityliving.locY(), entityliving.locZ());
            boolean flag = true; /**breaking line of sight does not stop the mob from attacking*/
            boolean flag1 = this.f > 0;

            if (flag != flag1) {
                this.f = 0;
            }

            if (flag) {
                ++this.f;
            } else {
                --this.f;
            }

            if (d0 <= (double)this.d && this.f >= 20) {
                this.a.getNavigation().o();
                ++this.i;
            } else {
                this.a.getNavigation().a((Entity) entityliving, this.b);
                this.i = -1;
            }

            if (this.i >= 20) {
                if ((double)this.a.getRandom().nextFloat() < 0.3D) {
                    this.g = !this.g;
                }

                if ((double)this.a.getRandom().nextFloat() < 0.3D) {
                    this.h = !this.h;
                }

                this.i = 0;
            }

            if (this.i > -1) {
                if (d0 > (double)(this.d * 0.75F)) {
                    this.h = false;
                } else if (d0 < (double)(this.d * 0.25F)) {
                    this.h = true;
                }

                this.a.getControllerMove().a(this.h ? -0.5F : 0.5F, this.g ? 0.5F : -0.5F);
                this.a.a((Entity) entityliving, 30.0F, 30.0F);
            } else {
                this.a.getControllerLook().a(entityliving, 30.0F, 30.0F);
            }

            if (this.a.isHandRaised()) {
                if (!flag && this.f < -60) {
                    this.a.clearActiveItem();
                } else if (flag) {
                    int i = this.a.dZ();

                    if (i >= 20) {
                        this.a.clearActiveItem();
                        ((IRangedEntity) this.a).a(entityliving, ItemBow.a(i));
                        this.e = this.c;
                    }
                }
            } else if (--this.e <= 0 && this.f >= -60) {
                this.a.c(ProjectileHelper.a(this.a, Items.BOW));
            }

        }
    }
}
