package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalMeleeAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalMoveFasterInCobweb;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.NewPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;

import java.util.Arrays;

public class CustomEntityVindicator extends EntityVindicator implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores y-level and line of sight for initially finding a player target and maintaining it as the target,
       as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a10, a25, a35;

    public CustomEntityVindicator(World world) {
        super(EntityTypes.VINDICATOR, world);
        this.vanillaTargetSelector = super.targetSelector;
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.IRON_AXE)); // makes sure that it has an axe
        this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.IRON_AXE)); /* vindicators have axes everywhere visible */
        this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.IRON_AXE));
        Arrays.fill(this.dropChanceHand, 0.0f);  /* vindicators can't drop the items they are holding/items in armor slots */
        Arrays.fill(this.dropChanceArmor, 0.0f);
        this.attacks = 0;
        this.a10 = false;
        this.a25 = false;
        this.a35 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.665); /* vindicators move 90% faster and have strength 1 */
        this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 0));
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0, IGNORE_LOS)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the closest option */
    }

    public double getDetectionRange() { /* vindicators have 24 block detection range (32 after 10 attacks) */
        return this.attacks < 10 ? 24.0 : 32.0;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height, mobs will spawn below you and prevent sleeping */
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
    public int bL() {
        return Integer.MAX_VALUE;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThresholds() {
//        return this.attackLevelingController.getAttacksThresholds();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /* after 10 attacks, vindicators get diamond axes which increases damage by 0-1 */
            this.a10 = true;
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.DIAMOND_AXE));
            this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.DIAMOND_AXE));
            this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.DIAMOND_AXE));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, IGNORE_LOS, IGNORE_Y)); // update follow range
        }

        if (this.attacks == 25 && !this.a25) { /* after 25 attacks, vindicators get strength 2 */
            this.a25 = true;
            this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 1));
        }

        if (this.attacks == 35 && !this.a35) { /* after 35 attacks, vindicators get netherite axes and strength 3 */
            this.a35 = true;
            this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
            this.setSlot(EnumItemSlot.OFFHAND, new ItemStack(Items.NETHERITE_AXE));
            this.setSlot(EnumItemSlot.HEAD, new ItemStack(Items.NETHERITE_AXE));
            this.addEffect(new MobEffect(MobEffects.INCREASE_DAMAGE, Integer.MAX_VALUE, 2));
        }
    }

    @Override
    public PathfinderGoalSelector getVanillaTargetSelector() {
        return this.vanillaTargetSelector;
    }
    
    public boolean getIgnoreLOS() {
        return IGNORE_LOS;
    }
    
    public boolean getIgnoreY() {
        return IGNORE_Y;
    }
}
