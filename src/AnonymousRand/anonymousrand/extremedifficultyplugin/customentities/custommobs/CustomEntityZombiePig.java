package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;

public class CustomEntityZombiePig extends EntityPigZombie implements ICommonCustomMethods {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a5, a15, a25, a35;


    public CustomEntityZombiePig(World world) {
        super(EntityTypes.ZOMBIFIED_PIGLIN, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD)); //makes sure that it has a sword
        this.attacks = 0;
        this.a5 = false;
        this.a15 = false;
        this.a25 = false;
        this.a35 = false;
        RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlockLookingAt(this)); /**custom goal that allows the mob to break the block it is looking at every 3 seconds as long as it has a target, it breaks the block that it is looking at up to 40 blocks away*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportToPlayerAdjustY(this, 5.0, random.nextDouble() * 5 + 10.0, 0.0005));  /**custom goal that gives mob a chance every tick to teleport to a spot where its y level difference from its target is reduced if its y level difference is too large*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300, 0.004)); /**custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
        this.goalSelector.a(0, new NewPathfinderGoalUpgradeArmor(this)); /**custom goal that allows this mob to upgrade its armor gradually as part of the attacks system*/
        this.goalSelector.a(1, new CustomPathfinderGoalZombieAttack(this, 1.0D, true)); /**uses the custom melee attack goal that attacks even when line of sight is broken*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**always aggro; uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    public double getFollowRange() { /**zombie piglins have 16 block detection range (setting attribute doesn't work)*/
        return 16.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 5 && !this.a5) { /**after 5 attacks, zombie piglins can aggro other zombie piglins up to 22 blocks away and get regen 2 and 30 max health*/
            this.a5 = true;
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(22.0);
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(30.0);
        }

        if (this.attacks == 15 && !this.a15) { /**after 15 attacks, zombie piglins can aggro other zombie piglins up to 30 blocks away*/
            this.a15 = true;
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(30.0);
        }

        if (this.attacks == 25 && !this.a25) { /**after 25 attacks, zombie piglins can aggro other zombie piglins up to 38 blocks away and get regen 3*/
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(38.0);
        }

        if (this.attacks == 35 && !this.a35) { /**after 35 attacks, zombie piglins can aggro other zombie piglins up to 48 blocks away*/
            this.a35 = true;
            this.getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(48.0);
        }
    }

    @Override
    public void checkDespawn() {
        if (this.getWorld().getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.getWorld().findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);*/
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
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
        double d3 = this.locX() - d0; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d5 = this.locZ() - d2;

        return d3 * d3 + d5 * d5;
    }

    @Override
    public double d(Vec3D vec3d) {
        double d0 = this.locX() - vec3d.x; /**for determining distance to entities, y level does not matter, eg. mob follow range, attacking (can hit player no matter the y level)*/
        double d2 = this.locZ() - vec3d.z;

        return d0 * d0 + d2 * d2;
    }

    @Override
    public int bL() { //getMaxFallHeight
        if (this.getGoalTarget() == null) {
            return 3;
        } else {
            int i = (int)(this.getHealth() * 20.0); /**mobs are willing to take 20 times the fall distance (same damage) to reach and do not stop taking falls if it is at less than 33% health*/

            return i + 3;
        }
    }
}
