package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.CustomEntityLargeFireball;
import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityPiglin;
import net.minecraft.server.v1_16_R1.EnumItemSlot;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.Items;
import net.minecraft.server.v1_16_R1.PiglinAI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.Random;

public class PlayerInteractListeners implements Listener {

    Random rand = new Random();

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.hasBlock()) {
            if (event.getClickedBlock().getType().equals(Material.CHEST) || event.getClickedBlock().getType().equals(Material.BARREL) || event.getClickedBlock().getType().equals(Material.DISPENSER) || event.getClickedBlock().getType().equals(Material.DROPPER) || event.getClickedBlock().getType().equals(Material.ENDER_CHEST) || event.getClickedBlock().getType().equals(Material.HOPPER) || event.getClickedBlock().getType().equals(Material.CHEST_MINECART) || event.getClickedBlock().getType().equals(Material.HOPPER_MINECART) || event.getClickedBlock().getType().equals(Material.SHULKER_BOX) || event.getClickedBlock().getType().equals(Material.TRAPPED_CHEST)) {
                Player player = event.getPlayer();
                Location loc = player.getLocation();
                CustomEntityPiglin piglin = new CustomEntityPiglin(((CraftWorld)player.getWorld()).getHandle());
                piglin.setPositionRotation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                piglin.setSlot(EnumItemSlot.MAINHAND, this.rand.nextDouble() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD)); //give piglin a sword or crossbow
                PiglinAI.a(piglin); //code from onInitialSpawn
                ((CraftWorld)player.getWorld()).getHandle().addEntity(piglin, CreatureSpawnEvent.SpawnReason.NATURAL);
            }
        }
    }
}
