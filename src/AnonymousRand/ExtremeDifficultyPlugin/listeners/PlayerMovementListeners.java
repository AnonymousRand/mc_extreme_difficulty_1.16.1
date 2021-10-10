package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import net.minecraft.server.v1_16_R1.Items;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class PlayerMovementListeners implements Listener {

    public double[] arr; //to keep track of fall heights: arr[0] y level of the previous tick, arr[1] is the y level of the current tick
    public double fallHeight;
    public boolean MLG;

    public PlayerMovementListeners() {
        this.arr = new double[]{-65.0, -65.0};
        this.fallHeight = 0.0;
        this.MLG = false;
    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        double y = event.getTo().getY(); //gets player's current y level

        if (arr[0] == -65.0) {                      //fill up arr[0] first if it hasn't been yet
            arr[0] = y;
            return;
        } else {
            arr[1] = y;
        }

        if (arr[1] < arr[0]) {                      //if the player's y level is lower than the previous tick's y level
            this.fallHeight += arr[0] - arr[1];          //update fallHeight
            arr[0] = y;                             //set previous y level as the current y level in preparation for next tick
            arr[1] = -65.0;                         //empty the current y level slot in preparation for next tick
        } else {                                    //fall ended or no change in y-level
            if (this.fallHeight >= 2.0 && !this.MLG) {                            //if player fell for more than 2 blocks onto ground or already existing water (fall damage calculated starting at 2 blocks' fall instead of 4)
                double damage = 0.0;

                Material m = event.getPlayer().getLocation().getBlock().getType();
                if (m == Material.WATER) {
                    damage += (Math.ceil(this.fallHeight - 2.0)) * 0.2;         //water only reduces 80% of fall damage
                    this.fallHeight = 0.0;                                       //reset fallHeight as the player is in water, meaning the fall ended
                } else {
                    damage = Math.ceil(this.fallHeight - 2.0);                  //otherwise if the player did not land in water calculate the damage based on how many blocks past 2 they fell (overrides the damage immunity from the default fall damage as this damage is 2 higher)
                }

                event.getPlayer().damage(damage);
            } else if (this.MLG) {                                               //do not double damage if the player MLG'ed as the MLG damage is in the playerInteractEvent
                this.MLG = false;
            }

            this.fallHeight = 0.0;                  //reset fallHeight after the fall ends

            for (int i = 0; i < 2; i++) {           //reset the array after the fall ends
                arr[i] = -65.0;
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        org.bukkit.inventory.ItemStack i = CraftItemStack.asBukkitCopy(new net.minecraft.server.v1_16_R1.ItemStack(Items.BUCKET)); //convert nms itemstack to craftitemstack which is a subclass of bukkit itemstack

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && Objects.equals(event.getItem(), i) && this.fallHeight >= 2.0) { //when the player right clicks onto a block and after the right click they have an empty bucket in their hand and they fell for more than 2 blocks and haven't hit the ground yet (so if they MLG water'ed)
            double damage = 0.0;
            damage += Math.ceil(this.fallHeight) * 0.2;    //assume that the player right clicks the water about 2 blocks before hitting ground (so it is -0 instead of -2); do 20% of the damage they would have taken if they hadn't landed the MLG (basically so water only reduces 80% of fall damage)
            event.getPlayer().damage(damage);
            this.fallHeight = 0.0;                          //reset fallHeight as the MLG has been landed, meaning the fall ended
            this.MLG = true;
        }
    }
}
