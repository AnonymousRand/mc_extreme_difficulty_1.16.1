package AnonymousRand.anonymousrand.plugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles.CustomEntityLlamaSpit;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attack.CustomPathfinderGoalAttackRanged;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.attackmvmt.CustomPathfinderGoalAttackMvmtRanged;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.plugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;

public class CustomEntityLlama extends EntityLlama implements ICustomHostile, IAttackLevelingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;

    private AttackLevelingController attackLevelingController = null;
    private Field didSpit;

    public CustomEntityLlama(World world) {
        super(EntityTypes.LLAMA, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    protected void initCustom() {
        this.initAttributes();

        try {
            this.didSpit = EntityLlama.class.getDeclaredField("bH");
            this.didSpit.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);

        this.setStrength(1); /* makes sure wolves etc. don't run away from llamas; also makes their inventory smaller */
    }

    protected void initAttributes() {
        ((LivingEntity) this.getBukkitEntity()).setMaxHealth(30.0); /* llamas have 30 health */
        this.setHealth(30.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() { /* llamas have 24 block detection range */
        return 24.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    protected void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(15);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] attackThreshs = this.getAttacksThreshs();
        int[] metThreshs = this.attackLevelingController.increaseAttacksAndCheckThreshs(increase);

        for (int metThresh : metThreshs) {
            if (metThresh == attackThreshs[0]) {
                /* After 15 attacks, llamas get 50 max health and health */
                ((LivingEntity) this.getBukkitEntity()).setMaxHealth(50.0);
                this.setHealth(50.0F);
            }
        }
    }

    public int[] getAttacksThreshs() {
        return this.attackLevelingController.getAttacksThreshs();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Overridden Vanilla Functions
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected void initPathfinder() { /* llamas won't panic anymore, are always aggro towards players, don't stop attack after spitting once, and no longer defend wolves */
        /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));
        /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalLlamaFollow(this, 2.1));
        this.goalSelector.a(3, new CustomPathfinderGoalAttackRanged<>(this, 40));
        this.goalSelector.a(3, new CustomPathfinderGoalAttackMvmtRanged<>(this, 1.25));
        this.goalSelector.a(4, new PathfinderGoalBreed(this, 1.0));
        this.goalSelector.a(5, new PathfinderGoalFollowParent(this, 1.0));
        this.goalSelector.a(6, new PathfinderGoalRandomStrollLand(this, 0.7));
        this.goalSelector.a(7, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 6.0F));
        this.goalSelector.a(8, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this));               /* custom goal that allows llama to keep spitting indefinitely and prevents mobs from retaliating against other mobs in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage */
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class)); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    public void a(EntityLiving attackTarget, float distFactor) { // shoot()
        this.increaseAttacks(1);

        CustomEntityLlamaSpit entityLlamaspit = new CustomEntityLlamaSpit(this.world, this, this.getAttacks() < 6 ? 12.0 : 18.0); /* after 6 attacks, trader llamas do 18 damage */
        double d0 = attackTarget.locX() - this.locX();
        double d1 = attackTarget.e(0.3333333333333333) - entityLlamaspit.locY();
        double d2 = attackTarget.locZ() - this.locZ();
        distFactor = MathHelper.sqrt(d0 * d0 + d2 * d2) * 0.2F;

        entityLlamaspit.shoot(d0, d1 + (double) distFactor, d2, 1.5F, 10.0F);
        if (!this.isSilent()) {
            this.world.playSound(null, this.locX(), this.locY(), this.locZ(), SoundEffects.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1.0F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.2F);
        }

        this.world.addEntity(entityLlamaspit);

        try {
            this.didSpit.setBoolean(this, true);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
