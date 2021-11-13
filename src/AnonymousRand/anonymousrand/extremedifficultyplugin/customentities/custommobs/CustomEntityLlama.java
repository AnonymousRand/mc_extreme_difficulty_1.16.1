package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLlamaSpit;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class CustomEntityLlama extends EntityLlama implements ICommonCustomMethods {

    public int attacks;
    private boolean a15;
    private Field bH;

    public CustomEntityLlama(World world) {
        super(EntityTypes.LLAMA, world);
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.attacks = 0;
        this.a15 = false;

        try {
            this.bH = EntityLlama.class.getDeclaredField("bH");
            this.bH.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void initPathfinder() { /**llamas won't panic anymore, are always aggro towards players, don't stop attacking after spitting once, and no longer defend wolves*/
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(2, new PathfinderGoalLlamaFollow(this, 2.0999999046325684D));
        this.goalSelector.a(3, new CustomPathfinderGoalArrowAttack(this, 1.25D, 40, 20.0F)); /**uses the custom goal that attacks even when line of sight is broken*/
        this.goalSelector.a(4, new PathfinderGoalBreed(this, 1.0D));
        this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 0.7D));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new CustomPathfinderGoalHurtByTarget(this, new Class[0])); /**custom goal that allows llama to keep spitting indefinitely and prevents mobs from retaliating against other mobs in case the mob damage event doesn't register and cancel the damage*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    public void a(EntityLiving entityliving, float f) { //shoots custom spit instead of vanilla
        this.attacks++;

        CustomEntityLlamaSpit entityllamaspit = new CustomEntityLlamaSpit(this.getWorld(), this, this.attacks < 6 ? 14.0 : 20.0); /**after 6 attacks, llamas do 20 damage*/
        double d0 = entityliving.locX() - this.locX();
        double d1 = entityliving.e(0.3333333333333333D) - entityllamaspit.locY();
        double d2 = entityliving.locZ() - this.locZ();
        f = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;

        entityllamaspit.shoot(d0, d1 + (double) f, d2, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.world.playSound((EntityHuman) null, this.locX(), this.locY(), this.locZ(), SoundEffects.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        }

        this.world.addEntity(entityllamaspit);

        try {
            this.bH.setBoolean(this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public double getFollowRange() { /**llamas have 24 block detection range (setting attribute doesn't work)*/
        return 24.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.attacks == 15 && !this.a15) { /**after 15 attacks, llamas get 40 max health*/
            this.a15 = true;
            ((LivingEntity)this.getBukkitEntity()).setMaxHealth(50.0);
            this.setHealth(50.0F);
        }

        if (this.ticksLived == 10) { /**llamas have 30 health*/
            this.setStrength(1); /**makes sure zombies etc. don't run away from llamas*/
            ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0);
            this.setHealth(30.0F);
        }
    }
}
