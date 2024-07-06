package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrows;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;

public class CustomEntityPillager extends EntityPillager implements ICustomHostile {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks, attackNum;

    public CustomEntityPillager(World world) {
        super(EntityTypes.PILLAGER, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids lava and fire */
        this.a(PathType.LAVA, 0.0F);
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.CROSSBOW)); // makes sure that it has a crossbow
        Arrays.fill(this.dropChanceArmor, 0.0f); /* pillagers can't drop any armor that it wears */
        this.attacks = 0;
        this.attackNum = 0;
        this.setHealth(15.0F); /* pillagers only have 15 health */
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(15.0);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones

        if (random.nextDouble() < 0.25) { /* pillagers have a 25% chance to spawn double and a 25% chance to spawn as an illusioner instead */
            new SpawnEntity(this.getWorld(), new CustomEntityPillager(this.getWorld()), 1, null, null, this, false, true);
        } else if (random.nextDouble() < 0.5) {
            new SpawnEntity(this.getWorld(), new CustomEntityIllusioner(this.getWorld()), 1, null, null, this, true, true);
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new NewPathfinderGoalUpgradeArmor(this)); /* custom goal that allows this mob to upgrade its armor gradually as part of the attacks system */
        this.goalSelector.a(2, new CustomPathfinderGoalRangedCrossbowAttack<>(this, 1.0, 4, 24.0F)); /* shoots every 4 ticks; uses the custom goal that attacks regardless of the y-level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Doesn't take into account y-level or line of sight to aggro a target */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class));
    }

    @Override
    public void a(EntityLiving entityLiving, float f) { // shoot()
        if (++this.attackNum % 12 == 0) { // attacks only count every ~2 seconds, or 12 shots
            this.attacks++;
        }

        new RunnableMobShootArrows(this, entityLiving, 1, this.attackNum % 18 == 0 ? 6 : 1, 3.0, random.nextDouble() < (this.attackNum % 24 == 0 ? 1.0 : 0.075) ? 1 : 0, false, true).run(); /* shoots a knockback arrow every 18th attack; 7.5% of arrows shot are piercing 1 (100% for knockback arrow); arrows do not lose y-level */
    }

    public double getFollowRange() { /* pillagers have 24 block detection range */
        return 24.0;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at y=256, mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer = Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSq = forceDespawnDist * forceDespawnDist;

                if (distSqToNearestPlayer > (double) forceDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSq = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && distSqToNearestPlayer
                        > (double) randomDespawnDistSq && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double x, double y, double z) {
        double distX = this.locX() - x;
        double distZ = this.locZ() - z;

        return distX * distX + distZ * distZ;
    }

    @Override
    public double d(Vec3D vec3d) {
        double distX = this.locX() - vec3d.x;
        double distZ = this.locZ() - vec3d.z;

        return distX * distX + distZ * distZ;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }
}
