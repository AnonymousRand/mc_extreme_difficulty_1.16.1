package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.bukkitrunnables.UtilMeteorRain;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityChickenAggressive;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.CustomPathfinderTargetCondition;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftVillager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

import static org.bukkit.entity.EntityType.VILLAGER;

public class RaidAndVillagerListeners implements Listener {

    private final JavaPlugin plugin;
    private final Random random = new Random();

    public RaidAndVillagerListeners(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void villagerDamage(EntityDamageByEntityEvent event) {
        if ((event.getEntityType() == VILLAGER)) {
            EntityVillager villager = ((CraftVillager)event.getEntity()).getHandle();
            EntityPlayer player = (villager.getWorld().a(EntityPlayer.class, new CustomPathfinderTargetCondition(), villager, villager.locX(), villager.locY(), villager.locZ(), villager.getBoundingBox().grow(128.0, 128.0, 128.0))); //get closest player within 128 sphere radius of villager

            if (player != null) {
                player.addEffect(new MobEffect(MobEffects.BAD_OMEN, Integer.MAX_VALUE, 255));
            }
        }
    }

    @EventHandler
    public void villagerDeath(EntityDeathEvent event) {
        if (event.getEntityType() == VILLAGER) {
            LivingEntity bukkitVillager = event.getEntity();
            World nmsWorld = ((CraftLivingEntity)bukkitVillager).getHandle().getWorld();

            new SpawnLivingEntity(nmsWorld, new CustomEntityChickenAggressive(nmsWorld), 5, null, bukkitVillager.getLocation(), true).run();  /**5 zombies are spawned when a villager dies*/
        }
    }

    @EventHandler
    public void raidFinish(RaidFinishEvent event) { /**summon meteor rain when raid ends on random player*/
        Player player = event.getWinners().get(this.random.nextInt(event.getWinners().size()));

        new UtilMeteorRain(player, 1, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
        new UtilMeteorRain(player, 2, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
        new UtilMeteorRain(player, 3, 70.0, 120).runTaskTimer(this.plugin, 0L, 1L);
    }

    //temp, todo end: delete
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.hasBlock()) {
            if (event.getClickedBlock().getType().equals(Material.OAK_SIGN)) {
                new UtilMeteorRain(event.getPlayer(), 1, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
                new UtilMeteorRain(event.getPlayer(), 2, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
                new UtilMeteorRain(event.getPlayer(), 3, 70.0, 120).runTaskTimer(this.plugin, 0L, 1L);
            }
        }
    }
}
