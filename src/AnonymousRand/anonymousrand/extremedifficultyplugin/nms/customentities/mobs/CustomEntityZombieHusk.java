package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IGoalRemovingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.VanillaPathfinderGoalsRemove;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack.CustomPathfinderGoalAttackMelee;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtMelee;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class CustomEntityZombieHusk extends EntityZombieHusk implements ICustomHostile, IAttackLevelingMob, IGoalRemovingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private int attacks;
    private boolean a8, a20;

    public CustomEntityZombieHusk(World world) {
        super(EntityTypes.HUSK, world);
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
        this.attacks = 0;
        this.a8 = false;
        this.a20 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.575); /* husks move 2.5x faster and always have regen 4 */
        this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 3));
        VanillaPathfinderGoalsRemove.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() { /* No longer targets iron golems and villagers */
        super.initPathfinder();
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 80, 1, 1, 1, 1, false)); /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this)); /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this)); /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalSummonLightningRandomly(this, 1.0)); /* Spawns lightning randomly */
        this.goalSelector.a(0, new CustomPathfinderGoalTeleportNearTarget(this, this.getDetectionRange(), 300.0, 0.0015)); /* Occasionally teleports to a spot near its target */
        this.goalSelector.a(1, new CustomEntityZombieHusk.NewPathfinderGoalHuskSandStorm(this)); /* custom goal that does the sandstorm mechanism */
        this.goalSelector.a(2, new CustomPathfinderGoalAttackMelee<>(this)); /* uses the custom melee attack goal that attacks regardless of the y-level */
        this.goalSelector.a(2, new CustomPathfinderGoalAttackMvmtMelee<>(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));                                /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */ // todo does the listener actually not work sometimes? also if this is no longer needed, don't remove old goal in igoalremovingmob
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, false, false, EntityTurtle.bv));
    }

    public double getSandStormStrength() {
        return this.attacks < 5 ? 0.0 : this.attacks < 12 ? 11.0 : this.attacks < 20 ? 16.0 : 0.6;
    }

    public int getSandStormAttackCooldown() {
        return this.attacks < 5 ? Integer.MAX_VALUE : this.attacks < 12 ? 185 : this.attacks < 20 ? 130 : 6;
    }

    public double getDetectionRange() { /* husks have 40 block detection range */
        return 40.0;
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

    public int getAttacks() {
        return this.attacks;
    }

    public void increaseAttacks(int increase) {
        this.attacks += increase;
    }

//    public int[] getAttacksThreshs() {
//        return this.attackLevelingController.getAttacksThreshs();
//    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 8 && !this.a8) { /* after 8 attacks, husks get 30 max health and health */
            this.a8 = true;
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
        }

        if (this.attacks == 20 && !this.a20) { /* after 20 attacks, husks get regen 5 */
            this.a20 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 4));
        }
    }

    @Override
    public PathfinderGoalSelector getVanillaGoalSelector() {
        return super.goalSelector;
    }

    public PathfinderGoalSelector getVanillaTargetSelector() {
        return super.targetSelector;
    }

    static class NewPathfinderGoalHuskSandStorm extends PathfinderGoal {

        private final CustomEntityZombieHusk husk;
        private final World nmsWorld;
        private final org.bukkit.World bukkitWorld;
        private Vec3D huskPos;
        private Location bukkitLocTemp;
        private BlockData blockData;
        private double randomDouble;
        private static final Random random = new Random();

        public NewPathfinderGoalHuskSandStorm(CustomEntityZombieHusk husk) {
            this.husk = husk;
            this.nmsWorld = husk.getWorld();
            this.bukkitWorld = this.nmsWorld.getWorld();
        }

        @Override
        public boolean a() {
            return this.husk.getGoalTarget() != null && this.husk.attacks >= 5;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.husk.ticksLived % this.husk.getSandStormAttackCooldown() == 0 && this.husk.getGoalTarget() != null) {
                this.huskPos = this.husk.getPositionVector();

                for (int i = 0; i < Math.ceil(this.husk.getSandStormStrength() / 5.0); i++) {
                    for (int j = 0; j < this.husk.getSandStormStrength() * 8.0; j++) {
                        this.bukkitLocTemp = CustomMathHelper.coordsFromHypotAndAngle(this.bukkitWorld, new BlockPosition(this.huskPos.getX(), this.huskPos.getY(), this.huskPos.getZ()), random.nextInt(16), this.husk.getGoalTarget().locY() + (this.husk.attacks < 20 ? 8.0 : 9.0) + i, 361.0);
                        this.randomDouble = random.nextDouble();

                        if (this.husk.attacks < 20) {
                            if (this.randomDouble < 0.5) {
                                this.blockData = org.bukkit.Material.SAND.createBlockData();
                            } else if (this.randomDouble < 0.85) {
                                this.blockData = org.bukkit.Material.STONE.createBlockData();
                            } else if (this.randomDouble < 0.95) {
                                this.blockData = org.bukkit.Material.INFESTED_STONE.createBlockData();
                            } else {
                                this.blockData = org.bukkit.Material.ANVIL.createBlockData();
                            }

                            this.bukkitWorld.spawnFallingBlock(new Location(this.bukkitWorld, (int) bukkitLocTemp.getX() + 0.5, (int) bukkitLocTemp.getY() + 0.5, (int) bukkitLocTemp.getZ() + 0.5), this.blockData);
                        } else { /* after 20 attacks, sand rains are always anvil rains */
                            this.bukkitWorld.getBlockAt(this.bukkitLocTemp).setType(org.bukkit.Material.DAMAGED_ANVIL);
                        }
                    }
                }
            }
        }
    }
}
