package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityPiglin;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.StaticPlugin;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class ListenerPlayerInteract implements Listener {

    private static final Random random = new Random();

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if (event.hasBlock()) {
            Action action = event.getAction();
            Block bukkitBlock = event.getClickedBlock();
            Material type = bukkitBlock.getType();
            Player bukkitPlayer = event.getPlayer();
            EntityPlayer nmsPlayer = ((CraftPlayer)bukkitPlayer).getHandle();
            Location loc = bukkitPlayer.getLocation();
            World nmsWorld = ((CraftWorld)bukkitPlayer.getWorld()).getHandle();
            boolean containerBlock = type == Material.CHEST || type == Material.BARREL || type == Material.DISPENSER || type == Material.DROPPER || type == Material.ENDER_CHEST || type == Material.HOPPER || type == Material.CHEST_MINECART || type == Material.HOPPER_MINECART || type == Material.SHULKER_BOX || type == Material.TRAPPED_CHEST;

            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    if (type == Material.SPAWNER) { /**attempting to mine a spawner gives mining fatigue 3 for 16 seconds*/
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 320, 2));
                    }
                } else {
                    if (containerBlock) { /**right-clicking these blocks spawns a piglin and causes all piglins within 40 blocks horizontally to go into a frenzy for 10 seconds*/
                        CustomEntityPiglin newPiglin = new CustomEntityPiglin(nmsWorld);
                        new SpawnEntity(nmsWorld, newPiglin, 1, null, loc, true);
                        newPiglin.setSlot(EnumItemSlot.MAINHAND, random.nextDouble() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD)); //give piglin a sword or crossbow
                        PiglinAI.a(newPiglin); //code from onInitialSpawn

                        nmsWorld.getEntities(nmsPlayer, nmsPlayer.getBoundingBox().grow(40.0, 128.0, 40.0), entity -> entity instanceof CustomEntityPiglin).forEach(entity -> {
                            ((CustomEntityPiglin)entity).veryAngryTicks += 200;
                        });
                    }

                    if (type == Material.ANVIL || type == Material.CHIPPED_ANVIL || type == Material.DAMAGED_ANVIL || type == Material.SMITHING_TABLE) { /**right-clicking an anvil or smithing table causes it to explode 10 seconds later*/
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(StaticPlugin.plugin, () -> {
                            nmsWorld.createExplosion(null, bukkitBlock.getLocation().getX(), bukkitBlock.getLocation().getY(), bukkitBlock.getLocation().getZ(), 4.0F, true, Explosion.Effect.NONE);
                            bukkitBlock.setType(Material.AIR);
                        }, 200L);
                    }

                    if (bukkitPlayer.getEquipment().getItemInMainHand().getType() == Material.FLINT_AND_STEEL && bukkitPlayer.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) { /**blocks that produce a lot of light can't be placed in the end to prevent exploiting them to deactivate spawners*/
                        event.setCancelled(true);
                        Bukkit.broadcastMessage("You can't place such bright blocks in the end");
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerPlaceLavaBucket(PlayerBucketEmptyEvent event) {
        if (event.getBucket() == Material.LAVA_BUCKET && event.getPlayer().getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) { /**blocks that produce a lot of light can't be placed in the end to prevent exploiting them to deactivate spawners*/
            event.setCancelled(true);
            Bukkit.broadcastMessage("You can't place such bright blocks in the end");
        }
    }
}
