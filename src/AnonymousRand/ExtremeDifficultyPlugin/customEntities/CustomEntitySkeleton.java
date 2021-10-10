package AnonymousRand.ExtremeDifficultyPlugin.customEntities;

import net.minecraft.server.v1_16_R1.*;
import net.minecraft.server.v1_16_R1.EntitySkeletonAbstract;

public class CustomEntitySkeleton extends EntitySkeleton {

    private final PathfinderGoalBowShoot<EntitySkeletonAbstract> b = new PathfinderGoalBowShoot<>(this, 1.0D, 20, 15.0F);

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
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, true));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityIronGolem.class, true));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget<>(this, EntityTurtle.class, 10, true, false, EntityTurtle.bv));
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