package AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.misc.CustomEntityTNTPrimed;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CustomEntityZoglin extends EntityZoglin {

    public int attacks;

    public CustomEntityZoglin(World world) {
        super(EntityTypes.ZOGLIN, world);
        this.a(PathType.LAVA, 0.0F); //no longer avoids lava
        this.attacks = 0;
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobweb(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(1, new CustomPathfinderGoalMeleeAttack(this, 1.0D, true)); /**custom melee attack goal continues attacking even when line of sight is broken*/
        this.goalSelector.a(2, new NewPathfinderGoalBreakBlocksAround(this, 40, 1, 1, 1, 1, false)); /**custom goal that breaks blocks around the mob periodically*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    public double getFollowRange() { /**zoglins have 64 block detection range (setting attribute doesn't work)*/
        return 64.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived % 6 == 0 && this.getGoalTarget() != null) {
            Location thisLoc = new Location(this.getWorld().getWorld(), this.locX(), this.locY(), this.locZ());

            if (thisLoc.getBlock().getType() == org.bukkit.Material.AIR) { /**zoglins create a path of power 1 tnt on itself as long as it is inside an air block*/
                CustomEntityTNTPrimed newTNT = new CustomEntityTNTPrimed(this.getWorld(), 35, 1.0f);
                newTNT.setPosition(this.locX(), this.locY(), this.locZ());
                this.getWorld().addEntity(newTNT);
            }
        }

        if (this.ticksLived == 10) { /**zoglins move 75% faster (125% faster for babies) and do 2 damage (4 for babies)*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(this.isBaby() ? 0.9 : 0.7);
            this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(this.isBaby() ? 4.0 : 2.0);
        }

        if (this.ticksLived % 5 == 2) {
            if (this.getLastDamager() != null) {
                EntityLiving target = this.getLastDamager();

                if (!(target instanceof EntityPlayer)) { /**mobs only target players (in case mob damage listener doesn't register)*/
                    this.setLastDamager(null);
                }
            }
        }
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman entityhuman = this.world.findNearbyPlayer(this, -1.0D);

            if (entityhuman != null) {
                double d0 = Math.pow(entityhuman.getPositionVector().getX() - this.getPositionVector().getX(), 2) + Math.pow(entityhuman.getPositionVector().getZ() - this.getPositionVector().getZ(), 2); /**mobs only despawn along horizontal axes; if you are at y level 256 mobs will still spawn below you at y64 and prevent sleepingdouble d0 = entityhuman.h(this);*/
                int i = this.getEntityType().e().f();
                int j = i * i;

                if (d0 > (double)j && this.isTypeNotPersistent(d0)) {
                    this.die();
                }

                int k = this.getEntityType().e().g() + 8; /**random despawn distance increased to 40 blocks*/
                int l = k * k;

                if (this.ticksFarFromPlayer > 600 && this.random.nextInt(800) == 0 && d0 > (double)l && this.isTypeNotPersistent(d0)) {
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
}
