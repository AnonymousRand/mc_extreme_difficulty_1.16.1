package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class CustomEntityZombieHusk extends EntityZombieHusk implements ICustomMob {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a8, a20;

    public CustomEntityZombieHusk(World world) {
        super(EntityTypes.HUSK, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.a8 = false;
        this.a20 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.575); /** husks move 2.5x faster and always have regen 4 */
        this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 3));
        RemovePathfinderGoals.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() { /** no longer targets iron golems */
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 80, 1, 1, 1, 1, false)); /** custom goal that breaks blocks around the mob periodically */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new NewPathfinderGoalSummonLightningRandomly(this, 1.0)); /** custom goal that spawns lightning randomly */
        this.goalSelector.a(0, new NewPathfinderGoalTeleportTowardsPlayer(this, this.getFollowRange(), 300, 0.004)); /** custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds */
        this.goalSelector.a(1, new CustomEntityZombieHusk.NewPathfinderGoalHuskSandStorm(this)); /** custom goal that does the sandstorm mechanism */
        this.goalSelector.a(2, new CustomPathfinderGoalZombieAttack(this, 1.0D, true)); /** uses the custom melee attack goal that attacks even when line of sight is broken */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(4, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
    }

    public double getSandStormStrength() {
        return this.attacks < 5 ? 0.0 : this.attacks < 12 ? 11.0 : this.attacks < 20 ? 16.0 : 0.6;
    }

    public int getSandStormAttackCooldown() {
        return this.attacks < 5 ? Integer.MAX_VALUE : this.attacks < 12 ? 185 : this.attacks < 20 ? 130 : 6;
    }

    public double getFollowRange() { /** husks have 40 block detection range (setting attribute doesn't work) */
        return 40.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 8 && !this.a8) { /** after 8 attacks, husks get 30 max health and health */
            this.a8 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
        }

        if (this.attacks == 20 && !this.a20) { /** after 20 attacks, husks get regen 5 */
            this.a20 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 4));
        }
    }

    static class NewPathfinderGoalHuskSandStorm extends PathfinderGoal {

        private final CustomEntityZombieHusk husk;
        private final World nmsWorld;
        private final org.bukkit.World bukkitWorld;
        private Vec3D huskPos;
        private Location locTemp;
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
                        this.locTemp = CustomMathHelper.coordsFromHypotenuseAndAngle(this.bukkitWorld, new BlockPosition(this.huskPos.getX(), this.huskPos.getY(), this.huskPos.getZ()), random.nextInt(16), this.husk.getGoalTarget().locY() + (this.husk.attacks < 20 ? 8.0 : 9.0) + i, 361.0);
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

                            this.bukkitWorld.spawnFallingBlock(new Location(this.bukkitWorld, (int)locTemp.getX() + 0.5, (int)locTemp.getY() + 0.5, (int)locTemp.getZ() + 0.5), this.blockData);
                        } else { /** after 20 attacks, sand rains are always anvil rains */
                            this.bukkitWorld.getBlockAt(this.locTemp).setType(org.bukkit.Material.DAMAGED_ANVIL);
                        }
                    }
                }
            }
        }
    }
}