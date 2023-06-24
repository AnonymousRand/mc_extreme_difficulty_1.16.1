package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;

public class CustomEntityHoglin extends EntityHoglin implements ICustomMob, IAttackLevelingMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks;
    private boolean a10, a20, a40, a75;

    public CustomEntityHoglin(World world) {
        super(EntityTypes.HOGLIN, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.a10 = false;
        this.a20 = false;
        this.a40 = false;
        this.a75 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7); /** hoglins move 75% faster (125% faster for babies), do 5 damage (8 for babies), and have extra knockback */
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 8.0 : 5.0);
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.0);
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /** custom goal that breaks blocks around the mob periodically except for diamond blocks, emerald blocks, nertherite blocks, and beacons */
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(0, new CustomEntityHoglin.NewPathfinderGoalHoglinBreakRepellentBlocksAround(this, 20, 5, 1, 5, 1, false)); /** custom goal that breaks repellant blocks around the mob periodically */
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D)); /** uses the custom melee attack goal that attacks regardless of the y level */
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void die() {
        super.die();

        if (random.nextDouble() < (this.attacks < 40 ? 0.3 : 1.0)) { /** hoglins have a 30% chance to spawn a zoglin after death (100% chance after 40 attacks) */
            new SpawnEntity(this.getWorld(), new CustomEntityZoglin(this.getWorld()), 1, null, null, this, false, true);
        }
    }

    public double getFollowRange() { /** hoglins have 40 block detection range (setting attribute doesn't work) (64 after 10 attacks) */
        return this.attacks < 10 ? 40.0 : 64.0;
    }

    public int getAttacks() {
        return this.attacks;
    }

    public void incrementAttacks(int increase) {
        this.attacks += increase;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /** after 10 attacks, hoglins get regen 2 */
            this.a10 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, true)); // updates follow range
        }

        if (this.attacks == 20 && !this.a20) { /** after 20 attacks, hoglins get regen 3 */
            this.a20 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        }

        if (this.attacks == 40 && !this.a40) { /** after 40 attacks, hoglins summon a baby hoglin */
            this.a40 = true;
            CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
            newHoglin.a(true);
            new SpawnEntity(this.getWorld(), newHoglin, 1, null, null, this, false, true);
        }

        if (this.attacks == 75 && !this.a75) { /** after 75 attacks, hoglins summon another baby hoglin */
            this.a75 = true;
            CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
            newHoglin.a(true);
            new SpawnEntity(this.getWorld(), newHoglin, 1, null, null, this, false, true);
        }

        if (this.getGoalTarget() != null) {
            Location bukkitLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
            Block bukkitBlock = bukkitLoc.getBlock();

            if (bukkitBlock.getType() == org.bukkit.Material.AIR) { /** hoglins lay down fire trails on itself as long as it is inside an air block */
                bukkitBlock.setType(org.bukkit.Material.FIRE);
            }
        }
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

    static class NewPathfinderGoalHoglinBreakRepellentBlocksAround extends NewPathfinderGoalBreakBlocksAround {

        public NewPathfinderGoalHoglinBreakRepellentBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
            super(entity, delayTimer, radX, radY, radZ, yOffset, removeFluids);
        }

        @Override
        public void e() {
            int X = (int)Math.floor(this.entity.getPositionVector().getX());
            int Y = (int)Math.floor(this.entity.getPositionVector().getY()) + yOffset;
            int Z = (int)Math.floor(this.entity.getPositionVector().getZ());
            org.bukkit.World bukkitWorld = this.entity.getWorld().getWorld();
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
