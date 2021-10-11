package AnonymousRand.ExtremeDifficultyPlugin.customEntities;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.EntitySkeletonAbstract;
import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderGoalBowShoot;

import java.util.Random;

public class CustomEntitySkeleton extends EntitySkeleton {

    private final CustomPathfinderGoalBowShoot<EntitySkeletonAbstract> b = new CustomPathfinderGoalBowShoot<>(this, 1.0D, 20, 15.0F); //custom goal that continues to shoot arrows even when line of sight is broken (provided the mob has already recognized a target via nearestAttackableTarget goal)
    Random rand = new Random();

    public CustomEntitySkeleton(World world) {
        super(EntityTypes.SKELETON, world);
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.BOW)); //makes sure that it has a bow
        this.b.a(0); //reduced attack cooldown
    }

    @Override
    protected void initPathfinder() { //no longer avoids sun and wolves
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, new Class[0]));
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); //uses the custom goal which doesn't need line of sight to start shooting at players (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, false));
        this.targetSelector.a(3, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, false, false, EntityTurtle.bv));
    }

    @Override
    public void a(EntityLiving entityliving, float f){
        for (int i = 0; i < 75; i++) { //shoots 75 arrows at a time with increased inaccuracy to seem like a cone
            ItemStack itemstack = this.f(this.b(ProjectileHelper.a(this, Items.BOW)));
            EntityArrow entityarrow = this.b(itemstack, f);
            double d0 = entityliving.locX() - this.locX();
            double d1 = entityliving.e(0.3333333333333333D) - entityarrow.locY();
            double d2 = entityliving.locZ() - this.locZ();
            double d3 = (double) MathHelper.sqrt(d0 * d0 + d2 * d2);

            if (rand.nextDouble() <= 0.02) { //2% of arrows are piercing 1
                entityarrow.setPierceLevel((byte)1);
            }

            entityarrow.shoot(d0, d1 + d3 * 0.20000000298023224D, d2, 1.6F, (float) (40 - this.world.getDifficulty().a() * 4));
            this.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
            this.world.addEntity(entityarrow);
        }
    }

    @Override
    public void eM() { //re-registers the new field "b" since reflection doesn't seem to work
        if (this.world != null && !this.world.isClientSide) {
            this.goalSelector.a((PathfinderGoal) this.b);
            ItemStack itemstack = this.b(ProjectileHelper.a(this, Items.BOW));

            if (itemstack.getItem() == Items.BOW) {
                this.goalSelector.a(4, this.b);
            }
        }
    }
}