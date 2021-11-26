package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class ListenerPlayerMovementAndFallDamage implements Listener { /**fall damage starts counting at 2 blocks instead of 4 and water only negates 80% of fall damage and feather falling does nothing if you MLG water*/
    public static HashMap<Player, double[]> arr = new HashMap<>(); //to keep track of fall heights: arr[0] y level of the previous tick, arr[1] is the y level of the current tick
    public static HashMap<Player, Double> fallHeight = new HashMap<>();
    public static HashMap<Player, Boolean> MLG = new HashMap<>();

    @EventHandler
    public void fallDamage(PlayerMoveEvent event) {
        Player bukkitPlayer = event.getPlayer();
        double y = event.getTo().getY();                                            //gets player's current y level

        if (!arr.containsKey(bukkitPlayer)) {
            arr.put(bukkitPlayer, new double[]{-65.0, -65.0});
            fallHeight.put(bukkitPlayer, 0.0);
            MLG.put(bukkitPlayer, false);
        }

        double[] arrTemp = arr.get(bukkitPlayer);
        double fallHeightTemp = fallHeight.get(bukkitPlayer);
        boolean MLGTemp = MLG.get(bukkitPlayer);
        
        if (arrTemp[0] == -65.0) {                                                  //fill up arrTemp[0] first if it hasn't been yet
            arrTemp[0] = y;
            arr.replace(bukkitPlayer, arrTemp);
            return;
        } else {
            arrTemp[1] = y;
        }

        if (arrTemp[1] < arrTemp[0]) {                                              //if the player's y level is lower than the previous tick's y level
            fallHeightTemp += arrTemp[0] - arrTemp[1];                              //update fallHeight
            arrTemp[0] = y;                                                         //set previous y level as the current y level in preparation for next tick
            arrTemp[1] = -65.0;                                                     //empty the current y level slot in preparation for next tick
        } else {                                                                    //fall ended or no change in y level

            if (fallHeightTemp == 0.0) {                                            //avoid lag from all the calculations if player is not going to be taking damage
                arr.replace(bukkitPlayer, new double[]{-65.0, -65.0});
                return;
            }

            MLG.replace(bukkitPlayer, false);                                             //do not double damage if the player MLG'ed as the MLG damage is already accounted for in the playerInteractEvent

            Location loc = event.getTo();
            BlockData b = loc.getBlock().getBlockData();
            int level = -1;

            if (b instanceof Levelled) {                                            //get whether the block that the player currently is in is fluid
                Levelled lv = (Levelled)b;
                level = lv.getLevel();                                              //fluid level 0 = source block, 1-7 = non-falling, 8-15 = falling
            } else {
                if (fallHeightTemp < 2.1) {                                         //if player is not swimming up/inside a fluid and is not going to be taking damage
                    arr.replace(bukkitPlayer, new double[]{-65.0, -65.0});
                    fallHeight.replace(bukkitPlayer, 0.0);
                    return;
                }
            }

            if (level <= 7) {                                                       //if the current block is a solid block/air or a liquid source block or a non-falling liquid block
                if (!MLGTemp && fallHeightTemp >= 2.1) {                            //if player fell for more than 2.1 blocks onto ground or already existing water (fall damage calculated starting at 2 blocks' fall instead of 4)
                    double damage = 0.0;

                    Material m = bukkitPlayer.getLocation().getBlock().getType();
                    if (m == Material.WATER || m == Material.LAVA) {
                        damage += (Math.ceil(fallHeight.get(bukkitPlayer) - 2.0)) * 0.2;  //water only reduces 80% of fall damage
                    } else {                                                        //otherwise if the player did not land in water calculate the damage based on how many blocks past 2 they fell (overrides the damage immunity from the default fall damage as this damage is 2 higher)
                        damage = Math.ceil(fallHeight.get(bukkitPlayer) - 2.0);
                    }

                    bukkitPlayer.damage(damage);
                }

                for (int i = 0; i < 2; i++) {                                       //reset the array after the fall ends
                    arrTemp[i] = -65.0;
                }

                fallHeightTemp = 0.0;                                               //reset fallHeight after the fall ends
            } else {                                                                //else if the current block is a falling liquid block
                fallHeightTemp += arrTemp[0] - arrTemp[1];                          //do not reset fall height, and calculate as usual (reduce fall height if the player swam up)

                if (fallHeightTemp < 0.0) {
                    fallHeightTemp = 0.0;
                }

                arrTemp[0] = y;                                                     //set previous y level as the current y level in preparation for next tick
                arrTemp[1] = -65.0;                                                 //empty the current y level slot in preparation for next tick
            }                                                                       //this is to prevent loopholes such as dropping water down and swimming down it to avoid fall damage; with this current method, they will still get the damage when their "fall" ends at the bottom of the waterfall
        }

        arr.replace(bukkitPlayer, arrTemp);
        fallHeight.replace(bukkitPlayer, fallHeightTemp);
    }

    @EventHandler
    public void playerMLG(PlayerBucketEmptyEvent event) {
        if (fallHeight.getOrDefault(event.getPlayer(), 0.0) >= 2.0) { //when the player empties a bucket and they fell for more than 2 blocks and haven't hit the ground yet (so if they MLG water'ed/lava'ed)
            Player player = event.getPlayer();
            double damage = 0.0;

            damage += Math.ceil(fallHeight.get(player)) * 0.2;      //assume that the player right clicks the water about 2 blocks before hitting ground (so it is -0 instead of -2); do 20% of the damage they would have taken if they hadn't landed the MLG (basically so water only reduces 80% of fall damage)
            event.getPlayer().damage(damage);
            fallHeight.replace(player, 0.0);                        //reset fallHeight as the MLG has been landed, meaning the fall ended
            MLG.replace(player, true);
        }
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        Player bukkitPlayer = event.getPlayer();
        Material type = bukkitPlayer.getLocation().getBlock().getType();

        if (type == Material.GRASS || type == Material.TALL_GRASS) {
            if (type == Material.GRASS) {
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 0)); /**grass gives players slowness 1 for half a second*/
            } else {
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0)); /**tall grass gives players slowness 1 for 1 second and weakness 1 for 3 seconds*/
                bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
            }
        }

        if (type == Material.WATER && bukkitPlayer.getEyeLocation().getBlock().getType() != Material.WATER && !bukkitPlayer.isInvulnerable() && !bukkitPlayer.isInsideVehicle()) { /**player loses air even if they are standing in 1-deep water (but twice as fast, as the player is still inhaling as well), as long as they are moving (to prevent using shallow water to slow down mobs)*/
            bukkitPlayer.setRemainingAir(bukkitPlayer.getRemainingAir() - (bukkitPlayer.getRemainingAir() <= 0 ? 5 : 8));

            if (bukkitPlayer.getRemainingAir() <= -20) {
                bukkitPlayer.setRemainingAir(0);
                bukkitPlayer.damage(2.0);
                Bukkit.getPluginManager().callEvent(new EntityDamageEvent(bukkitPlayer, EntityDamageEvent.DamageCause.DROWNING, 2.0)); //fire event that would otherwise not be fired
            }
        }
    }
}
