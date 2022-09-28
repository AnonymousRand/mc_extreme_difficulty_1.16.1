package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.ICustomMob;
import net.minecraft.server.v1_16_R1.*;

import java.util.Random;

public class CustomPathfinderGoalMeleeAttack extends PathfinderGoalMeleeAttack {

    protected final EntityCreature entity;
    protected final double b;
    protected final boolean c;
    protected PathEntity d;
    protected double e;
    protected double f;
    protected double g;
    protected int h;
    protected int i;
    protected long k;
    private static final Random random = new Random();

    public CustomPathfinderGoalMeleeAttack(EntityCreature entityCreature, double speedTowardsTarget, boolean useLongMemory) {
        super(entityCreature, speedTowardsTarget, useLongMemory);
        this.entity = entityCreature;
        this.b = speedTowardsTarget;
        this.c = useLongMemory;
    }

    @Override
    public boolean a() { // copied in because first two attacks are rapidfire otherwise
        long i = this.entity.world.getTime();

        if (i - this.k < 20L) { // this line dictates attack cooldown
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
                return this.d != null ? true : this.a(entityLiving) >= this.entity.g(entityLiving.locX(), entityLiving.locY(), entityLiving.locZ());
            }
        }
    }

    @Override
    public void c() { // copied in because mobs doesn't attack regularly otherwise
        this.entity.getNavigation().a(this.d, this.b);
        this.entity.setAggressive(true);
        this.h = 0;
        this.i = 0;
    }

    @Override
    public void e() {
        EntityLiving entityLiving = this.entity.getGoalTarget();

        if (entityLiving == null) {
            return;
        }

        this.entity.getControllerLook().a(entityLiving, 30.0F, 30.0F);
        double d0 = this.entity.g(entityLiving.locX(), entityLiving.locY(), entityLiving.locZ());

        this.h = Math.max(this.h - 1, 0);
        if ((this.c || true) && this.h <= 0 && (this.e == 0.0D && this.f == 0.0D && this.g == 0.0D || entityLiving.g(this.e, this.f, this.g) >= 1.0D || this.entity.getRandom().nextFloat() < 0.05F)) { /** no longer requires line of sight to continue attacking */
            this.e = entityLiving.locX();
            this.f = entityLiving.locY();
            this.g = entityLiving.locZ();
            this.h = 4 + this.entity.getRandom().nextInt(7);
            if (d0 > 1024.0D) {
                this.h += 10;
            } else if (d0 > 256.0D) {
                this.h += 5;
            }

            if (!this.entity.getNavigation().a(entityLiving, this.b)) {
                this.h += 15;
            }
        }

        this.i = Math.max(this.i - 1, 0);
        this.a(entityLiving, d0);
    }

    protected void a(EntityLiving entityLiving, double d0) {
        double d1 = this.a(entityLiving);

        if (d0 <= d1 && this.i <= 0) {
            if (this.entity instanceof ICustomMob) {
                if (((ICustomMob)this.entity).getNormalDistanceSq(this.entity.getPositionVector(), entityLiving.getPositionVector()) > d1 && random.nextDouble() < 0.996) { /** mobs can only successfully hit you occasionally when they are very distant vertically */
                    return;
                }
            }

            this.g();
            this.entity.swingHand(EnumHand.MAIN_HAND);
            this.entity.attackEntity(entityLiving);
        }
    }

    protected void g() {  // these three methods copied in as mobs hit multiple times per attack otherwise
        this.i = 20;
    }

    protected boolean h() {
        return this.i <= 0;
    }

    protected int j() {
        return this.i;
    }
}
