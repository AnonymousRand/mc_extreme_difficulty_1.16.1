package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.AttackLevelingController;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalHurtByTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.target.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.EntityFilter;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NmsUtil;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.Predicates;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.*;

public class CustomEntityEvoker extends EntityEvoker implements ICustomHostile, IAttackLevelingMob {

    /* Ignores line of sight and y-level for initially finding a player target and maintaining it as the target,
     * as well as for retaliating against players. Line of sight is also ignored for melee attack pathfinding. */
    private static final boolean IGNORE_LOS = true;
    private static final boolean IGNORE_Y = true;
    private EntitySheep wololoTarget;

    public CustomEntityEvoker(World world) {
        super(EntityTypes.EVOKER, world);
        this.initCustom();
        this.initAttackLevelingMob();
    }

    protected void initCustom() {
        /* No longer avoids fire and lava */
        this.a(PathType.DAMAGE_FIRE, 0.0F);
        this.a(PathType.LAVA, 0.0F);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `ICustomHostile`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    public double getDetectionRange() {
        /* Evokers have 28 block detection range */
        return 28.0;
    }

    public boolean ignoresLOS() {
        return IGNORE_LOS;
    }

    public boolean ignoresY() {
        return IGNORE_Y;
    }

    @Override
    public void checkDespawn() {
        if (this.world.getDifficulty() == EnumDifficulty.PEACEFUL && this.L()) {
            this.die();
        } else if (!this.isPersistent() && !this.isSpecialPersistence()) {
            EntityHuman nearestPlayer = this.world.findNearbyPlayer(this, -1.0);

            if (nearestPlayer != null) {
                /* Mobs only despawn along horizontal axes, so even at build height,
                   mobs will spawn below you and prevent sleeping */
                double distSqToNearestPlayer =
                        Math.pow(nearestPlayer.getPositionVector().getX() - this.getPositionVector().getX(), 2)
                        + Math.pow(nearestPlayer.getPositionVector().getZ() - this.getPositionVector().getZ(), 2);
                int forceDespawnDist = this.getEntityType().e().f();
                int forceDespawnDistSq = forceDespawnDist * forceDespawnDist;

                if (distSqToNearestPlayer > (double) forceDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                }

                /* Random despawn distance increased to 40 blocks */
                int randomDespawnDist = this.getEntityType().e().g() + 8;
                int randomDespawnDistSq = randomDespawnDist * randomDespawnDist;

                if (this.ticksFarFromPlayer > 600
                        && random.nextInt(800) == 0
                        && distSqToNearestPlayer > (double) randomDespawnDistSq
                        && this.isTypeNotPersistent(distSqToNearestPlayer)) {
                    this.die();
                } else if (distSqToNearestPlayer < (double) randomDespawnDistSq) {
                    this.ticksFarFromPlayer = 0;
                }
            }
        } else {
            this.ticksFarFromPlayer = 0;
        }
    }

    @Override
    public int bL() {
        return Integer.MAX_VALUE;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // `IAttackLevelingMob`
    ///////////////////////////////////////////////////////////////////////////////////////////////

    private AttackLevelingController attackLevelingController = null;

    protected void initAttackLevelingMob() {
        this.attackLevelingController = new AttackLevelingController(25, 35, 60);
    }

    public int getAttacks() {
        return this.attackLevelingController.getAttacks();
    }

    public void increaseAttacks(int increase) {
        int[] attackThreshs = this.getAttacksThreshs();
        int[] metThreshs = this.attackLevelingController.increaseAttacksAndCheckThreshs(increase);

        for (int metThresh : metThreshs) {
            if (metThresh == attackThreshs[0]) {
                /* After 25 attacks, evokers summon 3 vexes and gain regen 2 */
                new SpawnEntity(this.world, new CustomEntityVex(this.world), 3, null, null, this, false, false);
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 1));
            } else if (metThresh == attackThreshs[1]) {
                /* After 35 attacks, evokers gain regen 3 */
                this.addEffect(new MobEffect(MobEffects.REGENERATION, Integer.MAX_VALUE, 2));
            } else if (metThresh == attackThreshs[2]) {
                /* After 60 attacks, evokers gain speed 1 */
                this.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, Integer.MAX_VALUE, 0));
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
    protected void initPathfinder() {
        /* Evokers no longer target iron golems and villagers */
        // from `EntityRaider`
        this.goalSelector.a(1, new EntityRaider.b<>(this));
        this.goalSelector.a(3, new PathfinderGoalRaid<>(this));
        this.goalSelector.a(4, new PathfinderGoalInvadeHome(this, 1.05, 1));
        this.goalSelector.a(5, new PathfinderGoalCelebrateRaidLoss(this));
        // from `EntityMonsterPatrolling`
        this.goalSelector.a(4, new EntityMonsterPatrolling.a(this, 0.7, 0.595));
        // other
        this.goalSelector.a(0, new CustomPathfinderGoalMoveFasterInCobweb(this));                                /* Still moves fast in cobwebs */
        this.goalSelector.a(0, new CustomPathfinderGoalGetBuffedByMobs(this));                                   /* Takes buffs from bats, piglins, etc. */
        this.goalSelector.a(0, new CustomPathfinderGoalBreakBlocksAround(this, 20, 2, 1, 2, 2, true));           /* Breaks most blocks around the mob periodically */
        this.goalSelector.a(0, new PathfinderGoalFloat(this));
        this.goalSelector.a(1, new CustomEntityEvoker.PathfinderGoalCastSpell());
        this.goalSelector.a(2, new PathfinderGoalAvoidTarget<>(this, EntityPlayer.class, 8.0F, 0.6, 1.0));
        this.goalSelector.a(4, new CustomEntityEvoker.PathfinderGoalSummonVexSpell());
        this.goalSelector.a(5, new CustomEntityEvoker.PathfinderGoalFangSpell());
        this.goalSelector.a(6, new CustomEntityEvoker.PathfinderGoalWololoSpell());
        this.goalSelector.a(8, new PathfinderGoalRandomStroll(this, 0.6));
        this.goalSelector.a(9, new PathfinderGoalLookAtPlayer(this, EntityPlayer.class, 3.0F, 1.0F));
        this.goalSelector.a(10, new PathfinderGoalLookAtPlayer(this, EntityInsentient.class, 8.0F));
        this.targetSelector.a(0, new CustomPathfinderGoalHurtByTarget<>(this, EntityRaider.class));              /* Always retaliates against players and teleports to them if they are out of range/do not have line of sight, but doesn't retaliate against other mobs (in case the EntityDamageByEntityEvent listener doesn't register and cancel the damage) */
        this.targetSelector.a(1, (new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityPlayer.class))); /* Ignores invis/skulls for initially finding a player target and maintaining it as the target, and periodically retargets the nearest option */
    }

    @Override
    public void die() {
        super.die();

        /* After 60 attacks, evokers summon 7 vexes when killed */
        if (this.getAttacks() >= 60) {
            new SpawnEntity(this.world, new CustomEntityVex(this.world), 7, null, null, this, false, false);
        }
    }

    /* Overrides private `fh()` */
    @Nullable
    public EntitySheep getWololoTarget() {
        return this.wololoTarget;
    }

    /* Overrides private `a()` */
    public void setWololoTarget(@Nullable EntitySheep sheep) {
        this.wololoTarget = sheep;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Nested Classes
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /* If this is static it goes "waaaah but but no enclosing instance :(" like i hate you */
    class PathfinderGoalSummonVexSpell extends EntityIllagerWizard.c {

        @Override
        public boolean a() {
            if (!super.a()) {
                return false;
            }

            int vexNearbyCount = CustomEntityEvoker.this.getWorld().a(
                    EntityVex.class,
                    CustomEntityEvoker.this.getBoundingBox().g(getDetectionRange())).size();
            /* Evokers can summon vexes when there are less than or equal to 10 vexes in their follow range */
            return vexNearbyCount <= 10;
        }

        @Override // `getGlobalSpellCooldown()`; prevents spells from being cast simultaneously
        protected int g() {
            return 100;
        }

        @Override // `getSpellCooldown()`
        protected int h() {
            /* Evokers summon vexes every 25 seconds instead of 17 */
            return 500;
        }

        @Override // `castSpell()`
        protected void j() {
            CustomEntityEvoker.this.increaseAttacks(6);

            /* Evokers summon 5 vexes at a time instead of 3 */
            for (int i = 0; i < 5; i++) {
                BlockPosition blockPos = CustomEntityEvoker.this.getChunkCoordinates().b(
                        random.nextInt(5) - 2, 1, random.nextInt(5) - 2);
                CustomEntityVex newVex = new CustomEntityVex(CustomEntityEvoker.this.getWorld());

                newVex.setPositionRotation(blockPos, 0.0F, 0.0F);
                newVex.prepare(
                        CustomEntityEvoker.this.getWorld(),
                        CustomEntityEvoker.this.getWorld().getDamageScaler(blockPos),
                        EnumMobSpawn.MOB_SUMMONED,
                        null,
                        null);
                newVex.a(CustomEntityEvoker.this);
                newVex.g(blockPos);
                newVex.a(20 * (30 + random.nextInt(90)));
                CustomEntityEvoker.this.getWorld().addEntity(newVex, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        @Override // `getSpellPrepareSound()`
        protected SoundEffect k() {
            return SoundEffects.ENTITY_EVOKER_PREPARE_SUMMON;
        }

        @Override // `getSpellType()`
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.SUMMON_VEX;
        }
    }

    class PathfinderGoalFangSpell extends EntityIllagerWizard.c {

        @Override // `getGlobalSpellCooldown()`; prevents spells from being cast simultaneously 
        protected int g() {
            return 100;
        }

        @Override // `getSpellCooldown()`
        protected int h() {
            /* Evokers summon fangs every 85 ticks instead of 100 */
            return 85;
        }

        @Override // `castSpell()`
        protected void j() {
            EntityLiving attackTarget = CustomEntityEvoker.this.getGoalTarget();
            double lowerEntityY = Math.min(attackTarget.locY(), CustomEntityEvoker.this.locY());
            double higherEntityY = Math.max(attackTarget.locY(), CustomEntityEvoker.this.locY()) + 1.0;
            float angleToAttackTarget = (float) MathHelper.d(
                    attackTarget.locZ() - CustomEntityEvoker.this.locZ(),
                    attackTarget.locX() - CustomEntityEvoker.this.locX());

            if (NmsUtil.distSq(CustomEntityEvoker.this, attackTarget, true) < 9.0) {
                // if target is within 3 blocks away, summon fangs in 2 circles
                float yaw;

                for (int i = 0; i < 5; i++) {
                    yaw = angleToAttackTarget + (float) (i * 3.1415927 * 0.4);
                    this.spawnFangs(
                            CustomEntityEvoker.this.locX() + MathHelper.cos(yaw) * 1.5,
                            CustomEntityEvoker.this.locZ() + MathHelper.sin(yaw) * 1.5,
                            lowerEntityY,
                            higherEntityY,
                            yaw,
                            0);
                }

                for (int i = 0; i < 8; i++) {
                    yaw = angleToAttackTarget + (float) (i * 3.1415927 * 2.0 / 8.0 + 1.2566371);
                    this.spawnFangs(
                            CustomEntityEvoker.this.locX() + MathHelper.cos(yaw) * 2.5,
                            CustomEntityEvoker.this.locZ() + MathHelper.sin(yaw) * 2.5,
                            lowerEntityY,
                            higherEntityY,
                            yaw,
                            3);
                }
            } else {
                // else, summon fangs in straight line
                /* Evoker fang range synced to detection range */
                double detectionRange = CustomEntityEvoker.this.getDetectionRange();
                for (int i = 0; i < detectionRange; i++) {
                    double distMultiplier = 1.25 * (double) (i + 1);
                    this.spawnFangs(
                            CustomEntityEvoker.this.locX() + MathHelper.cos(angleToAttackTarget) * distMultiplier,
                            CustomEntityEvoker.this.locZ() + MathHelper.sin(angleToAttackTarget) * distMultiplier,
                            lowerEntityY,
                            higherEntityY,
                            angleToAttackTarget,
                            i);
                }
            }

            /* Every time the fangs attack, the target player is slowed for 1.2 seconds
             * (regardless of if the fangs were dodged) */
            new RunnableEvokerStopPlayer(attackTarget, 8).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 3L);
        }

        public void spawnFangs(
                double x, double z, double lowerEntityY, double higherEntityY, float yaw, int spawnDelay) {
            EntityLiving attackTarget = CustomEntityEvoker.this.getGoalTarget();
            BlockPosition blockPos = new BlockPosition(x, higherEntityY, z);
            boolean suitableYFound = false;
            double yAddition = 0.0;

            // find suitable y-level to spawn fangs on
            do {
                BlockPosition blockPosDown = blockPos.down();
                IBlockData blockDataDown = CustomEntityEvoker.this.getWorld().getType(blockPosDown);

                if (blockDataDown.d(CustomEntityEvoker.this.getWorld(), blockPosDown, EnumDirection.UP)) {
                    if (!CustomEntityEvoker.this.getWorld().isEmpty(blockPos)) {
                        IBlockData blockData = CustomEntityEvoker.this.getWorld().getType(blockPos);
                        VoxelShape voxelShape = blockData.getCollisionShape(
                                CustomEntityEvoker.this.getWorld(), blockPos);
                        if (!voxelShape.isEmpty()) {
                            yAddition = voxelShape.c(EnumDirection.EnumAxis.Y);
                        }
                    }
                    suitableYFound = true;
                    break;
                }

                blockPos = blockPos.down();
            } while (blockPos.getY() >= MathHelper.floor(lowerEntityY) - 1);

            if (suitableYFound) {
                CustomEntityEvoker.this.increaseAttacks(2);
                org.bukkit.World bukkitWorld = CustomEntityEvoker.this.getWorld().getWorld();
                Location bukkitLoc;
                Location bukkitLocBase;
                Block bukkitBlock;
                org.bukkit.Material bukkitMaterial;

                BlockIterator blockIterator = new BlockIterator(
                        bukkitWorld,
                        new Vector(
                                CustomEntityEvoker.this.locX(),
                                CustomEntityEvoker.this.locY(),
                                CustomEntityEvoker.this.locZ()),
                        new Vector(
                                attackTarget.locX() - CustomEntityEvoker.this.locX(),
                                attackTarget.locY() - CustomEntityEvoker.this.locY(),
                                attackTarget.locZ() - CustomEntityEvoker.this.locZ()),
                        1.0,
                        (int) Math.ceil(CustomEntityEvoker.this.getDetectionRange()));
                /* Every time fangs are summoned, evokers breaks all blocks within follow distance of itself towards the
                 * target, drilling a 3 by 3 hole through any blocks */ // todo runnable?
                while (blockIterator.hasNext()) {
                    bukkitLocBase = blockIterator.next().getLocation();

                    for (int x2 = -1; x2 <= 1; x2++) {
                        for (int y2 = -1; y2 <= 1; y2++) {
                            for (int z2 = -1; z2 <= 1; z2++) {
                                bukkitLoc = new Location(
                                        bukkitWorld,
                                        bukkitLocBase.getX() + x2,
                                        bukkitLocBase.getY() + y2,
                                        bukkitLocBase.getZ() + z2);
                                bukkitBlock = bukkitLoc.getBlock();
                                bukkitMaterial = bukkitBlock.getType();

                                if (Predicates.blockBreakableDefault.test(bukkitMaterial)
                                        && Predicates.notBedrock.test(bukkitMaterial)
                                        && Predicates.notHardBlocks.test(bukkitMaterial)) {
                                    bukkitBlock.setType(org.bukkit.Material.AIR);
                                } else if (!Predicates.notHardBlocks.test(bukkitMaterial)) {
                                    // 50% chance to break these blocks
                                    if (CustomEntityEvoker.this.getRandom().nextDouble() < 0.5) {
                                        bukkitBlock.setType(org.bukkit.Material.AIR);
                                    }
                                }
                            }
                        }
                    }
                }

                CustomEntityEvoker.this.getWorld().addEntity(new EntityEvokerFangs(
                        CustomEntityEvoker.this.getWorld(),
                        x,
                        (double) blockPos.getY() + yAddition,
                        z,
                        yaw,
                        spawnDelay,
                        CustomEntityEvoker.this));
            }
        }

        @Override // `getSpellPrepareSound()`
        protected SoundEffect k() {
            return SoundEffects.ENTITY_EVOKER_PREPARE_ATTACK;
        }

        @Override // `getSpellType()`
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.FANGS;
        }
    }

    class PathfinderGoalWololoSpell extends EntityIllagerWizard.c {
        
        /* Evokers can target all non-pink sheep within detection range */
        private final EntityFilter targetCondition = new EntityFilter(
                CustomEntityEvoker.this.getDetectionRange(),
                CustomEntityEvoker.this.ignoresLOS(),
                CustomEntityEvoker.this.ignoresY(),
                (entityLiving) -> !((EntitySheep) entityLiving).getColor().equals(EnumColor.PINK));

        @Override
        public boolean a() {
            boolean passedBasicChecks =
                    CustomEntityEvoker.this.getGoalTarget() == null
                    && !CustomEntityEvoker.this.eX()
                    && CustomEntityEvoker.this.ticksLived >= this.c
                    && CustomEntityEvoker.this.getWorld().getGameRules().getBoolean(GameRules.MOB_GRIEFING);
            if (!passedBasicChecks) {
                return false;
            }

            // todo entityfilter try builder pattern
            /* Evokers wololo the nearest sheep instead of a random one now */
            EntitySheep candidateTarget = NmsUtil.getNearestEntityInRange(
                    EntitySheep.class,
                    this.targetCondition,
                    CustomEntityEvoker.this,
                    CustomEntityEvoker.this.getDetectionRange());

            if (candidateTarget != null) {
                CustomEntityEvoker.this.setWololoTarget(candidateTarget);
                return true;
            }

            return false;
        }

        @Override
        public boolean b() {
            return CustomEntityEvoker.this.getWololoTarget() != null && this.b > 0;
        }

        @Override
        public void d() {
            super.d();
            CustomEntityEvoker.this.setWololoTarget(null);
        }

        @Override // `castSpell()`
        protected void j() {
            EntitySheep wololoTarget = CustomEntityEvoker.this.getWololoTarget();

            /* Instead of turning sheep red, evokers replace them with aggressive pink sheep */
            if (EntityFilter.BASE.test(wololoTarget)) {
                new SpawnEntity(
                        wololoTarget.getWorld(),
                        new CustomEntitySheepAggressive(wololoTarget.getWorld()),
                        1,
                        null,
                        null,
                        wololoTarget,
                        true,
                        true);
            }
        }

        @Override // `getSpellWarmupTime()`
        protected int m() {
            return 40;
        }

        @Override // `getGlobalSpellCooldown()`; prevents spells from being cast simultaneously 
        protected int g() {
            return 60;
        }

        @Override // `getSpellCooldown()`
        protected int h() {
            return 140;
        }

        @Override // `getSpellPrepareSound()`
        protected SoundEffect k() {
            return SoundEffects.ENTITY_EVOKER_PREPARE_WOLOLO;
        }

        @Override // `getSpellType()`
        protected EntityIllagerWizard.Spell l() {
            return EntityIllagerWizard.Spell.WOLOLO;
        }
    }

    class PathfinderGoalCastSpell extends EntityIllagerWizard.b {
        @Override
        public void e() {
            if (CustomEntityEvoker.this.getGoalTarget() != null) {
                CustomEntityEvoker.this.getControllerLook().a(
                        CustomEntityEvoker.this.getGoalTarget(),
                        (float) CustomEntityEvoker.this.ep(),
                        (float) CustomEntityEvoker.this.eo());
            } else if (CustomEntityEvoker.this.getWololoTarget() != null) {
                CustomEntityEvoker.this.getControllerLook().a(
                        CustomEntityEvoker.this.getWololoTarget(),
                        (float) CustomEntityEvoker.this.ep(),
                        (float) CustomEntityEvoker.this.eo());
            }
        }
    }

    /* From `EntityRaider` */
    static class PathfinderGoalCelebrateRaidLoss extends PathfinderGoal {

        private final CustomEntityEvoker evoker;

        public PathfinderGoalCelebrateRaidLoss(CustomEntityEvoker entityRaider) {
            this.evoker = entityRaider;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            Raid raid = this.evoker.fb();
            return this.evoker.isAlive() && this.evoker.getGoalTarget() == null && raid != null && raid.isLoss();
        }

        @Override
        public void c() {
            this.evoker.x(true);
            super.c();
        }

        @Override
        public void d() {
            this.evoker.x(false);
            super.d();
        }

        @Override
        public void e() {
            if (!this.evoker.isSilent() && this.evoker.getRandom().nextInt(100) == 0) {
                this.evoker.playSound(this.evoker.eM(), this.evoker.getSoundVolume(), this.evoker.dG());
            }

            if (!this.evoker.isPassenger() && this.evoker.getRandom().nextInt(50) == 0) {
                this.evoker.getControllerJump().jump();
            }

            super.e();
        }
    }

    /* From `EntityRaider` */
    static class PathfinderGoalInvadeHome extends PathfinderGoal {

        private final CustomEntityEvoker evoker;
        private final double speedTowardsTarget;
        private BlockPosition targetBlockPos;
        private final ArrayList<BlockPosition> previousTargetsBlockPos = new ArrayList<>();
        private final int rememberTargetDist;
        private boolean cannotPathToTarget;

        public PathfinderGoalInvadeHome(CustomEntityEvoker evoker, double speedTowardsTarget, int rememberTargetDist) {
            this.evoker = evoker;
            this.speedTowardsTarget = speedTowardsTarget;
            this.rememberTargetDist = rememberTargetDist;
            this.a(EnumSet.of(PathfinderGoal.Type.MOVE));
        }

        @Override
        public boolean a() {
            this.prunePreviousTargetsMemory();
            return this.isRaidOngoing() && this.findTarget() && this.evoker.getGoalTarget() == null;
        }

        @Override
        public boolean b() {
            if (this.evoker.getNavigation().m()) {
                return false;
            }

            return this.evoker.getGoalTarget() == null
                    // `a()` is `withinDistance()`; idk why it's negated but `this.rememberTargetDist = 1` anyway
                    && !this.targetBlockPos.a(
                            this.evoker.getPositionVector(), this.evoker.getWidth() + this.rememberTargetDist)
                    && !this.cannotPathToTarget;
        }

        @Override
        public void c() {
            super.c();

            this.evoker.n(0);              // `setIdleTime()`
            this.evoker.getNavigation().a( // `tryMoveToTarget()`
                    this.targetBlockPos.getX(),
                    this.targetBlockPos.getY(),
                    this.targetBlockPos.getZ(),
                    this.speedTowardsTarget);
            this.cannotPathToTarget = false;
        }

        @Override
        public void d() {
            if (this.targetBlockPos.a(this.evoker.getPositionVector(), this.rememberTargetDist)) { // `withinDistance()`
                this.previousTargetsBlockPos.add(this.targetBlockPos);
            }
        }

        @Override
        public void e() {
            if (this.evoker.getNavigation().m()) {                                                    // `noPath()`
                Vec3D vec3d = Vec3D.c(this.targetBlockPos);
                Vec3D vec3d1 = RandomPositionGenerator.a(this.evoker, 16, 7, vec3d, 0.31415927);

                if (vec3d1 == null) {
                    vec3d1 = RandomPositionGenerator.b(this.evoker, 8, 7, vec3d);
                }

                if (vec3d1 == null) {
                    this.cannotPathToTarget = true;
                    return;
                }

                this.evoker.getNavigation().a(vec3d1.x, vec3d1.y, vec3d1.z, this.speedTowardsTarget); // `tryMoveTo()`
            }
        }

        private boolean findTarget() {
            WorldServer worldServer = (WorldServer) this.evoker.getWorld();
            BlockPosition blockPosEvoker = this.evoker.getChunkCoordinates();
            Optional<BlockPosition> candidateTargetBlockPos = worldServer.x().a(
                    (villagePlaceType) -> villagePlaceType == VillagePlaceType.r,
                    this::isNotAPreviousTarget,
                    VillagePlace.Occupancy.ANY,
                    blockPosEvoker,
                    48,
                    this.evoker.getRandom());

            if (!candidateTargetBlockPos.isPresent()) {
                return false;
            }

            this.targetBlockPos = candidateTargetBlockPos.get().immutableCopy();
            return true;
        }

        private void prunePreviousTargetsMemory() {
            while (this.previousTargetsBlockPos.size() > 2) {
                this.previousTargetsBlockPos.remove(0);
            }
        }

        private boolean isNotAPreviousTarget(BlockPosition candidateTargetBlockPos) {
            for (BlockPosition blockPos : this.previousTargetsBlockPos) {
                if (Objects.equals(candidateTargetBlockPos, blockPos)) {
                    return false;
                }
            }

            return true;
        }

        private boolean isRaidOngoing() {
            return this.evoker.fc() && !this.evoker.fb().a();
        }
    }

    static class RunnableEvokerStopPlayer extends BukkitRunnable {

        private final LivingEntity bukkitTarget;
        private int cycleCount;
        private final int maxCycleCount;

        public RunnableEvokerStopPlayer(EntityLiving target, int maxCycleCount) {
            this.bukkitTarget = (LivingEntity) target.getBukkitEntity();
            this.cycleCount = 0;
            this.maxCycleCount = maxCycleCount;
        }

        @Override
        public void run() {
            if (++this.cycleCount > this.maxCycleCount) {
                this.cancel();
                return;
            }

            this.bukkitTarget.setVelocity(new Vector(0.0, 0.0, 0.0));
        }
    }
}
