package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;

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
    public boolean a() { //copied in because first two attacks are rapidfire otherwise
        long i = this.a.world.getTime();

        if (i - this.k < 20L) {
            return false;
        } else {
            this.k = i;
            EntityLiving entityliving = this.a.getGoalTarget();

            if (entityliving == null) {
                return false;
            } else if (!entityliving.isAlive()) {
                return false;
            } else {
                this.d = this.a.getNavigation().a((Entity) entityliving, 0);
                return this.d != null ? true : this.a(entityliving) >= this.a.g(entityliving.locX(), entityliving.locY(), entityliving.locZ());
            }
        }
    }

    @Override
    public void c() { //copied in because mobs doesn't attack regularly otherwise
        this.a.getNavigation().a(this.d, this.b);
        this.a.setAggressive(true);
        this.h = 0;
        this.i = 0;
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

            if (!this.entity.getNavigation().a((Entity)entityliving, this.b)) {
                this.h += 15;
            }
        }

        this.i = Math.max(this.i - 1, 0);
        this.a(entityliving, d0);
    }

    protected void a(EntityLiving entityliving, double d0) { //these four methods copied in as mobs hit multiple times per attack otherwise
        double d1 = this.a(entityliving);

        if (d0 <= d1 && this.i <= 0) {
            this.g();
            this.a.swingHand(EnumHand.MAIN_HAND);
            this.a.attackEntity(entityliving);
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
}
