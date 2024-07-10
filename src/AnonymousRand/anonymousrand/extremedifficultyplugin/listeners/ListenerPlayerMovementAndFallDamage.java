package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;

public class ListenerPlayerMovementAndFallDamage implements Listener { /* fall damage starts counting at 2 blocks instead of 4 and water only negates 45-80% of fall damage and feather falling does nothing */
    public static HashMap<Player, Double> fallHeight = new HashMap<>();

    @EventHandler
    public void fallDamage(PlayerMoveEvent event) {
        Player bukkitPlayer = event.getPlayer();
        double from = event.getFrom().getY(), to = event.getTo().getY();
        double fallHeightTmp = fallHeight.get(bukkitPlayer);

        if ((from == to || bukkitPlayer.isOnGround()) && fallHeightTmp <= 2.0) { // not falling/hit the ground but has not fallen far enough to take damage
            fallHeight.replace(bukkitPlayer, 0.0);
            return;
        }

        if (to < from && !bukkitPlayer.isOnGround()) { // still falling; sometimes fall damage doesn't register as the player just hit the ground and still moved down relative to the last so that's why I prevent players on the ground from exiting the method without taking damage here
            fallHeight.replace(bukkitPlayer, fallHeightTmp + (from - to));
            return;
        }

        // otherwise if stopped falling (to == from and will be taking damage)/is moving upwards
        BlockData b = event.getTo().getBlock().getBlockData();
        int level = 16; // levels: 0 = fluid source block, 1-7 = fluid non-source block, 8-15 = falling fluid block; 16 (artificially designated here) = non-fluid block

        if (b instanceof Levelled) { // if the block that the player currently is in is fluid
            level = ((Levelled)b).getLevel();
        }

        if (to > from) { // if moving upwards
            if (level == 16) { // jumping/climbing upwards; not in a fluid
                if (fallHeightTmp <= 2.0) { // sometimes a jump starts right after a fall, so this must be accounted for
                    fallHeight.replace(bukkitPlayer, 0.0);
                    return;
                }
            } else { // swimming up in a vertical stream of fluid
                fallHeight.replace(bukkitPlayer, Math.max(0.0, fallHeightTmp - (to - from)));
                return;
            }
        }

        // otherwise if stopped falling (onto ground or existing water) and will be taking damage/just fell and is immediately jumping again
        bukkitPlayer.damage((fallHeightTmp - 2.0) * ((level * 0.05) + 0.2)); // formula for fall damage is "(fallHeight - 2.0) * ((level * 0.05) + 0.2))" so source blocks cause only (0 * 0.05) + 0.2 which is 20% of the original damage to be taken, and each block away from the source there is 5% less damage reduction up to 55% of the original damage; this allows 16 to be the value for falling onto no water as this would equate to a 0% fall damage reduction
        fallHeight.replace(bukkitPlayer, 0.0);
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        Player bukkitPlayer = event.getPlayer();
        Material bukkitMaterial = bukkitPlayer.getLocation().getBlock().getType();

        if (bukkitMaterial == Material.GRASS || bukkitMaterial == Material.TALL_GRASS) {
            if (bukkitMaterial == Material.GRASS) {
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 0)); /* grass gives players slowness 1 for half a second */
            } else {
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0)); /* tall grass gives players slowness 1 for 1 second and weakness 1 for 3 seconds */
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
            }

            return;
        }

        if (bukkitMaterial == Material.WATER && bukkitPlayer.getEyeLocation().getBlock().getType() != Material.WATER && !bukkitPlayer.isInvulnerable() && !bukkitPlayer.isInsideVehicle()) { /* player loses air even if they are standing in 1-deep water (but twice as fast, as the player is still inhaling as well), as long as they are moving (to prevent using shallow water to slow down mobs) */
            bukkitPlayer.setRemainingAir(bukkitPlayer.getRemainingAir() - (bukkitPlayer.getRemainingAir() <= 0 ? 5 : 8));

            if (bukkitPlayer.getRemainingAir() <= -20) {
                bukkitPlayer.setRemainingAir(0);
                bukkitPlayer.damage(2.0);
                Bukkit.getPluginManager().callEvent(new EntityDamageEvent(bukkitPlayer, EntityDamageEvent.DamageCause.DROWNING, 2.0)); // fire event that would otherwise not be fired
            }

            return;
        }

        if (bukkitMaterial == Material.SOUL_FIRE) { /* soul fire sets players on fire for 10 minutes */
            bukkitPlayer.setFireTicks(12000);
        }
    }
}