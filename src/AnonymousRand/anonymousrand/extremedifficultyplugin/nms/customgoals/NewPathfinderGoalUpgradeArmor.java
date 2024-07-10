package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals;

import net.minecraft.server.v1_16_R1.EntityInsentient;
import net.minecraft.server.v1_16_R1.PathfinderGoal;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;

public class NewPathfinderGoalUpgradeArmor extends PathfinderGoal {

    private final EntityInsentient nmsEntity;
    private final LivingEntity bukkitEntity;
    private final HashMap<Integer, Boolean> attackBooleans = new HashMap<>();
    private Field attacks;

    public NewPathfinderGoalUpgradeArmor(EntityInsentient nmsEntity) {
        this.nmsEntity = nmsEntity;
        this.bukkitEntity = ((LivingEntity) this.nmsEntity.getBukkitEntity());
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
        Arrays.fill(this.nmsEntity.dropChanceArmor, 0.0f);  /* entity can't drop the items in their armor slots */

        try {
            this.attacks = this.nmsEntity.getClass().getDeclaredField("attacks");
            this.attacks.setAccessible(true);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean a() {
        int attacksLocal;

        try {
            attacksLocal = this.attacks.getInt(this.nmsEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }

        if (this.attackBooleans.containsKey(attacksLocal)) {
            if (!this.attackBooleans.get(attacksLocal)) {
                this.attackBooleans.replace(attacksLocal, true);
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
        int attacksLocal;
        EntityEquipment bukkitEquipment = this.bukkitEntity.getEquipment();
        org.bukkit.inventory.ItemStack helmet = new org.bukkit.inventory.ItemStack(Material.IRON_HELMET),
                chestplate = new org.bukkit.inventory.ItemStack(Material.IRON_CHESTPLATE),
                leggings = new org.bukkit.inventory.ItemStack(Material.IRON_LEGGINGS),
                boots = new org.bukkit.inventory.ItemStack(Material.IRON_BOOTS);

        try {
            attacksLocal = this.attacks.getInt(this.nmsEntity);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return;
        }

        switch (attacksLocal) {
            case 4:
                bukkitEquipment.setChestplate(chestplate);
                break;
            case 8:
                bukkitEquipment.setLeggings(leggings);
                break;
            case 11:
                bukkitEquipment.setHelmet(helmet);

                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /* most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);
                bukkitEquipment.setBoots(boots);
                break;
            case 15:
                bukkitEquipment.setChestplate(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_CHESTPLATE));
                break;
            case 19:
                bukkitEquipment.setLeggings(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_LEGGINGS));
                break;
            case 22:
                bukkitEquipment.setHelmet(new org.bukkit.inventory.ItemStack(org.bukkit.Material.DIAMOND_HELMET));

                boots = new org.bukkit.inventory.ItemStack(Material.DIAMOND_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /* most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);
                bukkitEquipment.setBoots(boots);
                break;
            case 26:
                chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                bukkitEquipment.setChestplate(chestplate);
                break;
            case 29:
                leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                leggings.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                bukkitEquipment.setLeggings(leggings);
                break;
            case 32:
                helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                helmet.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                bukkitEquipment.setHelmet(helmet);

                boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 1);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /* most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);
                bukkitEquipment.setBoots(boots);
                break;
            case 36:
                chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                bukkitEquipment.setChestplate(chestplate);
                break;
            case 39:
                leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                leggings.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                bukkitEquipment.setLeggings(leggings);
                break;
            case 42:
                helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                helmet.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                bukkitEquipment.setHelmet(helmet);

                boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 3);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /* most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 255);
                bukkitEquipment.setBoots(boots);
                break;
            case 50:
                chestplate = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_CHESTPLATE);
                chestplate.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                bukkitEquipment.setChestplate(chestplate);

                leggings = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_LEGGINGS);
                leggings.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                bukkitEquipment.setLeggings(leggings);

                helmet = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_HELMET);
                helmet.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                bukkitEquipment.setHelmet(helmet);

                boots = new org.bukkit.inventory.ItemStack(org.bukkit.Material.NETHERITE_BOOTS);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.PROTECTION_ENVIRONMENTAL, 4);
                boots.addEnchantment(org.bukkit.enchantments.Enchantment.DEPTH_STRIDER, 3); /* most mobs spawn with depth strider 3 to avoid loopholes such as using water flow to keep them back */
                boots.addUnsafeEnchantment(Enchantment.DURABILITY, 255);
                bukkitEquipment.setBoots(boots);
                break;

        }
    }
}
