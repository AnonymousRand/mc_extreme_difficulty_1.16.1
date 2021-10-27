package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityPiglin;
import net.minecraft.server.v1_16_R1.EnumItemSlot;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.Items;
import net.minecraft.server.v1_16_R1.PiglinAI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class PlayerInteractListeners implements Listener {

    private final Random rand = new Random();

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.hasBlock()) {
            if (event.getClickedBlock().getType() == Material.CHEST || event.getClickedBlock().getType() == Material.BARREL || event.getClickedBlock().getType() == Material.DISPENSER || event.getClickedBlock().getType() == Material.DROPPER || event.getClickedBlock().getType() == Material.ENDER_CHEST || event.getClickedBlock().getType() == Material.HOPPER || event.getClickedBlock().getType() == Material.CHEST_MINECART || event.getClickedBlock().getType() == Material.HOPPER_MINECART || event.getClickedBlock().getType() == Material.SHULKER_BOX || event.getClickedBlock().getType() == Material.TRAPPED_CHEST) {
                Player player = event.getPlayer();
                Location loc = player.getLocation();
                CustomEntityPiglin piglin = new CustomEntityPiglin(((CraftWorld)player.getWorld()).getHandle());
                piglin.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                piglin.setSlot(EnumItemSlot.MAINHAND, this.rand.nextDouble() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD)); //give piglin a sword or crossbow
                PiglinAI.a(piglin); //code from onInitialSpawn
                ((CraftWorld)player.getWorld()).getHandle().addEntity(piglin, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }

        if (event.getAction() == Action.LEFT_CLICK_BLOCK && event.hasBlock()) {
            if (event.getClickedBlock().getType() == Material.SPAWNER) { /**attempting to mine a spawner gives mining fatigue 2 for 7.5 seconds*/
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 150, 2));
            }

            if (event.getClickedBlock().getType() == Material.OBSIDIAN || event.getClickedBlock().getType() == Material.CRYING_OBSIDIAN || event.getClickedBlock().getType() == Material.ANCIENT_DEBRIS || event.getClickedBlock().getType() == Material.NETHERITE_BLOCK) { /**attempting to mine these blocks gives the player mining fatigue 1 for 30 seconds*/
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 600, 0));
            }
        }
    }
}
