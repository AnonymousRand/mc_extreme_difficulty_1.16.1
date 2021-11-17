package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import net.minecraft.server.v1_16_R1.*;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

public class CustomEntityWitch extends EntityWitch implements ICommonCustomMethods {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks, attackNum;
    private boolean a10, a30;
    private final CustomEntityAreaEffectCloud newAEC;
    Field bx;

    public CustomEntityWitch(World world) {
        super(EntityTypes.WITCH, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.attackNum = 0;
        this.a10 = false;
        this.a30 = false;
        this.newAEC = new CustomEntityAreaEffectCloud(this.getWorld(), 1.0F,5, 0);
        this.newAEC.addEffect(new MobEffect(MobEffects.HARM, 0));
        RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones

        try {
            this.bx = EntityWitch.class.getDeclaredField("bx");
            this.bx.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(1, new CustomPathfinderGoalArrowAttack(this, 1.0D, 5, 24.0F)); /**throws a potion every 5 ticks and uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        if (++this.attackNum % 8 == 0) { //attacks only count every 2 seconds, or 8 shots
            this.attacks++;
        }

        if (!this.m()) {
            Vec3D vec3d = entityliving.getMot();
            double d0 = entityliving.locX() + vec3d.x - this.locX();
            double d1 = entityliving.getHeadY() - 1.100000023841858D - this.locY();
            double d2 = entityliving.locZ() + vec3d.z - this.locZ();
            float f1 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            PotionRegistry potionregistry = Potions.HARMING;

            if (entityliving instanceof EntityRaider) {
                if (entityliving.getHealth() <= 10.0F) { /**gives fellow raiders instant health 2 instead of instant health 1 below 5 hearts*/
                    potionregistry = Potions.STRONG_HEALING;
                } else {
                    potionregistry = Potions.STRONG_REGENERATION;
                }

                this.setGoalTarget((EntityLiving)null);
            } else if (f1 >= 6.0F && !entityliving.hasEffect(MobEffects.SLOWER_MOVEMENT)) { /**gives slowness 2 instead of slowness 1 up to 6 blocks away*/
                potionregistry = Potions.STRONG_SLOWNESS;
            } else if (f1 < 5.0F && !entityliving.hasEffect(MobEffects.WEAKNESS)) { /**100% to give weakness when player within 5 blocks*/
                potionregistry = Potions.WEAKNESS;
            } else if (f1 >= 4.0F) { /**gives poison for 90 seconds instead of 45 but poison range increased to anything beyond 4 blocks; within 4 blocks witches start spamming harming 1*/
                potionregistry = Potions.LONG_POISON;
            }

            EntityPotion entitypotion = new EntityPotion(this.world, this);

            entitypotion.setItem(PotionUtil.a(new ItemStack(Items.SPLASH_POTION), potionregistry));
            entitypotion.pitch -= -20.0F;
            entitypotion.shoot(d0, d1 + (double) (f1 * 0.2F), d2, 0.75F, 8.0F);
            if (!this.isSilent()) {
                this.world.playSound((EntityHuman) null, this.locX(), this.locY(), this.locZ(), SoundEffects.ENTITY_WITCH_THROW, this.getSoundCategory(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            }

            this.world.addEntity(entitypotion);
        }
    }

    @Override
    public void movementTick() {
        super.movementTick();

        if (!this.world.isClientSide && this.isAlive()) {
            if (!this.m()) {
                PotionRegistry potionregistry = null;

                /**witches don't drink water breathing potions as they no longer take drowning damage*/
                if ((this.isBurning() || this.dl() != null && this.dl().isFire()) && !this.hasEffect(MobEffects.FIRE_RESISTANCE)) { /**witches always drink fire resistance when they are burning*/
                    potionregistry = Potions.FIRE_RESISTANCE;
                } else if (this.getHealth() < this.getMaxHealth() * 0.2 && !this.hasEffect(MobEffects.RESISTANCE)) { /**below 5.2 health, witches always drink turtle master 1*/
                    potionregistry = this.attacks < 45 ? Potions.TURTLE_MASTER : Potions.STRONG_TURTLE_MASTER; /**after 45 attacks, witches get turtle master 2 instead of 1*/
                } else if (this.getHealth() < this.getMaxHealth() * 0.5) { /**below 13 health, witches always drink instant health 2*/
                    potionregistry = Potions.STRONG_HEALING;
                }

                if (potionregistry != null) {
                    this.setSlot(EnumItemSlot.MAINHAND, PotionUtil.a(new ItemStack(Items.POTION), potionregistry));
                }
            }
        }

        try { /**witches drink potions twice as fast*/
            this.bx.setInt(this, this.bx.getInt(this) - 1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public double getFollowRange() { /**witches have 24 blocks detection range*/
        return 24.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 10 && !this.a10) { /**after 10 attakcs, witches summon a ring of dragon fireballs*/
            this.a10 = true;
            //todo:
        }

        if (this.attacks == 30 && !this.a30) { /**after 30 attacks, witches summon area effect clouds wherever it goes*/
            this.a30 = true;
            this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, this.newAEC, 1, 0, 1, 0, 1.0));
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
