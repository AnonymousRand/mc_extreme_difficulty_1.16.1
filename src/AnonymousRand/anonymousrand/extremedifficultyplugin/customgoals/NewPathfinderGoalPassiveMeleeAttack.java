package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;
import java.util.Random;

public class NewPathfinderGoalPassiveMeleeAttack extends PathfinderGoal {

    protected final EntityInsentient entity;
    protected final double b;
    protected final boolean c;
    protected PathEntity d;
    protected double e;
    protected double f;
    protected double g;
    protected int h;
    protected int i;
    protected final int j = 20;
    protected long k;
    protected static final Random random = new Random();

    public NewPathfinderGoalPassiveMeleeAttack(EntityInsentient entity, double d0) {
        this.entity = entity;
        this.b = d0;
        this.c = true; // follows target even if no line of sight anymore; basically allows it to continue attacking without LOS
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public boolean a() { // canUse()/shouldExecute(); fires constantly
        long i = this.entity.world.getTime();

        if (i - this.k < 20L) {
            return false;
        } else {
            this.k = i;
            EntityLiving entityLiving = this.entity.getGoalTarget();

            if (entityLiving == null) {
                return false;
            } else if (!entityLiving.isAlive()) {
                return false;
            } else {
                this.d = this.entity.getNavigation().a(entityLiving, 0);
                return this.a(entityLiving) >= this.entity.g(entityLiving.locX(), entityLiving.locY(), entityLiving.locZ());
            }
        }
    }

    @Override
    public boolean b() { // canContinueToUse()/shouldContinueExecuting(); fires after every attack
        EntityLiving entityLiving = this.entity.getGoalTarget();
        return entityLiving == null ? false : (!entityLiving.isAlive() ? false : (!this.c ? !this.entity.getNavigation().m() : (!this.entity.a(entityLiving.getChunkCoordinates()) ? false : !(entityLiving instanceof EntityHuman) || !entityLiving.isSpectator() && !((EntityHuman) entityLiving).isCreative())));
    }

    @Override
    public void c() { // start()
        this.entity.getNavigation().a(this.d, this.b);
        this.entity.setAggressive(true);
        this.h = 0;
        this.i = 0;
    }

    @Override
    public void d() { // stop()
        this.entity.setAggressive(false);
        this.entity.getNavigation().o();
    }

    @Override
    public void e() { // tick(); fires if shouldExecute() or shouldContinueExecuting() is true
        EntityLiving attackTarget = this.entity.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        this.entity.getControllerLook().a(attackTarget, 30.0F, 30.0F);
        double distanceToSquared = this.entity.d(attackTarget.getPositionVector());

        this.h = Math.max(this.h - 1, 0);
        if (this.c && this.h <= 0 && (this.e == 0.0D && this.f == 0.0D && this.g == 0.0D || attackTarget.g(this.e, this.f, this.g) >= 1.0D || this.entity.getRandom().nextFloat() < 0.05F)) { /* no longer requires line of sight to continue attacking */
            this.e = attackTarget.locX();
            this.f = attackTarget.locY();
            this.g = attackTarget.locZ();
            this.h = 4 + this.entity.getRandom().nextInt(7);
            if (distanceToSquared > 1024.0D) {
                this.h += 10;
            } else if (distanceToSquared > 256.0D) {
                this.h += 5;
            }

            if (!this.entity.getNavigation().a(attackTarget, this.b)) {
                this.h += 15;
            }
        }

        this.i = Math.max(this.i - 1, 0);
        this.a(attackTarget, distanceToSquared);
    }

    protected void a(EntityLiving entityLiving, double d0) {
        double d1 = this.a(entityLiving);

        if (d0 <= d1 && this.i <= 0) {
            if (this.entity instanceof ICustomHostile) {
                if (((ICustomHostile)this.entity).get3DDistSq(this.entity.getPositionVector(), entityLiving.getPositionVector()) > d1 && random.nextDouble() < 0.996) { /* mobs can only successfully hit you occasionally when they are very distant vertically */ // todo test frequency
                    return;
                }
            }

            this.g();
            this.entity.swingHand(EnumHand.MAIN_HAND);
            this.entity.attackEntity(entityLiving);
        }
    }

    protected void g() {
        this.i = 20;
    }

    protected boolean h() {
        return this.i <= 0;
    }

    protected int j() {
        return this.i;
    }

    protected int k() {
        return 20;
    }

    protected double a(EntityLiving entityLiving) {
        return this.entity.getWidth() * 4.0F * this.entity.getWidth() * 4.0F + entityLiving.getWidth();
    }
}
