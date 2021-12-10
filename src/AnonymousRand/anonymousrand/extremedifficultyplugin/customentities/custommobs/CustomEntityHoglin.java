package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;

public class CustomEntityHoglin extends EntityHoglin implements ICustomMob {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a8, a15, a25, a40;

    public CustomEntityHoglin(World world) {
        super(EntityTypes.HOGLIN, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.a8 = false;
        this.a15 = false;
        this.a25 = false;
        this.a40 = false;
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7); /**hoglins move 75% faster (125% faster for babies), do 5 damage (10 for babies), and have extra knockback*/
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 10.0 : 5.0);
        this.getAttributeInstance(GenericAttributes.ATTACK_KNOCKBACK).setValue(3.0);
        RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new CustomEntityHoglin.NewPathfinderGoalHoglinBreakRepellentBlocksAround(this, 20, 5, 1, 5, 1, false)); /**custom goal that breaks repellant blocks around the mob periodically*/
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /**uses the custom melee attack goal that attacks even when line of sight is broken*/
        this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void die() {
        super.die();

        if (random.nextDouble() < (this.attacks < 40 ? 0.3 : 1.0)) { /**hoglins have a 30% chance to spawn a zoglin after death (100% chance after 40 attacks)*/
            new SpawnEntity(this.getWorld(), new CustomEntityZoglin(this.getWorld()), 1, null, null, this, false, true);
        }
    }

    public double getFollowRange() { /**hoglins have 40 block detection range (setting attribute doesn't work) (64 after 10 attacks)*/
        return this.attacks < 10 ? 40.0 : 64.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 8 && !this.a8) { /**after 8 attacks, hoglins get regen 2*/
            this.a8 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            this.targetSelector.a(0, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, true)); //updates follow range
        }

        if (this.attacks == 15 && !this.a15) { /**after 15 attacks, hoglins get regen 3*/
            this.a15 = true;
            this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
        }

        if (this.attacks == 25 && !this.a25) { /**after 25 attacks, hoglins summon a baby hoglin*/
            this.a25 = true;
            CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
            newHoglin.a(true);
            new SpawnEntity(this.getWorld(), newHoglin, 1, null, null, this, false, true);
        }

        if (this.attacks == 40 && !this.a40) { /**after 40 attacks, hoglins summon 2 baby hoglins*/
            this.a40 = true;
            CustomEntityHoglin newHoglin = new CustomEntityHoglin(this.getWorld());
            newHoglin.a(true);
            new SpawnEntity(this.getWorld(), newHoglin, 2, null, null, this, false, true);
        }

        Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());
        if (thisLoc.getBlock().getType() == org.bukkit.Material.AIR) { /**hoglins lay down fire trails on itself as long as it is inside an air block*/
            thisLoc.getBlock().setType(org.bukkit.Material.FIRE);
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

    static class NewPathfinderGoalHoglinBreakRepellentBlocksAround extends NewPathfinderGoalBreakBlocksAround {

        public NewPathfinderGoalHoglinBreakRepellentBlocksAround(EntityInsentient entity, int delayTimer, int radX, int radY, int radZ, int yOffset, boolean removeFluids) {
            super(entity, delayTimer, radX, radY, radZ, yOffset, removeFluids);
        }

        @Override
        public void e() {
            int X = (int)Math.floor(this.entity.getPositionVector().getX());
            int Y = (int)Math.floor(this.entity.getPositionVector().getY()) + yOffset;
            int Z = (int)Math.floor(this.entity.getPositionVector().getZ());
            org.bukkit.World world = this.entity.getWorld().getWorld();

            for (int x = -this.radX; x <= this.radX; x++) {
                for (int y = -this.radY; y <= this.radY; y++) {
                    for (int z = -this.radZ; z <= this.radZ; z++) {
                        int x1 = X + x, y1 = Y + y, z1 = Z + z;

                        if (world.getBlockAt(x1, y1, z1).getType() == org.bukkit.Material.WARPED_FUNGUS || world.getBlockAt(x1, y1, z1).getType() == org.bukkit.Material.POTTED_WARPED_FUNGUS || world.getBlockAt(x1, y1, z1).getType() == org.bukkit.Material.NETHER_PORTAL || world.getBlockAt(x1, y1, z1).getType() == org.bukkit.Material.RESPAWN_ANCHOR) {
                            world.getBlockAt(x1, y1, z1).setType(org.bukkit.Material.AIR);
                        }
                    }
                }
            }
        }
    }
}
