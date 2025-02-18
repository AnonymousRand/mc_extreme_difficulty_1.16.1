package AnonymousRand.anonymousrand.plugin.listeners;

import AnonymousRand.anonymousrand.plugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.CustomEntityPiglin;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
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
            Material bukkitMaterial = bukkitBlock.getType();
            Player bukkitPlayer = event.getPlayer();
            EntityPlayer nmsPlayer = ((CraftPlayer)bukkitPlayer).getHandle();
            Location bukkitLoc = bukkitPlayer.getLocation();
            World nmsWorld = ((CraftWorld)bukkitPlayer.getWorld()).getHandle();
            boolean containerBlock = bukkitMaterial == Material.CHEST || bukkitMaterial == Material.BARREL || bukkitMaterial == Material.DISPENSER || bukkitMaterial == Material.DROPPER || bukkitMaterial == Material.ENDER_CHEST || bukkitMaterial == Material.HOPPER || bukkitMaterial == Material.CHEST_MINECART || bukkitMaterial == Material.HOPPER_MINECART || bukkitMaterial == Material.SHULKER_BOX || bukkitMaterial == Material.TRAPPED_CHEST;

            if (action == Action.LEFT_CLICK_BLOCK || action == Action.RIGHT_CLICK_BLOCK) {
                if (action == Action.LEFT_CLICK_BLOCK) {
                    if (bukkitMaterial == Material.SPAWNER) { /* attempting to mine a spawner gives mining fatigue 3 for 15 seconds */
                        bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_DIGGING, 300, 2));
                    }
                } else {
                    if (containerBlock) { /* right-clicking these blocks spawns a piglin and causes all piglins within 40 blocks horizontally to go into a frenzy for 10 seconds */
                        CustomEntityPiglin newPiglin = new CustomEntityPiglin(nmsWorld);
                        new SpawnEntity(nmsWorld, newPiglin, 1, null, bukkitLoc, true);
                        newPiglin.setSlot(EnumItemSlot.MAINHAND, random.nextDouble() < 0.5 ? new ItemStack(Items.CROSSBOW) : new ItemStack(Items.GOLDEN_SWORD)); // give piglin a sword or crossbow
                        PiglinAI.a(newPiglin); // code from onInitialSpawn

                        nmsWorld.getEntities(nmsPlayer, nmsPlayer.getBoundingBox().grow(40.0, 128.0, 40.0), entity -> entity instanceof CustomEntityPiglin).forEach(entity -> ((CustomEntityPiglin)entity).frenzyTicks += 200);
                    }

                    if (bukkitMaterial == Material.ANVIL || bukkitMaterial == Material.CHIPPED_ANVIL || bukkitMaterial == Material.DAMAGED_ANVIL || bukkitMaterial == Material.SMITHING_TABLE) { /* right-clicking an anvil or smithing table causes it to explode 10 seconds later */
                        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> {
                            nmsWorld.createExplosion(null, bukkitBlock.getLocation().getX(), bukkitBlock.getLocation().getY(), bukkitBlock.getLocation().getZ(), 4.0F, true, Explosion.Effect.NONE);
                            bukkitBlock.setType(Material.AIR);
                        }, 200L);
                    }

                    if (bukkitPlayer.getEquipment().getItemInMainHand().getType() == Material.FLINT_AND_STEEL && bukkitPlayer.getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) { /* blocks that produce a lot of light can't be placed in the end to prevent exploiting them to deactivate spawners */
                        event.setCancelled(true);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + bukkitPlayer.getName() + " \"You can't place such bright blocks in the end\"");
                    }
                }
            }
        }
    }

    @EventHandler
    public void playerPlaceLavaBucket(PlayerBucketEmptyEvent event) {
        if (event.getBucket() == Material.LAVA_BUCKET && event.getPlayer().getWorld().getEnvironment() == org.bukkit.World.Environment.THE_END) { /* blocks that produce a lot of light can't be placed in the end to prevent exploiting them to deactivate spawners */
            event.setCancelled(true);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + event.getPlayer().getName() + " \"You can't place such bright blocks in the end\"");
        }
    }
}
