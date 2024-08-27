package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtMelee;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class CustomEntityHoglin extends EntityHoglin implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    private static final boolean IGNORE_LOS = false;
    private static final boolean IGNORE_Y = false;

    private AttackLevelingController attackLevelingController = null;
    public CustomEntityHoglin(World world) {
        super(EntityTypes.HOGLIN, world);
        this.initCustom();
        this.initAttackLevelingMob();
        this.initGoalRemovingMob();
    }

    protected void initCustom() {
        this.initAttributes();

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    protected void initAttributes() {
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7); /* hoglins move 75% faster (125% faster for babies), do 5 damage (8 for babies), and have extra knockback */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 8.0 : 5.0);
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() { /* hoglins have 40 block detection range (64 after 10 attacks) */
        return (this.attackLevelingController == null || this.getAttacks() < 10) ? 40.0 : 64.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height,
                   mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer =
                        Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
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

                if (this.ticksFarFromPlayer > 600
                        && random.nextInt(800) == 0
                        && distSqToNearestPlayer > (double) randomDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
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

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(10, 20, 40, 75);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] attackThreshs = this.getAttacksThreshs();
        int[] metThreshs = this.attackLevelingController.increaseAttacksAndCheckThreshs(increase);

        for (int metThresh : metThreshs) {
            if (metThresh == attackThreshs[0]) {
                /* After 10 attacks, hoglins get regen 2 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
                this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); // update follow range
            } else if (metThresh == attackThreshs[1]) {
                /* After 20 attacks, hoglins get regen 3 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            } else if (metThresh == attackThreshs[2]) {
                /* After 40 attacks, hoglins summon a baby hoglin */
                CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.world);
                newHoglin.a(true);
                new SpawnEntity(this.world, newHoglin, 1, null, null, this, false, true);
            } else if (metThresh == attackThreshs[3]) {
                /* After 75 attacks, hoglins summon another baby hoglin */
                CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.world);
                newHoglin.a(true);
                new SpawnEntity(this.world, newHoglin, 1, null, null, this, false, true);
            }
        }
    }

    public int[] getAttacksThreshs() {
        return this.attackLevelingController.getAttacksThreshs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IGoalRemovingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void initGoalRemovingMob() {
        // remove vanilla HurtByTarget and NearestAttackableTarget goals to replace them with custom ones
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this);
    }

    public PathfinderGoalSelector getVanillaGoalSelector() {
        return super.goalSelector;
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return super.targetSelector;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden Vanilla Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new CustomPathfinderGoalHoglinBreakRepellentBlocksAround(this, 20, 5, 1, 5, 1, false)); /* custom goal that breaks repellant blocks around the mob periodically */
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMelee<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(1, new CustomPathfinderGoalAttackMvmtMelee<>(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public void die() {
        super.die();

        if (this.attackLevelingController != null && random.nextDouble() < (this.getAttacks() < 40 ? 0.3 : 1.0)) { /* hoglins have a 30% chance to spawn a zoglin after death (100% chance after 40 attacks) */
            new SpawnEntity(this.world, new CustomEntityZoglin(this.world), 1, null, null, this, false, true);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (this.getGoalTarget() != null) {
            Location bukkitLoc = new Location(this.world.getWorld(), this.locX(), this.locY(), this.locZ());
            Block bukkitBlock = bukkitLoc.getBlock();

            if (bukkitBlock.getType() == org.bukkit.Material.AIR) { /* hoglins lay down fire trails on itself as long as it is inside an air block */
                bukkitBlock.setType(org.bukkit.Material.FIRE);
            }
        }
    }

    static class CustomPathfinderGoalHoglinBreakRepellentBlocksAround extends CustomPathfinderGoalBreakBlocksAround {

        public CustomPathfinderGoalHoglinBreakRepellentBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int offsetY, boolean removeFluids) {
            super(entity, delayTimer, radX, radY, radZ, offsetY, removeFluids);
        }

        @Override
        public void e() {
            int X = (int) Math.floor(this.goalOwner.getPositionVector().getX());
            int Y = (int) Math.floor(this.goalOwner.getPositionVector().getY()) + offsetY;
            int Z = (int) Math.floor(this.goalOwner.getPositionVector().getZ());
            org.bukkit.World bukkitWorld = this.goalOwner.getWorld().getWorld();
            Block bukkitBlock;
            org.bukkit.Material bukkitMaterial;

            for (int x = -this.radX; x <= this.radX; x++) {
                for (int y = -this.radY; y <= this.radY; y++) {
                    for (int z = -this.radZ; z <= this.radZ; z++) {
                        int x1 = X + x, y1 = Y + y, z1 = Z + z;
                        bukkitBlock = bukkitWorld.getBlockAt(x1, y1, z1);
                        bukkitMaterial = bukkitBlock.getType();

                        if (bukkitMaterial == org.bukkit.Material.WARPED_FUNGUS || bukkitMaterial == org.bukkit.Material.POTTED_WARPED_FUNGUS || bukkitMaterial == org.bukkit.Material.NETHER_PORTAL || bukkitMaterial == org.bukkit.Material.RESPAWN_ANCHOR) {
                            bukkitBlock.setType(org.bukkit.Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
