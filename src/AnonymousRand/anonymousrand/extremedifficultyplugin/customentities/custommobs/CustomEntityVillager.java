package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs;

import net.minecraft.server.v1_16_R1.EntityTypes;
import net.minecraft.server.v1_16_R1.EntityVillager;
import net.minecraft.server.v1_16_R1.World;
import org.bukkit.entity.LivingEntity;

public class CustomEntityVillager extends EntityVillager {

    public CustomEntityVillager(World world) {
        super(EntityTypes.VILLAGER, world);
        this.dropChanceArmor[0] = 100; //to differentiate between vanilla villagers in listenermobspawnandreplace to avoid potential infinite loops
        this.setHealth(10.0F); /**villagers only have 10 health*/
        ((LivingEntity)this.getBukkitEntity()).setMaxHealth(10.0);
    }
}
