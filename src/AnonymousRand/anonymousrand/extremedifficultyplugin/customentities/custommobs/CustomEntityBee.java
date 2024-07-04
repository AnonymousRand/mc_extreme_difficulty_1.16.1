package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

public class CustomEntityBee extends EntityBee implements ICustomHostile, IGoalRemovingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private boolean firstSting;

    public CustomEntityBee(World world) {
        super(EntityTypes.BEE, world);
        initCustomHostile();
        initGoalRemoval();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                                      ICustomHostile                                       //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public void initCustomHostile() {
        /** No longer avoids lava*/
        this.a(PathType.LAVA, 0.0F);
        /** No longer avoids fire */
        this.a(PathType.DAMAGE_FIRE, 0.0F);

        this.firstSting = true;

        this.initAttributes();
    }

    private void initAttributes() {
        /** bees do 1000 damage but only have 5 health */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(1000.0);
        this.setHealth(5.0F);
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(5.0);
    }

    public double getFollowRange() {
        /** Bees have 16 block detection range (setting attribute doesn't work) */
        return 16.0;
    }

    @Override
    public double g(double x, double y, double z) {
        double dist_x = this.locX() - x;
        double dist_z = this.locZ() - z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    @Override
    public double d(Vec3D vec3d) {
        double dist_x = this.locX() - vec3d.x;
        double dist_z = this.locZ() - vec3d.z;

        return dist_x * dist_x + dist_z * dist_z;
    }

    /////////////////////////////////////  IGoalRemovingMob  //////////////////////////////////////

    public void initGoalRemoval() {
        this.vanillaTargetSelector = super.targetSelector;
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    //                               Overridden vanilla functions                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        /** Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this));
        /** Takes buffs from bats and piglins etc. */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this));
        /** Doesn't need line of sight to continue attacking, and occasionally ignores y-level range limitations */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.399999976158142D));
        /** Doesn't need line of sight to find targets and start attacking */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class));
    }

    @Override
    public void tick() {
        super.tick();

        if (this.hasStung()) {
            /** Doesn't die from stinging */
            this.setHasStung(false);

            /** 50% chance to duplicate after the first sting */
            if (this.firstSting) {
                this.firstSting = false;

                if (random.nextDouble() < 0.5) {
                    new SpawnEntity(this.getWorld(), new CustomEntityBee(this.getWorld()), 1, null, null, this, false, true);
                }
            }
        }
    }
}
