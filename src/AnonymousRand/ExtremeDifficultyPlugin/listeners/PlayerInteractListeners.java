package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customEntities.customMobs.CustomEntityPiglin;
import net.minecraft.server.v1_16_R1.EnumItemSlot;
import net.minecraft.server.v1_16_R1.ItemStack;
import net.minecraft.server.v1_16_R1.Items;
import net.minecraft.server.v1_16_R1.PiglinAI;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
        if (event.hasBlock()) {
            Material type = event.getClickedBlock().getType();
            Player bukkitPlayer = event.getPlayer();
            Location loc = bukkitPlayer.getLocation();
            boolean containerBlock = type == Material.CHEST || type == Material.BARREL || type == Material.DISPENSER || type == Material.DROPPER || type == Material.ENDER_CHEST || type == Material.HOPPER || type == Material.CHEST_MINECART || type == Material.HOPPER_MINECART || type == Material.SHULKER_BOX || type == Material.TRAPPED_CHEST;

            if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (type == Material.SPAWNER) { /**attempting to mine a spawner gives mining fatigue 2 for 7.5 seconds*/
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 150, 2));
                }

                if (type == Material.OBSIDIAN || type == Material.CRYING_OBSIDIAN || type == Material.ANCIENT_DEBRIS || type == Material.NETHERITE_BLOCK) { /**attempting to mine these blocks gives the player mining fatigue 1 for 30 seconds*/
                    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 600, 0));
                }

                if (containerBlock) { /**mining or right-clicking these blocks spawns a piglin*/
                    CustomEntityPiglin piglin = new CustomEntityPiglin(((CraftWorld) bukkitPlayer.getWorld()).getHandle());
                    piglin.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    piglin.setSlot(EnumItemSlot.MAINHAND, this.rand.nextDouble() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD)); //give piglin a sword or crossbow
                    PiglinAI.a(piglin); //code from onInitialSpawn
                    ((CraftWorld) bukkitPlayer.getWorld()).getHandle().addEntity(piglin, CreatureSpawnEvent.SpawnReason.NATURAL);
                }
            } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (containerBlock) { /**mining or right-clicking these blocks spawns a piglin*/
                    CustomEntityPiglin piglin = new CustomEntityPiglin(((CraftWorld) bukkitPlayer.getWorld()).getHandle());
                    piglin.setPosition(loc.getX(), loc.getY(), loc.getZ());
                    piglin.setSlot(EnumItemSlot.MAINHAND, this.rand.nextDouble() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD)); //give piglin a sword or crossbow
                    PiglinAI.a(piglin); //code from onInitialSpawn
                    ((CraftWorld) bukkitPlayer.getWorld()).getHandle().addEntity(piglin, CreatureSpawnEvent.SpawnReason.NATURAL);
                }
            }
        }
    }
}
