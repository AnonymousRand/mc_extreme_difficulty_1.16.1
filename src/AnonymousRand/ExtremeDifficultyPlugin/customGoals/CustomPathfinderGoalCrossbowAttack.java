package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class CustomPathfinderGoalCrossbowAttack<T extends EntityMonster & IRangedEntity & ICrossbow> extends PathfinderGoalCrossbowAttack { //todo: apply to all applicable mobs

    private final T b;
    private CustomPathfinderGoalCrossbowAttack.State c;
    private final double d;
    private final float e;
    private int f;
    private int g;
    private int h;

    public CustomPathfinderGoalCrossbowAttack(T t0, double d0, float f) {
        super(t0, d0, f);
        this.c = CustomPathfinderGoalCrossbowAttack.State.UNCHARGED;
        this.b = t0;
        this.d = d0;
        this.e = f * f;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    private boolean j() {
        return this.c == CustomPathfinderGoalCrossbowAttack.State.UNCHARGED;
    }

    @Override
    public void e() {
        EntityLiving entityliving = this.b.getGoalTarget();

        if (entityliving != null) {
            boolean flag = true; //breaking line of sight does not stop the mob from attacking
            boolean flag1 = this.f > 0;

            if (flag != flag1) {
                this.f = 0;
            }

            if (flag) {
                ++this.f;
            } else {
                --this.f;
            }

            double d0 = this.b.h((Entity) entityliving);
            boolean flag2 = (d0 > (double) this.e || this.f < 5) && this.g == 0;

            if (flag2) {
                --this.h;
                if (this.h <= 0) {
                    this.b.getNavigation().a((Entity) entityliving, this.j() ? this.d : this.d * 0.5D);
                    this.h = CustomPathfinderGoalCrossbowAttack.a.a(this.b.getRandom());
                }
            } else {
                this.h = 0;
                this.b.getNavigation().o();
            }

            this.b.getControllerLook().a(entityliving, 30.0F, 30.0F);
            if (this.c == CustomPathfinderGoalCrossbowAttack.State.UNCHARGED) {
                if (!flag2) {
                    this.b.c(ProjectileHelper.a(this.b, Items.CROSSBOW));
                    this.c = CustomPathfinderGoalCrossbowAttack.State.CHARGING;
                    ((ICrossbow) this.b).b(true);
                }
            } else if (this.c == CustomPathfinderGoalCrossbowAttack.State.CHARGING) {
                if (!this.b.isHandRaised()) {
                    this.c = CustomPathfinderGoalCrossbowAttack.State.UNCHARGED;
                }

                int i = this.b.dZ();
                ItemStack itemstack = this.b.getActiveItem();

                if (i >= ItemCrossbow.g(itemstack)) {
                    this.b.releaseActiveItem();
                    this.c = CustomPathfinderGoalCrossbowAttack.State.CHARGED;
                    this.g = 20 + this.b.getRandom().nextInt(20);
                    ((ICrossbow) this.b).b(false);
                }
            } else if (this.c == CustomPathfinderGoalCrossbowAttack.State.CHARGED) {
                --this.g;
                if (this.g == 0) {
                    this.c = CustomPathfinderGoalCrossbowAttack.State.READY_TO_ATTACK;
                }
            } else if (this.c == CustomPathfinderGoalCrossbowAttack.State.READY_TO_ATTACK && flag) {
                ((IRangedEntity) this.b).a(entityliving, 1.0F);
                ItemStack itemstack1 = this.b.b(ProjectileHelper.a(this.b, Items.CROSSBOW));

                ItemCrossbow.a(itemstack1, false);
                this.c = CustomPathfinderGoalCrossbowAttack.State.UNCHARGED;
            }

        }
    }

    static enum State {

        UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK;

        private State() {}
    }
}
