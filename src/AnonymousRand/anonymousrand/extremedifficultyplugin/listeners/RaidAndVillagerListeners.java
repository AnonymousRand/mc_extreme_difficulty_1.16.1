package AnonymousRand.anonymousrand.extremedifficultyplugin.listeners;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityArrow;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableMeteorRain;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.SpawnLivingEntity;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableThorLightningEffectStorm;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.entity.AbstractArrow;
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

    public static JavaPlugin plugin;
    private static final Random random = new Random();

    @EventHandler
    public void villagerDamage(EntityDamageByEntityEvent event) {
        Entity nmsDamager = ((CraftEntity)event.getDamager()).getHandle();

        if ((event.getEntityType() == VILLAGER)) {
            event.setDamage(event.getDamage() * 2.0); /**villagers take twice as much damage from everything*/

            if (nmsDamager instanceof EntityPlayer) { /**villagers give players bad omen if they are hit by a player*/
                ((EntityPlayer)nmsDamager).addEffect(new MobEffect(MobEffects.BAD_OMEN, Integer.MAX_VALUE, 255));
            } else if (nmsDamager instanceof CustomEntityArrow) {
                if (((CustomEntityArrow)nmsDamager).getShooter() instanceof EntityPlayer) {
                    ((EntityPlayer)((CustomEntityArrow)nmsDamager).getShooter()).addEffect(new MobEffect(MobEffects.BAD_OMEN, Integer.MAX_VALUE, 255));
                }
            }
        }
    }

    @EventHandler
    public void villagerDeath(EntityDeathEvent event) {
        if (event.getEntityType() == VILLAGER) {
            LivingEntity bukkitVillager = event.getEntity();
            EntityLiving nmsEntity = ((CraftLivingEntity)bukkitVillager).getHandle();
            World nmsWorld = ((CraftLivingEntity)bukkitVillager).getHandle().getWorld();
            double rand = random.nextDouble();

            new SpawnLivingEntity(nmsWorld, rand < 0.25 ? new CustomEntityPillager(nmsWorld) : rand < 0.5 ? new CustomEntityVindicator(nmsWorld) : rand < 0.7 ? new CustomEntityWitch(nmsWorld) : rand < 0.875 ? new CustomEntityEvoker(nmsWorld) : new CustomEntityIllagerIllusioner(nmsWorld), 1, null, bukkitVillager.getLocation(), true); /**when killed, villagers have a 25% chance to summon a pillager, a 25% chance to summon a vindicator, a 20% chance to summon a witch, a 17.5% chance to summon an evoker, and a 12.5% chance to summon an illusioner*/

            nmsEntity.getWorld().getEntities(nmsEntity, nmsEntity.getBoundingBox().grow(64.0, 128.0, 64.0), entity -> entity instanceof CustomEntityIronGolem).forEach(entity -> { /**golems within 64 blocks horizontally of killed villager get a 25% stat boost and summon a lightning effect storm like thor around it for 5 seconds*/
                ((CustomEntityIronGolem)entity).increaseStatsMultiply(1.25);
                new RunnableThorLightningEffectStorm(entity, 50 , true).runTaskTimer(plugin, 0L, 2L);
            });
        }
    }

    @EventHandler
    public void raidFinish(RaidFinishEvent event) { /**summon meteor rain when raid ends on random player*/
        Player player = event.getWinners().get(random.nextInt(event.getWinners().size()));

        new RunnableMeteorRain(player, 1, 70.0, 100).runTaskTimer(plugin, 0L, 1L);
        new RunnableMeteorRain(player, 2, 70.0, 100).runTaskTimer(plugin, 0L, 1L);
        new RunnableMeteorRain(player, 3, 70.0, 120).runTaskTimer(plugin, 0L, 1L);
    }

    //temp, todo end: delete
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {
        if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) && event.hasBlock()) {
            if (event.getClickedBlock().getType().equals(Material.OAK_SIGN)) {
                new RunnableTornado(((CraftWorld)event.getPlayer().getWorld()).getHandle(), new BlockPosition(event.getClickedBlock().getLocation().getX(), event.getClickedBlock().getLocation().getY(), event.getClickedBlock().getLocation().getZ()), 50.0, 150).runTaskTimer(plugin, 0L, 1L);
            }
        }
    }
}
