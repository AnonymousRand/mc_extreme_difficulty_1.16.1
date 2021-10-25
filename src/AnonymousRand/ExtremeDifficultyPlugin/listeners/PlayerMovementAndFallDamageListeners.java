package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Objects;

public class PlayerMovementAndFallDamageListeners implements Listener { /**fall damage starts counting at 2 blocks instead of 4 and water only negates 80% of fall damage*/
    public static HashMap<Player, double[]> arr = new HashMap<>(); //to keep track of fall heights: arr[0] y level of the previous tick, arr[1] is the y level of the current tick
    public static HashMap<Player, Double> fallHeight = new HashMap<>();
    public static HashMap<Player, Boolean> MLG = new HashMap<>();

    @EventHandler
    public void fallDamage(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        double y = event.getTo().getY();                                            //gets player's current y level

        if (!arr.containsKey(player)) {
            arr.put(player, new double[]{-65.0, -65.0});
            fallHeight.put(player, 0.0);
            MLG.put(player, false);
        }

        double[] arrTemp = arr.get(player);
        double fallHeightTemp = fallHeight.get(player);
        boolean MLGTemp = MLG.get(player);
        
        if (arrTemp[0] == -65.0) {                                                  //fill up arrTemp[0] first if it hasn't been yet
            arrTemp[0] = y;
            arr.replace(player, arrTemp);
            return;
        } else {
            arrTemp[1] = y;
        }

        if (arrTemp[1] < arrTemp[0]) {                                              //if the player's y level is lower than the previous tick's y level
            fallHeightTemp += arrTemp[0] - arrTemp[1];                              //update fallHeight
            arrTemp[0] = y;                                                         //set previous y level as the current y level in preparation for next tick
            arrTemp[1] = -65.0;                                                     //empty the current y level slot in preparation for next tick
        } else {                                                                    //fall ended or no change in y level
            Location loc = event.getTo();
            BlockData b = loc.getBlock().getBlockData();
            int level = -1;

            if (b instanceof Levelled) {                                            //get whether the block that the player currently is in is fluid
                Levelled lv = (Levelled)b;
                level = lv.getLevel();                                              //fluid level 0 = source block, 1-7 = non-falling, 8-15 = falling
            }

            if (level <= 7) {                                                       //if the current block is a solid block/air or a liquid source block or a non-falling liquid block
                if (fallHeightTemp >= 2.0 && !MLGTemp) {                            //if player fell for more than 2 blocks onto ground or already existing water (fall damage calculated starting at 2 blocks' fall instead of 4)
                    double damage = 0.0;

                    Material m = player.getLocation().getBlock().getType();
                    if (m == Material.WATER || m == Material.LAVA) {
                        damage += (Math.ceil(fallHeight.get(player) - 2.0)) * 0.2;  //water only reduces 80% of fall damage
                    } else {                                                        //otherwise if the player did not land in water calculate the damage based on how many blocks past 2 they fell (overrides the damage immunity from the default fall damage as this damage is 2 higher)
                        damage = Math.ceil(fallHeight.get(player) - 2.0);
                    }

                    player.damage(damage);
                } else if (MLGTemp) {                                               //do not double damage if the player MLG'ed as the MLG damage is already accounted for in the playerInteractEvent
                    MLGTemp = false;
                }

                fallHeightTemp = 0.0;                                               //reset fallHeight after the fall ends

                for (int i = 0; i < 2; i++) {                                       //reset the array after the fall ends
                    arrTemp[i] = -65.0;
                }
            } else {                                                                //else if the current block is a falling liquid block
                fallHeightTemp += arrTemp[0] - arrTemp[1];                          //do not reset fall height, and calculate as usual (reduce fall height if the player swam up)

                if (fallHeightTemp < 0.0) {
                    fallHeightTemp = 0.0;
                }

                arrTemp[0] = y;                                                     //set previous y level as the current y level in preparation for next tick
                arrTemp[1] = -65.0;                                                 //empty the current y level slot in preparation for next tick
            }                                                                       //this is to prevent loopholes such as dropping water down and swimming down it to avoid fall damage; with this current method, they will still get the damage when their "fall" ends at the bottom of the waterfall
        }

        arr.replace(player, arrTemp);
        fallHeight.replace(player, fallHeightTemp);
        MLG.replace(player, MLGTemp);
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
    public void playerMoveThroughGrass(PlayerMoveEvent event) {
        if (event.getPlayer().getLocation().getBlock().getType() == Material.GRASS || event.getPlayer().getLocation().getBlock().getType() == Material.TALL_GRASS) {
            if (event.getPlayer().getLocation().getBlock().getType() == Material.GRASS) {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 10, 0)); /**grass gives players slowness 1 for half a second*/
            } else {
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0)); /**tall grass gives players slowness 1 for 1 second and weakness 1 for 3 seconds*/
                event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0));
            }
        }
    }

    @EventHandler
    public void playerInShallowWater(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (player.getLocation().getBlock().getType() == Material.WATER && player.getEyeLocation().getBlock().getType() != Material.WATER) { /**player loses air even if they are standing in 1-deep water (but twice as fast, as the player is still inhaling as well), as long as they are moving (to prevent using shallow water to slow down mobs)*/
            player.setRemainingAir(player.getRemainingAir() - (player.getRemainingAir() <= 0 ? 5 : 8));

            if (player.getRemainingAir() <= -20) {
                player.setRemainingAir(0);
                player.damage(2.0);
                Bukkit.getPluginManager().callEvent(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.DROWNING, 2.0)); //fire event that would otherwise not be fired
            }
        }
    }
}
