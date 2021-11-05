package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class CustomEntitySilverfish extends EntitySilverfish {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks;
    private boolean a15, a90;

    public CustomEntitySilverfish(World world) {
        super(EntityTypes.SILVERFISH, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.attacks = 0;
        this.a15 = false;
        this.a90 = false;
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 100, 1, 0, 1, 0, true)); /**custom goal that breaks blocks around the mob periodically*/
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new NewPathfinderGoalTeleportToPlayerAdjustY(this, 1.0, random.nextDouble() * 3.0, 0.0075)); /**custom goal that gives mob a chance every tick to teleport to within initial follow_range-2 to follow_range+13 blocks of nearest player if it has not seen a player target within follow range for 15 seconds*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public boolean damageEntity(DamageSource damagesource, float f) {
        if (damagesource.getEntity() instanceof EntityPlayer && this.getHealth() - f > 0.0) { /**duplicates when hit by player and not killed*/
            new SpawnLivingEntity(this.getWorld(), new CustomEntitySilverfish(this.getWorld()), 1, null, null, this, false, true).run();
        }

        return super.damageEntity(damagesource, f);
    }

    public double getFollowRange() { /**silverfish have 20 block detection range (setting attribute doesn't work)*/
        return 20.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 15 && !this.a15) { /**after 15 attacks, silverfish gain speed 3*/
            this.a15 = true;
            this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 2));
        }

        if (this.attacks == 90 && !this.a90) { /**after 90 attacks, silverfish spawns a 5 by 3 by 5 block of invested stone around it and dies*/
            this.a90 = true;

            Location loc;
            for (int x = -2; x <= 2; x++) {
                for (int y = 0; y <= 2; y++) {
                    for (int z = -2; z <= 2; z++) {
                        loc = new Location(this.getWorld().getWorld(), Math.floor(this.locX()) + x, Math.floor(this.locY()) + y, Math.floor(this.locZ()) + z);
                        if (loc.getBlock().getType() == org.bukkit.Material.AIR) {
                            loc.getBlock().setType(org.bukkit.Material.INFESTED_STONE);
                        }
                    }
                }
            }

            this.die();
        }

        if (this.ticksLived == 10) { /**silverfish move 50% faster and have 9 health*/
            this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.375);
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(9.0);
            this.setHealth(9.0F);
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
