package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.VanillaPathfinderGoalsAccess;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableRingOfFireballs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.event.entity.EntityTargetEvent;

import java.lang.reflect.Field;

public class CustomEntityWitch extends EntityWitch implements ICustomMob {

    public PathfinderGoalSelector vanillaTargetSelector;
    private int attacks, attackNum;
    private boolean a12, a30;
    private final CustomEntityAreaEffectCloud newAEC;
    private static Field bx;

    public CustomEntityWitch(World world) {
        super(EntityTypes.WITCH, world);
        this.vanillaTargetSelector = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.attackNum = 0;
        this.a12 = false;
        this.a30 = false;
        this.newAEC = new CustomEntityAreaEffectCloud(this.getWorld(), 1.0F,5, 0);
        this.newAEC.addEffect(new MobEffect(MobEffects.HARM, 0));
        VanillaPathfinderGoalsAccess.removePathfinderGoals(this); // remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
    }

    static {
        try {
            bx = EntityWitch.class.getDeclaredField("bx");
            bx.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(1, new CustomPathfinderGoalRangedAttack<>(this, 1.0D, 5, 24.0F)); /** throws a potion every 5 ticks and uses the custom goal that attacks regardless of the y level (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal) */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    @Override
    public void a(EntityLiving entityLiving, float f) { // shoot()
        if (++this.attackNum % 6 == 0) { // attacks only count every 1.5 seconds, or 6 shots
            this.attacks++;
        }

        if (!this.m()) {
            Vec3D vec3d = entityLiving.getMot();
            double d0 = entityLiving.locX() + vec3d.x - this.locX();
            double d1 = entityLiving.getHeadY() - 1.100000023841858D - this.locY();
            double d2 = entityLiving.locZ() + vec3d.z - this.locZ();
            float f1 = MathHelper.sqrt(d0 * d0 + d2 * d2);
            PotionRegistry potionregistry = this.attacks < 45 ? Potions.HARMING : Potions.STRONG_HARMING; /** after 45 attacks, witches throw harming 2 instead of 1 */

            if (entityLiving instanceof EntityRaider) {
                if (entityLiving.getHealth() <= 10.0F) { /** gives fellow raiders instant health 2 instead of instant health 1 below 5 hearts */
                    potionregistry = Potions.STRONG_HEALING;
                } else {
                    potionregistry = Potions.STRONG_REGENERATION;
                }

                this.setGoalTarget(null, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
            } else if (f1 >= 6.0F && !entityLiving.hasEffect(MobEffects.SLOWER_MOVEMENT)) { /** gives slowness 2 up to 6 blocks away */
                potionregistry = Potions.STRONG_SLOWNESS;
            } else if (f1 < 6.0F && !entityLiving.hasEffect(MobEffects.WEAKNESS)) { /** 100% to give weakness when player within 6 blocks */
                potionregistry = Potions.WEAKNESS;
            } else if (f1 >= 5.0F) { /** gives poison 2 instead of 1 but poison range increased to anything beyond 5 blocks; within 5 blocks witches start spamming harming */
                potionregistry = Potions.STRONG_POISON;
            }

            EntityPotion entityPotion = new EntityPotion(this.world, this);

            entityPotion.setItem(PotionUtil.a(new ItemStack(Items.SPLASH_POTION), potionregistry));
            entityPotion.pitch -= -20.0F;
            entityPotion.shoot(d0, d1 + (double)(f1 * 0.2F), d2, 0.75F, 8.0F);
            if (!this.isSilent()) {
                this.world.playSound(null, this.locX(), this.locY(), this.locZ(), SoundEffects.ENTITY_WITCH_THROW, this.getSoundCategory(), 1.0F, 0.8F + this.random.nextFloat() * 0.4F);
            }

            this.world.addEntity(entityPotion);
        }
    }

    @Override
    public void movementTick() {
        super.movementTick();

        if (!this.world.isClientSide && this.isAlive()) {
            if (!this.m()) {
                PotionRegistry potionregistry = null;

                /** witches don't drink water breathing potions as they no longer take drowning damage */
                if ((this.isBurning() || this.dl() != null && this.dl().isFire()) && !this.hasEffect(MobEffects.FIRE_RESISTANCE)) { /** witches always drink fire resistance when they are burning */
                    potionregistry = Potions.FIRE_RESISTANCE;
                } else if (this.getHealth() < this.getMaxHealth() * 0.2 && !this.hasEffect(MobEffects.RESISTANCE)) { /** below 5.2 health, witches always drink turtle master 2 */
                    potionregistry = Potions.STRONG_TURTLE_MASTER;
                } else if (this.getHealth() < this.getMaxHealth() * 0.5) { /** below 13 health, witches always drink instant health 2 */
                    potionregistry = Potions.STRONG_HEALING;
                }

                if (potionregistry != null) {
                    this.setSlot(EnumItemSlot.MAINHAND, PotionUtil.a(new ItemStack(Items.POTION), potionregistry));
                }
            }
        }

        try { /** witches drink potions 3 times as fast */
            bx.setInt(this, bx.getInt(this) - 2);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public double getFollowRange() { /** witches have 24 blocks detection range */
        return 24.0;
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

        if (this.attacks == 12 && !this.a12) { /** after 12 attakcs, witches summon a ring of dragon fireballs */
            this.a12 = true;
            new RunnableRingOfFireballs(this, 1.0, 2, 1).run();
        }

        if (this.attacks == 30 && !this.a30) { /** after 30 attacks, witches summon area effect clouds wherever it goes */
            this.a30 = true;
            this.goalSelector.a(1, new NewPathfinderGoalSpawnBlocksEntitiesOnMob(this, this.newAEC, 1, 0, 1, 0, 1.0));
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
}
