package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalArrowAttack;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderGoalNearestAttackableTarget;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalCobwebMoveFaster;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.NewPathfinderGoalGetBuffedByMobs;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.RemovePathfinderGoals;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMobShootArrowsNormally;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;

import java.util.Arrays;
import java.util.HashMap;

public class CustomEntityPillager extends EntityPillager implements ICommonCustomMethods {

    public PathfinderGoalSelector targetSelectorVanilla;
    public int attacks, attackNum;

    public CustomEntityPillager(World world) {
        super(EntityTypes.PILLAGER, world);
        this.targetSelectorVanilla = super.targetSelector;
        this.a(PathType.LAVA, 0.0F); /**no longer avoids lava*/
        this.a(PathType.DAMAGE_FIRE, 0.0F); /**no longer avoids fire*/
        this.setSlot(EnumItemSlot.MAINHAND, new ItemStack(Items.CROSSBOW)); //makes sure that it has a crossbow
        Arrays.fill(this.dropChanceArmor, 0.0f); /**pillagers can't drop any armor that it wears*/
        this.attacks = 0;
        this.attackNum = 0;

        if (random.nextDouble() < 0.25) { /**pillagers have a 25% chance to spawn double and a 25% chance to spawn as an illusioner instead*/
            new SpawnLivingEntity(this.getWorld(), new CustomEntityPillager(this.getWorld()), 1, null, null, this, false, true);
        } else if (random.nextDouble() < 0.5) {
            new SpawnLivingEntity(this.getWorld(), new CustomEntityIllagerIllusioner(this.getWorld()), 1, null, null, this, true, true);
        }
    }

    @Override
    public void initPathfinder() {
        super.initPathfinder();
        this.goalSelector.a(0, new NewPathfinderGoalCobwebMoveFaster(this)); /**custom goal that allows non-player mobs to still go fast in cobwebs*/
        this.goalSelector.a(0, new NewPathfinderGoalGetBuffedByMobs(this)); /**custom goal that allows this mob to take certain buffs from bats etc.*/
        this.goalSelector.a(0, new CustomEntityPillager.PathfinderGoalPillagerUpgradeArmor(this)); /**custom goal that allows the pillager to upgrade its armor gradually as part of the attacks system*/
        this.goalSelector.a(2, new CustomPathfinderGoalArrowAttack(this, 1.0, 3, 24.0F)); /**shoots every 3 ticks; uses the custom goal that attacks even when line of sight is broken (the old goal stopped the mob from attacking even if the mob has already recognized a target via CustomNearestAttackableTarget goal)*/
        this.targetSelector.a(2, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityVillagerAbstract.class, false));
        this.targetSelector.a(1, new CustomPathfinderGoalNearestAttackableTarget<>(this, EntityHuman.class, false)); /**uses the custom goal which doesn't need line of sight to start attacking (passes to CustomPathfinderGoalNearestAttackableTarget.g() which passes to CustomIEntityAccess.customFindPlayer() which passes to CustomIEntityAccess.customFindEntity() which passes to CustomPathfinderTargetConditions.a() which removes line of sight requirement)*/
    }

    @Override
    public void a(EntityLiving entityliving, float f) { //shoot
        if (++this.attackNum % 12 == 0) { //attacks only count every ~2 seconds, or 12 shots
            this.attacks++;
        }

        new RunnableMobShootArrowsNormally(this, entityliving, f, 1, this.attackNum % 24 == 0 ? 6 : 1, 3.0, random.nextDouble() < (this.attackNum % 24 == 0 ? 0.75 : 0.05) ? 1 : 0, false, true); /**shoots a knockback arrow every 24th attack; 5% of arrows shot are piercing 1 (75% for knockback arrow); arrows do not lose y level*/
    }

    public double getFollowRange() { /**pillagers have 24 block detection range (setting attribute doesn't work)*/
        return 24.0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.ticksLived == 10) {
            RemovePathfinderGoals.removePathfinderGoals(this); //remove vanilla HurtByTarget and NearestAttackableTarget goals and replace them with custom ones
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

    static class PathfinderGoalPillagerUpgradeArmor extends PathfinderGoal {

        private final CustomEntityPillager pillager;
        private HashMap<Integer, Boolean> attackBooleans = new HashMap<>();

        public PathfinderGoalPillagerUpgradeArmor(CustomEntityPillager pillager) {
            this.pillager = pillager;
            this.attackBooleans.put(4, false);
            this.attackBooleans.put(8, false);
            this.attackBooleans.put(11, false);
            this.attackBooleans.put(15, false);
            this.attackBooleans.put(19, false);
            this.attackBooleans.put(22, false);
            this.attackBooleans.put(26, false);
            this.attackBooleans.put(29, false);
            this.attackBooleans.put(32, false);
            this.attackBooleans.put(36, false);
            this.attackBooleans.put(39, false);
            this.attackBooleans.put(42, false);
            this.attackBooleans.put(50, false);
        }

        @Override
        public boolean a() {
            if (this.attackBooleans.containsKey(this.pillager.attacks)) {
                if (!this.attackBooleans.get(this.pillager.attacks)) {
                    this.attackBooleans.replace(this.pillager.attacks, true);
                    return true;
                }
            }

            return false;
        }

        @Override
        public boolean b() {
            return this.a();
        }

        @Override
        public void e() {
            LivingEntity livingEntity = ((LivingEntity)this.pillager.getBukkitEntity());

            switch (this.pillager.attacks) {
                case 4 -> {
                    if (livingEntity.getEquipment().getChestplate().getType() != org.bukkit.Material.IRON_CHESTPLATE) {
                        livingEntity.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_CHESTPLATE));
                    }
                }
                case 8 -> {
                    if (livingEntity.getEquipment().getLeggings().getType() != org.bukkit.Material.IRON_LEGGINGS) {
                        livingEntity.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_LEGGINGS));
                    }
                }
                case 11 -> {
                    org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_BOOTS);
                    boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back*/
                    boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                    if (livingEntity.getEquipment().getHelmet().getType() != org.bukkit.Material.IRON_HELMET) {
                        livingEntity.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.IRON_HELMET));
                    }

                    if (livingEntity.getEquipment().getBoots().getType() != org.bukkit.Material.IRON_BOOTS) {
                        livingEntity.getEquipment().setBoots(boots);
                    }
                }
                case 15 -> {
                    if (livingEntity.getEquipment().getChestplate().getType() != org.bukkit.Material.DIAMOND_CHESTPLATE) {
                        livingEntity.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_CHESTPLATE));
                    }
                }
                case 19 -> {
                    if (livingEntity.getEquipment().getLeggings().getType() != org.bukkit.Material.DIAMOND_LEGGINGS) {
                        livingEntity.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_LEGGINGS));
                    }
                }
                case 22 -> {
                    org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_BOOTS);
                    boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back*/
                    boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                    if (livingEntity.getEquipment().getHelmet().getType() != org.bukkit.Material.DIAMOND_HELMET) {
                        livingEntity.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_HELMET));
                    }

                    if (livingEntity.getEquipment().getBoots().getType() != org.bukkit.Material.DIAMOND_BOOTS) {
                        livingEntity.getEquipment().setBoots(boots);
                    }
                }
                case 26 -> {
                    org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                    chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

                    if (livingEntity.getEquipment().getChestplate().getType() != org.bukkit.Material.NETHERITE_CHESTPLATE) {
                        livingEntity.getEquipment().setChestplate(chestplate);
                    }
                }
                case 29 -> {
                    org.bukkit.inventory.ItemStack leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                    leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

                    if (livingEntity.getEquipment().getLeggings().getType() != org.bukkit.Material.NETHERITE_LEGGINGS) {
                        livingEntity.getEquipment().setLeggings(leggings);
                    }
                }
                case 32 -> {
                    org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                    helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);

                    org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                    boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                    boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back*/
                    boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                    if (livingEntity.getEquipment().getHelmet().getType() != org.bukkit.Material.NETHERITE_HELMET) {
                        livingEntity.getEquipment().setHelmet(helmet);
                    }

                    if (livingEntity.getEquipment().getBoots().getType() != org.bukkit.Material.NETHERITE_BOOTS) {
                        livingEntity.getEquipment().setBoots(boots);
                    }
                }
                case 36 -> {
                    org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                    chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                    if (livingEntity.getEquipment().getChestplate().getType() != org.bukkit.Material.NETHERITE_CHESTPLATE) {
                        livingEntity.getEquipment().setChestplate(chestplate);
                    }
                }
                case 39 -> {
                    org.bukkit.inventory.ItemStack leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                    leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                    if (livingEntity.getEquipment().getLeggings().getType() != org.bukkit.Material.NETHERITE_LEGGINGS) {
                        livingEntity.getEquipment().setLeggings(leggings);
                    }
                }
                case 42 -> {
                    org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                    helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);

                    org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                    boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                    boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back*/
                    boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                    if (livingEntity.getEquipment().getHelmet().getType() != org.bukkit.Material.NETHERITE_HELMET) {
                        livingEntity.getEquipment().setHelmet(helmet);
                    }

                    if (livingEntity.getEquipment().getBoots().getType() != org.bukkit.Material.NETHERITE_BOOTS) {
                        livingEntity.getEquipment().setBoots(boots);
                    }
                }
                case 50 -> {
                    org.bukkit.inventory.ItemStack chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                    chestplate.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

                    if (livingEntity.getEquipment().getChestplate().getType() != org.bukkit.Material.NETHERITE_CHESTPLATE) {
                        livingEntity.getEquipment().setChestplate(chestplate);
                    }

                    org.bukkit.inventory.ItemStack leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                    leggings.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

                    if (livingEntity.getEquipment().getLeggings().getType() != org.bukkit.Material.NETHERITE_LEGGINGS) {
                        livingEntity.getEquipment().setLeggings(leggings);
                    }

                    org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                    helmet.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);

                    org.bukkit.inventory.ItemStack boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                    boots.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                    boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /**most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back*/
                    boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);

                    if (livingEntity.getEquipment().getHelmet().getType() != org.bukkit.Material.NETHERITE_HELMET) {
                        livingEntity.getEquipment().setHelmet(helmet);
                    }

                    if (livingEntity.getEquipment().getBoots().getType() != org.bukkit.Material.NETHERITE_BOOTS) {
                        livingEntity.getEquipment().setBoots(boots);
                    }
                }
            }
        }
    }
}
