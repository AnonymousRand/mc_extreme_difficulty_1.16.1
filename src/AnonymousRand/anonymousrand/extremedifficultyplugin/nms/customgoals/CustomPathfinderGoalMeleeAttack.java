package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;

import java.util.Random;

public class CustomPathfinderGoalMeleeAttack extends PathfinderGoalMeleeAttack {

    protected final EntityCreature goalOwner;
    protected final double speedTowardsTarget;
    protected final boolean longMemory;
    protected PathEntity path;
    protected double targetX;
    protected double targetY;
    protected double targetZ;
    protected int delayCounter;
    protected int i;
    private int attackCooldown;
    protected long k;
    private static final Random random = new Random();

    public CustomPathfinderGoalMeleeAttack(EntityCreature goalOwner, double speedTowardsTarget) {
        this(goalOwner, speedTowardsTarget, 20);
    }

    public CustomPathfinderGoalMeleeAttack(EntityCreature goalOwner, double speedTowardsTarget, int attackCooldown) {
        super(goalOwner, speedTowardsTarget, true);
        this.goalOwner = goalOwner;
        this.speedTowardsTarget = speedTowardsTarget;
        this.attackCooldown = attackCooldown;
        this.longMemory = true; // follows target even if no line of sight anymore; basically allows it to continue attacking without LOS
    }

    @Override
    public boolean a() {
        long i = this.goalOwner.getWorld().getTime();

        if (i - this.k < 20L) { // this line dictates attack cooldown // todo check
            return false;
        } else {
            this.k = i;

            EntityLiving entityLiving = this.goalOwner.getGoalTarget();

            if (entityLiving == null) {
                return false;
            } else if (!entityLiving.isAlive()) {
                return false;
            } else {
                this.path = this.goalOwner.getNavigation().a(entityLiving, 0);
                return this.path != null ? true : this.a(entityLiving) >= NMSUtil.distSq(this.goalOwner, entityLiving, true);
            }
        }
    }

    @Override
    public void c() {
        this.goalOwner.getNavigation().a(this.path, this.speedTowardsTarget);
        this.goalOwner.setAggressive(true);
        this.delayCounter = 0;
        this.i = 0;
    }

    @Override
    public void e() {
        EntityLiving attackTarget = this.goalOwner.getGoalTarget();
        if (attackTarget == null) {
            return;
        }

        this.goalOwner.getControllerLook().a(attackTarget, 30.0F, 30.0F);
        double distanceToSquared = NMSUtil.distSq(this.goalOwner, attackTarget, true);

        this.delayCounter = Math.max(this.delayCounter - 1, 0);
        if ((this.longMemory || true) && this.delayCounter <= 0 && (this.targetX == 0.0D && this.targetY == 0.0D && this.targetZ == 0.0D || NMSUtil.distSq(attackTarget.locX(), attackTarget.locY(), attackTarget.locZ(), this.targetX, this.targetY, this.targetZ, true) >= 1.0D || this.goalOwner.getRandom().nextFloat() < 0.05F)) { /* no longer requires line of sight to continue attacking */
            this.targetX = attackTarget.locX();
            this.targetY = attackTarget.locY();
            this.targetZ = attackTarget.locZ();
            this.delayCounter = 4 + this.goalOwner.getRandom().nextInt(7);
            if (distanceToSquared > 1024.0D) {
                this.delayCounter += 10;
            } else if (distanceToSquared > 256.0D) {
                this.delayCounter += 5;
            }

            if (!this.goalOwner.getNavigation().a(attackTarget, this.speedTowardsTarget)) {
                this.delayCounter += 15;
            }
        }

        this.i = Math.max(this.i - 1, 0);
        this.a(attackTarget, distanceToSquared);
    }

    protected void a(EntityLiving entityLiving, double d0) {
        double d1 = this.a(entityLiving);

        if (d0 <= d1 && this.i <= 0) {
            if (this.goalOwner instanceof ICustomHostile) {
                if (NMSUtil.distSq(this.goalOwner, entityLiving, false) > d1 && random.nextDouble() < 0.996) { /* mobs can successfully hit you occasionally when they are out of range vertically */
                    return;
                }
            }

            this.g();
            this.goalOwner.swingHand(EnumHand.MAIN_HAND);
            this.goalOwner.attackEntity(entityLiving);
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