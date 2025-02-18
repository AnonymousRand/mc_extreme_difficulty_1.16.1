package AnonymousRand.anonymousrand.plugin.nms.customgoals;

import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntitySilverfish;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.util.IAttackLevelingMob;
import AnonymousRand.anonymousrand.plugin.nms.customentities.projectiles.CustomEntityArrow;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class CustomPathfinderGoalGetBuffedByMobs extends PathfinderGoal {

    public EntityInsentient goalOwner;
    private boolean hunger, unluck;

    private final World nmsWorld;
    private static final Random random = new Random();

    public CustomPathfinderGoalGetBuffedByMobs(EntityInsentient goalOwner) {
        this.goalOwner = goalOwner;
        this.hunger = false;
        this.unluck = false;
        this.nmsWorld = goalOwner.getWorld();
    }

    @Override
    public boolean a() {
        this.hunger = false;
        this.unluck = false;

        if (this.goalOwner.hasEffect(MobEffects.HUNGER)) {
            if (this.goalOwner.getEffect(MobEffects.HUNGER).getAmplifier() >= 252 && this.goalOwner.getGoalTarget() != null) {
                this.hunger = true;
            }
        }

        if (this.goalOwner.hasEffect(MobEffects.UNLUCK)) {
            if (this.goalOwner.getEffect(MobEffects.UNLUCK).getAmplifier() >= 253 && this.goalOwner.getGoalTarget() != null) {
                this.unluck = true;
            }
        }

        return this.hunger || this.unluck;
    }

    @Override
    public boolean b() {
        return this.a();
    }

    @Override
    public void e() {
        if (this.hunger) { // bat buff
            int amplifier = this.goalOwner.getEffect(MobEffects.HUNGER).getAmplifier();

            if (this.goalOwner.ticksLived % (amplifier == 252 ? 40 : amplifier == 253 ? 30 : 20) == 0) {
                this.shootArrows();
            }

            if (amplifier > 252 && this.goalOwner.ticksLived % 300 == 0) {
                new SpawnEntity(this.nmsWorld, new CustomEntitySilverfish(this.nmsWorld), 1, null, null, this.goalOwner, false, true); // todo nametag
            }
        }

        if (this.unluck) { // piglin buff
            int amplifier = this.goalOwner.getEffect(MobEffects.UNLUCK).getAmplifier();
            this.goalOwner.removeEffect(MobEffects.UNLUCK);

            if (this.goalOwner instanceof IAttackLevelingMob) {
                switch (amplifier) {
                    case 253:
                        ((IAttackLevelingMob) this.goalOwner).increaseAttacks(5);
                        break;
                    case 254:
                        ((IAttackLevelingMob) this.goalOwner).increaseAttacks(5);
                        this.setGoldEquipment();
                        break;
                }
            }
        }
    }

    public void shootArrows() {
        CustomEntityArrow entityArrow = new CustomEntityArrow(this.nmsWorld);
        entityArrow.setShooter(this.goalOwner);
        Vec3D pos = this.goalOwner.getPositionVector();
        Vec3D lookDirection = this.goalOwner.getGoalTarget().getPositionVector().d(pos).d(); // d(pos) is subtract, d() is normalize

        double d0 = lookDirection.getX();
        double d1 = lookDirection.getY() + random.nextDouble() * 0.01 + 0.01;
        double d2 = lookDirection.getZ();
        double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);

        if (random.nextDouble() <= 0.2) { /* 20% of arrows shot are piercing 1 */
            entityArrow.setPierceLevel((byte)1);
        }

        entityArrow.setPosition(pos.getX() + lookDirection.getX() * 0.5, pos.getY() + 1.8, pos.getZ() + lookDirection.getZ() * 0.5);
        entityArrow.shoot(d0, d1 + d3 * 0.2, d2, 1.25F, 0.0F); /* arrows have no inaccuracy and move slightly slower */
        this.goalOwner.playSound(SoundEffects.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        this.nmsWorld.addEntity(entityArrow);
    }

    public void setGoldEquipment() {
        LivingEntity livingEntity = ((LivingEntity) this.goalOwner.getBukkitEntity()); // use bukkitEntities instead as nms entities seems to require sending out packets and stuff before it works

        if (livingEntity.getEquipment().getHelmet().getType() == org.bukkit.Material.AIR) {
            livingEntity.getEquipment().setHelmet(new org.bukkit.inventory.ItemStack(Material.GOLDEN_HELMET));
        }

        if (livingEntity.getEquipment().getChestplate().getType() == org.bukkit.Material.AIR) {
            livingEntity.getEquipment().setChestplate(new org.bukkit.inventory.ItemStack(Material.GOLDEN_CHESTPLATE));
        }

        if (livingEntity.getEquipment().getLeggings().getType() == org.bukkit.Material.AIR) {
            livingEntity.getEquipment().setLeggings(new org.bukkit.inventory.ItemStack(Material.GOLDEN_LEGGINGS));
        }

        if (livingEntity.getEquipment().getItemInMainHand().getType() == org.bukkit.Material.AIR) {
            livingEntity.getEquipment().setItemInMainHand(new org.bukkit.inventory.ItemStack(Material.GOLDEN_SWORD));
        }
    }
}
