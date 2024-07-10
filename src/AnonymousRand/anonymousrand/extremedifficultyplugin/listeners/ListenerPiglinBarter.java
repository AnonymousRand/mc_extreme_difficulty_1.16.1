package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.CustomEntityPiglin;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;

public class ListenerPiglinBarter implements Listener {

    @EventHandler
    public void piglinBarter(EntityDropItemEvent event) {
        if (((CraftEntity)event.getEntity()).getHandle() instanceof CustomEntityPiglin) { /* trading increases a piglin's attacks by 1 */
            CustomEntityPiglin piglin = (CustomEntityPiglin)((CraftEntity)event.getEntity()).getHandle();
            piglin.increaseAttacks(1);
            piglin.setHealth((float) (piglin.getHealth() + 0.75)); /* piglins heal by 0.75 every time its attacks increase by 1 */
        }
    }
}