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
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.CROSSBOW)); // makes sure that it has a crossbow
        Arrays.fill(this.dropChanceArmor, 0.0f); /** pillagers can't drop any armor that it wears */
        this.attacks = 0;
        this.attackNum = 0;
        this.setHealth(15.0F); /** pillagers only have 15 health */
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(15.0);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones

        if (random.nextDouble() < 0.25) { /** pillagers have a 25% chance to spawn double and a 25% chance to spawn as an illusioner instead */
            new SpawnEntity(this.getWorld(), new CustomEntityPillager(this.getWorld()), 1, null, null, this, false, true);
        } else if (random.nextDouble() < 0.5) {
            new SpawnEntity(this.getWorld(), new CustomEntityIllusioner(this.getWorld()), 1, null, null, this, true, true);
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalUpgradeArmor(this)); /** custom goal that allows this mob to upgrade its armor gradually as part of the attacks system */
        this.goalSelector.a(2, new CustomPathfinderGoalRangedCrossbowAttack<>(this, 1.0, 4, 24.0F)); /** shoots every 4 ticks; uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class));
    }

    @Override
    public void a(EntityLiving entityLiving, float f) { // shoot()
        if (++this.attackNum % 12 == 0) { // attacks only count every ~2 seconds, or 12 shots
            this.attacks++;
        }

        new RunnableMobShootArrows(this, entityLiving, 1, this.attackNum % 18 == 0 ? 6 : 1, 3.0, random.nextDouble() < (this.attackNum % 24 == 0 ? 1.0 : 0.075) ? 1 : 0, false, true).run(); /** shoots a knockback arrow every 18th attack; 7.5% of arrows shot are piercing 1 (100% for knockback arrow); arrows do not lose y level */
    }

    public double getFollowRange() { /** pillagers have 24 block detection range (setting attribute doesn't work) */
        return 24.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityHuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityHuman != null) {
                double d0 = Math.pow(entityHuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityHuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /** mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityHuman.h(this); */
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /** random despawn distance increased to 40 blocks */
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
                    this.die();
                } else if (d0 < (double)l) {
                    this.ticksFarFromPlayer = 0;
                }
            }

        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public double g(double d0, double d1, double d2) {
        double d3 = this.locX() - d0; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /** for determining distance to entities, y level does not matter, e.g. mob follow range, attacking (can hit player no matter the y level) */
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE; /** mobs are willing to take any fall to reach the player as they don't take fall damage */
    }
}
