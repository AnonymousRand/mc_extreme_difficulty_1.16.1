package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityBat;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class CustomPathfinderGoalPassiveMeleeAttack extends PathfinderGoal {

    protected final EntityInsentient entity;
    private final double b;
    private final boolean c;
    private PathEntity d;
    private double e;
    private double f;
    private double g;
    private int h;
    private int i;
    private final int j = 20;
    private long k;

    public CustomPathfinderGoalPassiveMeleeAttack(EntityInsentient entity, double d0, boolean flag) {
        this.entity = entity;
        this.b = d0;
        this.c = flag;
        this.a(EnumSet.of(PathfinderGoal.Type.MOVE, PathfinderGoal.Type.LOOK));
    }

    @Override
    public boolean a() { //should execute; fires constantly
        long i = this.entity.world.getTime();

        if (i - this.k < 20L) {
            return false;
        } else {
            this.k = i;
            EntityLiving entityliving = this.entity.getGoalTarget();

            if (entityliving == null) {
                return false;
            } else if (!entityliving.isAlive()) {
                return false;
            } else {
                this.d = this.entity.getNavigation().a((Entity) entityliving, 0);
                return this.a(entityliving) >= this.entity.g(entityliving.locX(), entityliving.locY(), entityliving.locZ());
            }
        }
    }

    @Override
    public boolean b() { //should continue executing; fires after every attack
        EntityLiving entityliving = this.entity.getGoalTarget();

        return entityliving == null ? false : (!entityliving.isAlive() ? false : (!this.c ? !this.entity.getNavigation().m() : (!this.entity.a(entityliving.getChunkCoordinates()) ? false : !(entityliving instanceof EntityHuman) || !entityliving.isSpectator() && !((EntityHuman) entityliving).isCreative())));
    }

    @Override
    public void c() {
        this.entity.getNavigation().a(this.d, this.b);
        this.entity.setAggressive(true);
        this.h = 0;
        this.i = 0;
    }

    @Override
    public void d() {
        EntityLiving entityliving = this.entity.getGoalTarget();

        this.entity.setAggressive(false);
        this.entity.getNavigation().o();
    }

    @Override
    public void e() { //tick; fires if continue execute or should execute is true
        EntityLiving entityliving = this.entity.getGoalTarget();

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

    protected void a(EntityLiving entityliving, double d0) {
        double d1 = this.a(entityliving);

        if (d0 <= d1 && this.i <= 0) {
            this.g();
            this.entity.swingHand(EnumHand.MAIN_HAND);
            this.entity.attackEntity(entityliving);
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

    protected double a(EntityLiving entityliving) {
        return (double)(this.entity.getWidth() * 4.0F * this.entity.getWidth() * 4.0F + entityliving.getWidth());
    }
}
