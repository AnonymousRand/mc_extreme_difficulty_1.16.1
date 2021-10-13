package AnonymousRand.ExtremeDifficultyPlugin.listeners;

import AnonymousRand.ExtremeDifficultyPlugin.customGoals.CustomPathfinderTargetCondition;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;

public class SleepListeners implements Listener {

    private static int cycles; //for extending duration of sleep
    private static long time;
    private static int peeCounter;

    public SleepListeners() {
        cycles = 0;
        time = 0;
        peeCounter = 0;
    }

    @EventHandler
    public void playerBedEnter(PlayerBedEnterEvent event) { //players can't sleep even if there are monsters below it (doesn't count horizontal range; no monster range increased to 30 blocks)

        EntityPlayer player = ((CraftPlayer)event.getPlayer()).getHandle();
        World world = ((CraftWorld)event.getPlayer().getWorld()).getHandle();
        EntityMonster closestMonster = world.a(EntityMonster.class, new CustomPathfinderTargetCondition(), player, player.locX(), player.locY(), player.locZ(), player.getBoundingBox().grow(128.0, 128.0, 128.0)); //get closes monster within 128 sphere radius of player

        if (closestMonster != null && (Math.pow(closestMonster.locX() - player.locX(), 2) + Math.pow(closestMonster.locZ() - player.locZ(), 2) <= 900.0) && (Math.pow(closestMonster.locX() - player.locX(), 2) + Math.pow(closestMonster.locZ() - player.locZ(), 2) >= 64.0)) { //player within 30 blocks horizontally of closestMonster but out of the default 8 block range
            if (Math.pow(closestMonster.locX() - player.locX(), 2) + Math.pow(closestMonster.locY() - player.locY(), 2) + Math.pow(closestMonster.locZ() - player.locZ(), 2) < 1800.0) { //player within 30 blocks including vertical distance of closestMonster
                Bukkit.broadcastMessage("There are still monsters nearby");
                event.setCancelled(true);
            } else if (closestMonster.locY() < player.locY()){ //player not within 30 blocks if counting vertical distance and is above mobs
                Bukkit.broadcastMessage("You may not sleep now, there are monsters below you");
                event.setCancelled(true);
            } else { //player not within 30 blocks if counting vertical distance and is below mobs
                Bukkit.broadcastMessage("You may not sleep now, there are monsters above you");
                event.setCancelled(true);
            }
        }

        if (!event.isCancelled()) {
            if (event.getPlayer().getWorld().getFullTime() - time >= 11000) { //to reset these stats if the last of the 3 sleep cycles were not executed and more than 1 night's worth of time has passed
                cycles = 0;                                                   //full time tells you the amount of RTA that has passed since the world started (not changed by sleeping and time set commands)
                time = 0;
                peeCounter = 0;
                Bukkit.broadcastMessage("reset");
            }

            if (time != 0 && event.getPlayer().getWorld().getFullTime() - time <= 120) { //it has been less than 6 seconds since the player was last woken up by playerBedLeave and the 3 sleep cycles haven't finished yet
                if (cycles == 1) {
                    Bukkit.broadcastMessage("You are still being haunted by the nightmare and can't sleep yet");
                } else {
                    if (peeCounter == 1) {
                        Bukkit.broadcastMessage("You are still peeing");
                        peeCounter++;
                    } else if (peeCounter < 4) {
                        Bukkit.broadcastMessage("Where does Steve pee?");
                    } else {
                        Bukkit.broadcastMessage("IT'S YOUR OWN BODY CAN'T YOU TELL THAT YOU ARE STILL PEEING");
                    }
                }

                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerBedLeave(PlayerBedLeaveEvent event) { //must sleep 3 times to completely pass night as each time only passes 1/3 of the night; with a 5 sec delay between each sleep attempt
        org.bukkit.World world = event.getPlayer().getWorld();

        switch (cycles) {
            case 0:
                Bukkit.broadcastMessage("time now: " + time);
                Bukkit.broadcastMessage("Congrats, you made it through the night......sike");
                Bukkit.broadcastMessage("You got woken up by a nightmare");
                time = world.getFullTime() - 7000;
                world.setFullTime(time); //approx 2/3 of the night
                cycles++;
                Bukkit.broadcastMessage("time now: " + time);
                break;
            case 1:
                Bukkit.broadcastMessage("It's 3am and you need to pee");
                time = world.getFullTime() - 3500;
                world.setFullTime(time); //approx 1/3 of the night
                cycles++;
                peeCounter = 1;
                break;
            case 2:
                Bukkit.broadcastMessage("Congrats, you made it through the night...enjoy all the leftover mobs");
                cycles = 0;
                time = 0;
                peeCounter = 0;
                break;
        }
    }
}