package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityIronGolem;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.CustomEntityZombie;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMeteorRain;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableThorLightningEffectStorm;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
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
            event.setDamage(event.getDamage() * 2.0); /**villagers take twice as much damage from everything*/

            if (event.getDamager() instanceof Player) { /**villagers give players bad omen if they are hit by a player*/
                ((Player)event.getDamager()).addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, Integer.MAX_VALUE, 255));
            }
        }
    }

    @EventHandler
    public void villagerDeath(EntityDeathEvent event) {
        if (event.getEntityType() == VILLAGER) {
            LivingEntity bukkitVillager = event.getEntity();
            EntityLiving nmsEntity = ((CraftLivingEntity)bukkitVillager).getHandle();
            World nmsWorld = ((CraftLivingEntity)bukkitVillager).getHandle().getWorld();

            new SpawnLivingEntity(nmsWorld, this.plugin, new CustomEntityZombie(nmsWorld, this.plugin), 5, null, bukkitVillager.getLocation(), true);  /**5 zombies are spawned when a villager dies*/

            List<Entity> nmsEntities = nmsEntity.getWorld().getEntities(nmsEntity, nmsEntity.getBoundingBox().g(64.0), entity -> entity instanceof CustomEntityIronGolem);

            for (Entity entity : nmsEntities) { /**golems within 64 block cube of killed villager get a 25% stat boost and summon a lightning effect storm like thor around it for 10 seconds*/
                ((CustomEntityIronGolem)entity).increaseStatsMultiply(1.25);
                new RunnableThorLightningEffectStorm(entity, 100 , true).runTaskTimer(this.plugin, 0L, 2L);
            }
        }
    }

    @EventHandler
    public void raidFinish(RaidFinishEvent event) { /**summon meteor rain when raid ends on random player*/
        Player player = event.getWinners().get(this.random.nextInt(event.getWinners().size()));

        new RunnableMeteorRain(player, 1, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
        new RunnableMeteorRain(player, 2, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
        new RunnableMeteorRain(player, 3, 70.0, 120).runTaskTimer(this.plugin, 0L, 1L);
    }

    //temp, todo end: delete
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.hasBlock()) {
            if (event.getClickedBlock().getType().equals(Material.OAK_SIGN)) {
                new RunnableMeteorRain(event.getPlayer(), 1, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
                new RunnableMeteorRain(event.getPlayer(), 2, 70.0, 100).runTaskTimer(this.plugin, 0L, 1L);
                new RunnableMeteorRain(event.getPlayer(), 3, 70.0, 120).runTaskTimer(this.plugin, 0L, 1L);
            }
        }
    }
}
