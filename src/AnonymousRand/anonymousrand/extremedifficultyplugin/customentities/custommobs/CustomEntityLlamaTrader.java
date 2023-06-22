package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util.ICustomMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLlamaSpit;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class CustomEntityLlamaTrader extends EntityLlamaTrader implements ICustomMob, IAttackLevelingMob {

    private int attacks;
    private boolean a15;
    private static Field bH;

    public CustomEntityLlamaTrader(World world) {
        super(EntityTypes.TRADER_LLAMA, world);
        this.a(PathType.LAVA, 0.0F); /** no longer avoids lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F); /** no longer avoids fire */
        this.attacks = 0;
        this.a15 = false;
        this.setStrength(1); /** makes sure zombies etc. don't run away from trader llamas */
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0); /** trader llamas have 30 health */
        this.setHealth(30.0F);
    }

    static {
        try {
            bH = EntityLlama.class.getDeclaredField("bH");
            bH.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() { /** llamas won't panic anymore, are always aggro towards players, don't stop attacking after spitting once, and no longer defend wolves */
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /** custom goal that allows non-player mobs to still go fast in cobwebs */
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /** custom goal that allows this mob to take certain buffs from bats etc. */
        this.goalSelector.a(2, new PathfinderGoalLlamaFollow(this, 2.0999999046325684D));
        this.goalSelector.a(3, new PathfinderGoalArrowAttack(this, 1.25D, 40, 20.0F));
        this.goalSelector.a(4, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 0.7D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /** custom goal that allows llama to keep spitting indefinitely and prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage */
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class, false)); /** uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement) */
    }

    public void a(EntityLiving entityLiving, float f) { // shoot()s custom spit instead of vanilla
        this.attacks++;

        CustomEntityLlamaSpit entityLlamaspit = new CustomEntityLlamaSpit(this.getWorld(), this, this.attacks < 6 ? 12.0 : 18.0); /** after 6 attacks, trader llamas do 18 damage */
        double d0 = entityLiving.locX() - this.locX();
        double d1 = entityLiving.e(0.3333333333333333D) - entityLlamaspit.locY();
        double d2 = entityLiving.locZ() - this.locZ();
        f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;

        entityLlamaspit.shoot(d0, d1 + (double)f, d2, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.world.playSound(null, this.locX(), this.locY(), this.locZ(), SoundEffects.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        }

        this.world.addEntity(entityLlamaspit);

        try {
            bH.setBoolean(this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public double getFollowRange() { /** trader llamas have 24 block detection range (setting attribute doesn't work) */
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

        if (this.attacks == 15 && !this.a15) { /** after 15 attacks, trader llamas get 40 max health */
            this.a15 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(50.0);
            this.setHealth(50.0F);
        }
    }
}
