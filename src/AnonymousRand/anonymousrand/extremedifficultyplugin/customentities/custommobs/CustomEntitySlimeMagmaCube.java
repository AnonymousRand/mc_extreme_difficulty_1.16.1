package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalBreakBlocksAround;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalSpawnBlocksEntitiesOnMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;

public class CustomEntitySlimeMagmaCube extends EntityMagmaCube implements ICommonCustomMethods {

    public PathfinderGoalSelector targetSelectorVanilla;
    private int attackCooldown;
    public int attacks;
    private boolean a15, a40, deathExplosion;

    public CustomEntitySlimeMagmaCube(World world) {
        super(EntityTypes.MAGMA_CUBE, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.attackCooldown = 0;
        this.attacks = 0;
        this.a15 = false;
        this.a40 = false;
        this.deathExplosion = false;
        RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    @Override
    protected void initPathfinder() { /**no longer targets iron golems*/
        super.initPathfinder();
        this.goalSelector.a(0, new CustomEntitySlimeMagmaCube.PathfinderGoalMagmaCubeFireAndLava(this)); /**custom goal that allows magma cube to summon fire, magma cubes and/or lava on it depending on attack count*/
        this.goalSelector.a(1, new PathfinderGoalMagmaCubeBlazeFireball(this)); /**custom goal that allows magma cube to occasionally shoot small fireballs in all directions depending on attack count*/
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void setSize(int i, boolean flag) { /**toned down stats a bit to account for potential size 16 magma cubes*/
        super.setSize(i, flag);
        this.getAttributeInstance(GenericAttributes.ARMOR).setValue(0.0); /**magma cubes don't have armor*/
        this.getAttributeInstance(GenericAttributes.MAX_HEALTH).setValue(1.0 + ((Math.log10(i) / Math.log10(2)) * ((Math.log10(i) + 0.6) / (Math.log10(1.4))))); //approx: 7.166 health for size 2, 17.452 health for size 4, 31.858 health for size 8, 50.385 health for size 16
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(Math.ceil(0.325F + 0.025F * (float)i)); //0.375 for 2, 0.425 for 4, 0.525 for 8, 0.725 for 16
        this.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE).setValue(Math.floor(((0.4 + Math.log10(i)) / (Math.log10(2))) * Math.pow(1.03, i))); //2 for 2, 3 for 4, 5 for 8, 8 for 16
        if (flag) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    protected void j(EntityLiving entityliving) {
        if (this.isAlive() && this.attackCooldown <= 0) { /**magma cubes attack every 20 ticks instead of every tick (which is effectively every 10 ticks due to damage immunity)*/
            int i = this.getSize();

            if (this.h((Entity)entityliving) < 0.6D * (double)i * 0.6D * (double)i && entityliving.damageEntity(DamageSource.mobAttack(this), this.eN())) { /**magma cubes don't need line of sight to attack player*/
                this.attackCooldown = 20;
                this.playSound(SoundEffects.ENTITY_SLIME_ATTACK, 1.0F, (random.nextFloat() - random.nextFloat()) * 0.2F + 1.0F);
                this.a((EntityLiving)this, (Entity)entityliving);
            }
        }
    }

    protected int eK() { /**magma cubes jump faster*/
        return random.nextInt(4) + 9;
    }

    public int getBlazeFireballGoalAttackCooldown() { /**after 20 attacks, magma cubes shoot a ring of blaze fireballs every 11 seconds (6.5 seconds after 40 attacks)*/
        return this.attacks < 20 ? Integer.MAX_VALUE : this.attacks < 40 ? 220 : 130;
    }

    public double getFollowRange() { /**magma cubes have 40 block detection range (setting attribute doesn't work)*/
        return 40.0;
    }

    @Override
    public void tick() {
        super.tick();

        this.attackCooldown--;

        if (this.getHealth() <= 0.0 && this.attacks >= 30 && !this.deathExplosion) { /**after 30 attacks, magma cubes explode when killed*/
            this.deathExplosion = true;
            this.getWorld().createExplosion(this, this.locX(), this.locY(), this.locZ(), (float)(Math.log10(this.getSize()) / Math.log10(2)), false, Explosion.Effect.DESTROY);
        }

        if (this.attacks == 15 && !this.a15) { /**after 15 attacks, magma cubes increase in size by 3 unless it is already at the largest possible size or is going to exceed it*/
            this.a15 = true;

            if (this.getSize() < 14) {
                this.setSize(this.getSize() + 3, true);
            }
        }

        if (this.attacks == 40 && !this.a40) { /**after 40 attacks, magma cubes increase in size by 3 unless it is already at the largest possible size or is going to exceed it*/
            this.a40 = true;

            if (this.getSize() < 14) {
                this.setSize(this.getSize() + 3, true);
            }
        }

        if (this.ticksLived == 10) {
            if (this.getSize() > 3) {
                this.goalSelector.a(0, new NewPathfinderGoalBreakBlocksAround(this, 10, this.getSize() / 4 + 1, this.getSize() / 4, this.getSize() / 4 + 1, this.getSize() / 4, false)); /**custom goal that breaks blocks around the mob periodically*/
            }
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

    static class PathfinderGoalMagmaCubeFireAndLava extends PathfinderGoal {

        private final CustomEntitySlimeMagmaCube cube;
        private int firstLand;

        public PathfinderGoalMagmaCubeFireAndLava(CustomEntitySlimeMagmaCube cube) {
            this.cube = cube;
            this.firstLand = 1;
        }

        @Override
        public boolean a() {
            return this.cube.getGoalTarget() != null;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.cube.isOnGround() && this.firstLand > 0) { /**magma cube spawns fire on it while on the ground and magma blocks below it*/
                this.firstLand = -15; //to reduce lag, this can only happen every 15 ticks
                double diameterDecimal = this.cube.getBoundingBox().maxX - this.cube.getBoundingBox().minX;
                int diameter = (int)(Math.floor(diameterDecimal) + 2);

                new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this.cube, org.bukkit.Material.MAGMA_BLOCK, 1, diameter / 2, 0, diameter / 2, -1.0, true).e();
                new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this.cube, org.bukkit.Material.FIRE, 1, diameter / 2, 0, diameter / 2, 0.0, false).e();
            } else {
                this.firstLand++;
            }
        }
    }

    static class PathfinderGoalMagmaCubeBlazeFireball extends PathfinderGoal {

        private final CustomEntitySlimeMagmaCube cube;

        public PathfinderGoalMagmaCubeBlazeFireball(CustomEntitySlimeMagmaCube cube) {
            this.cube = cube;
        }

        @Override
        public boolean a() {
            return this.cube.getGoalTarget() != null;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            if (this.cube.ticksLived % this.cube.getBlazeFireballGoalAttackCooldown() == 0) {
                new RunnableRingOfFireballs(this.cube, 0.4, 1).run();
            }
        }
    }
}
