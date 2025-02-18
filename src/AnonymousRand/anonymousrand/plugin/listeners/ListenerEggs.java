package AnonymousRand.anonymousrand.plugin.listeners;

import AnonymousRand.anonymousrand.plugin.ExtremeDifficultyPlugin;
import AnonymousRand.anonymousrand.plugin.nms.customentities.mobs.*;
import AnonymousRand.anonymousrand.plugin.nms.customentities.misc.CustomEntityAreaEffectCloud;
import AnonymousRand.anonymousrand.plugin.nms.customentities.misc.CustomEntityLightning;
import AnonymousRand.anonymousrand.plugin.util.SpawnEntity;
import AnonymousRand.anonymousrand.plugin.util.bukkitrunnables.*;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftProjectile;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ListenerEggs implements Listener {
    @EventHandler
    public void eggLand(ProjectileHitEvent event) { /* thrown eggs have chances to summon mobs/effects etc. */
        if (event.getEntity() instanceof Egg) {
            new RunnableEggLand(event.getEntity());
        }
    }

    static class RunnableEggLand extends BukkitRunnable {

        private final IProjectile nmsEgg;
        private final LivingEntity bukkitPlayer;
        private final EntityLiving nmsPlayer;
        private final Location playerLoc, eggLoc;
        private final org.bukkit.World bukkitWorld;
        private final World nmsWorld;
        private static final Random random = new Random();

        public RunnableEggLand(Projectile bukkitEgg) {
            this.nmsEgg = ((CraftProjectile)bukkitEgg).getHandle();

            if (bukkitEgg.getShooter() != null) {
                this.bukkitPlayer = (LivingEntity) bukkitEgg.getShooter();
                this.nmsPlayer = ((CraftLivingEntity)this.bukkitPlayer).getHandle();
                this.playerLoc = this.bukkitPlayer.getLocation();
            } else {
                this.bukkitPlayer = null;
                this.nmsPlayer = null;
                this.playerLoc = null;
                this.eggLoc = null;
                this.bukkitWorld = null;
                this.nmsWorld = null;
                return;
            }

            this.eggLoc = bukkitEgg.getLocation();
            this.bukkitWorld = bukkitEgg.getWorld();
            this.nmsWorld = ((CraftProjectile)bukkitEgg).getHandle().getWorld();
            this.run();
        }

        @Override
        public void run() {
            double rand = random.nextDouble();

            if (rand >= 0.5) {
                MobEffect nmsMobEffect;
                int rand2 = random.nextInt(39) + 1;

                switch (rand2) {
                    case 1:
                        nmsMobEffect = new MobEffect(MobEffects.ABSORBTION, 6000, 2);
                        break;
                    case 2:
                        nmsMobEffect = new MobEffect(MobEffects.BAD_OMEN, 36000, 0);
                        break;
                    case 3:
                        nmsMobEffect = new MobEffect(MobEffects.BLINDNESS, 150, 0);
                        break;
                    case 4:
                        nmsMobEffect = new MobEffect(MobEffects.CONDUIT_POWER, 1200, 0);
                        break;                        
                    case 5:
                        nmsMobEffect = new MobEffect(MobEffects.DOLPHINS_GRACE, 300, 0);
                        break;                        
                    case 6:
                        nmsMobEffect = new MobEffect(MobEffects.FIRE_RESISTANCE, 700, 0);
                        break;                        
                    case 7:
                        nmsMobEffect = new MobEffect(MobEffects.FASTER_DIG, 800, 1);
                        break;                        
                    case 8:
                        nmsMobEffect = new MobEffect(MobEffects.FASTER_DIG, 500, 3);
                        break;                        
                    case 9:
                        nmsMobEffect = new MobEffect(MobEffects.HEALTH_BOOST, 700, 4);
                        break;                        
                    case 10:
                        nmsMobEffect = new MobEffect(MobEffects.HUNGER, 1200, 2);
                        break;                        
                    case 11:
                        nmsMobEffect = new MobEffect(MobEffects.HARM, 1, 2);
                        break;                        
                    case 12:
                        nmsMobEffect = new MobEffect(MobEffects.HEAL, 36000, 1);
                        break;                        
                    case 13:
                        nmsMobEffect = new MobEffect(MobEffects.INVISIBILITY, 200, 0);
                        break;                        
                    case 14:
                        nmsMobEffect = new MobEffect(MobEffects.JUMP, 500, 2);
                        break;                        
                    case 15:
                        nmsMobEffect = new MobEffect(MobEffects.LEVITATION, 300, 0);
                        break;                        
                    case 16:
                        nmsMobEffect = new MobEffect(MobEffects.SLOWER_DIG, 3000, 0);
                        break;                        
                    case 17:
                        nmsMobEffect = new MobEffect(MobEffects.NIGHT_VISION, 2400, 0);
                        break;                        
                    case 18:
                        nmsMobEffect = new MobEffect(MobEffects.POISON, 300, 1);
                        break;                        
                    case 19:
                        nmsMobEffect = new MobEffect(MobEffects.POISON, 150, 2);
                        break;                        
                    case 20:
                        nmsMobEffect = new MobEffect(MobEffects.REGENERATION, 500, 1);
                        break;                        
                    case 21:
                        nmsMobEffect = new MobEffect(MobEffects.REGENERATION, 100, 3);
                        break;                        
                    case 22:
                        nmsMobEffect = new MobEffect(MobEffects.RESISTANCE, 1200, 0);
                        break;                        
                    case 23:
                        nmsMobEffect = new MobEffect(MobEffects.RESISTANCE, 600, 1);
                        break;                        
                    case 24:
                        nmsMobEffect = new MobEffect(MobEffects.RESISTANCE, 300, 2);
                        break;                        
                    case 25:
                        nmsMobEffect = new MobEffect(MobEffects.SATURATION, 8, 0);
                        break;                        
                    case 26:
                        nmsMobEffect = new MobEffect(MobEffects.SLOW_FALLING, 400, 0);
                        break;                        
                    case 27:
                        nmsMobEffect = new MobEffect(MobEffects.SLOWER_MOVEMENT, 600, 0);
                        break;                        
                    case 28:
                        nmsMobEffect = new MobEffect(MobEffects.SLOWER_MOVEMENT, 400, 2);
                        break;                        
                    case 29:
                        nmsMobEffect = new MobEffect(MobEffects.SLOWER_MOVEMENT, 280, 3);
                        break;                        
                    case 30:
                        nmsMobEffect = new MobEffect(MobEffects.FASTER_MOVEMENT, 600, 0);
                        break;                        
                    case 31:
                        nmsMobEffect = new MobEffect(MobEffects.FASTER_MOVEMENT, 400, 1);
                        break;                        
                    case 32:
                        nmsMobEffect = new MobEffect(MobEffects.FASTER_MOVEMENT, 280, 2);
                        break;                        
                    case 33:
                        nmsMobEffect = new MobEffect(MobEffects.INCREASE_DAMAGE, 280, 0);
                        break;                        
                    case 34:
                        nmsMobEffect = new MobEffect(MobEffects.INCREASE_DAMAGE, 140, 1);
                        break;                        
                    case 35:
                        nmsMobEffect = new MobEffect(MobEffects.WATER_BREATHING, 1200, 0);
                        break;                        
                    case 36:
                        nmsMobEffect = new MobEffect(MobEffects.WEAKNESS, 500, 0);
                        break;                        
                    case 37:
                        nmsMobEffect = new MobEffect(MobEffects.WEAKNESS, 250, 1);
                        break;                        
                    case 38:
                        nmsMobEffect = new MobEffect(MobEffects.WITHER, 600, 0);
                        break;                        
                    default:
                        nmsMobEffect = new MobEffect(MobEffects.WITHER, 130, 2);
                        break;                        
                }

                if (rand < 0.55) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityChickenAggressive(this.nmsWorld), 4, null, this.eggLoc, true);
                } else if (rand < 0.58) {
                    this.nmsPlayer.addEffect(nmsMobEffect);
                } else if (rand < 0.605) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.IRON_INGOT));
                } else if (rand < 0.63) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.ARROW, random.nextInt(3) + 1));
                } else if (rand < 0.65) {
                    this.bukkitWorld.spawn(this.eggLoc, ExperienceOrb.class).setExperience(random.nextInt(251) + 150);
                } else if (rand < 0.67) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityCreeper(this.nmsWorld), 1, null, this.playerLoc, true);
                } else if (rand < 0.69) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityZombie(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.705) {
                    CustomEntityAreaEffectCloud newAEC = new CustomEntityAreaEffectCloud(this.nmsWorld, 8, 1, 0);
                    newAEC.addEffect(nmsMobEffect);
                    newAEC.setPosition(this.eggLoc.getX(), this.eggLoc.getY(), this.eggLoc.getZ());
                    this.nmsWorld.addEntity(newAEC);
                } else if (rand < 0.7175) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.OBSIDIAN, 4));
                } else if (rand < 0.7285) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityWitch(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.7385) {
                    ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.addEnchant(Enchantment.DIG_SPEED, 1, false);
                    itemStack.setItemMeta(itemMeta);
                    this.bukkitWorld.dropItem(this.eggLoc, itemStack);
                } else if (rand < 0.7485) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityChickenAggressive(this.nmsWorld), 11, null, this.eggLoc, true);
                } else if (rand < 0.7585) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityGhast(this.nmsWorld), 1, null, this.eggLoc, false);
                } else if (rand < 0.7685) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityLightning(this.nmsWorld), 1, null, this.eggLoc, false);
                } else if (rand < 0.7785) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.DIAMOND));
                } else if (rand < 0.7885) {
                    ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.addEnchant(Enchantment.ARROW_DAMAGE, 1, false);
                    itemStack.setItemMeta(itemMeta);
                    this.bukkitWorld.dropItem(this.eggLoc, itemStack);
                } else if (rand < 0.7985) {
                    new SpawnEntity(this.nmsWorld, new CustomEntitySkeleton(this.nmsWorld), 2, null, this.eggLoc, true);
                } else if (rand < 0.8085) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.GOLDEN_APPLE));
                } else if (rand < 0.8165) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityEnderman(this.nmsWorld), 2, null, this.eggLoc, true);
                } else if (rand < 0.8245) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityDrowned(this.nmsWorld), 2, null, this.eggLoc, true);
                } else if (rand < 0.8325) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.NETHERITE_SCRAP));
                } else if (rand < 0.8405) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.SCUTE));
                } else if (rand < 0.848) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityShulker(this.nmsWorld), 1, null, this.eggLoc, false);
                } else if (rand < 0.855) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.BEETROOT));
                } else if (rand < 0.861) {
                    this.nmsWorld.getEntities(this.nmsEgg, this.nmsEgg.getBoundingBox().g(24.0), entity -> entity instanceof EntityMonster).forEach(entity -> {
                        if (!(entity instanceof EntityEnderDragon || entity instanceof EntityGolem || entity instanceof EntityRavager || entity instanceof EntityWither)) {
                            entity.die();
                        }
                    });
                } else if (rand < 0.867) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityBlaze(this.nmsWorld), 3, null, this.eggLoc, true);
                } else if (rand < 0.873) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityIllusioner(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.878) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.WHITE_BED));
                } else if (rand < 0.883) {
                    this.nmsWorld.createExplosion(this.nmsEgg, this.eggLoc.getX(), this.eggLoc.getY(), this.eggLoc.getZ(), 8.0F, true, Explosion.Effect.DESTROY);
                } else if (rand < 0.888) {
                    new SpawnEntity(this.nmsWorld, new CustomEntitySlimeMagmaCube(this.nmsWorld, 16), 1, null, this.eggLoc, true);
                } else if (rand < 0.893) {
                    new RunnableLightningStorm(this.nmsWorld, this.eggLoc, random.nextInt(11) + 35).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, random.nextInt(4) + 2);
                } else if (rand < 0.898) {
                    ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.addEnchant(Enchantment.SWEEPING_EDGE, 1, false);
                    itemStack.setItemMeta(itemMeta);
                    this.bukkitWorld.dropItem(this.eggLoc, itemStack);
                } else if (rand < 0.903) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityPig(this.nmsWorld), 20, null, this.eggLoc, true);
                } else if (rand < 0.908) {
                    new RunnableMeteorRain(this.nmsEgg, 4, 40.0, 30).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 3L);
                } else if (rand < 0.912) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityRavager(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.916) {
                    ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.addEnchant(Enchantment.DURABILITY, 1, false);
                    itemStack.setItemMeta(itemMeta);
                    this.bukkitWorld.dropItem(this.eggLoc, itemStack);
                } else if (rand < 0.92) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.IRON_BLOCK));
                } else if (rand < 0.924) {
                    new SpawnEntity(this.nmsWorld, new CustomEntitySilverfish(this.nmsWorld), 15, null, this.eggLoc, true);
                } else if (rand < 0.928) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.DIRT, 512));
                } else if (rand < 0.932) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityEvoker(this.nmsWorld), 3, null, this.eggLoc, true);
                } else if (rand < 0.9355) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityBee(this.nmsWorld), 6, null, this.eggLoc, true);
                } else if (rand < 0.9385) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityIronGolem(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.9415) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityGuardianElder(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.9445) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.BEDROCK));
                } else if (rand < 0.9475) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityLlama(this.nmsWorld), 3, null, this.eggLoc, true);
                } else if (rand < 0.9505) {
                    this.bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BAD_OMEN, Integer.MAX_VALUE, 0));
                } else if (rand < 0.9535) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.GOLD_BLOCK));
                } else if (rand < 0.9565) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityZombieThor(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.9595) {
                    new SpawnEntity(this.nmsWorld, new CustomEntitySheepAggressive(this.nmsWorld), 1, null, this.eggLoc, true);
                } else if (rand < 0.9625) {
                    this.nmsWorld.getEntities(this.nmsEgg, this.nmsEgg.getBoundingBox().g(32.0), entity -> ((entity instanceof EntityMonster || entity instanceof EntityGolem) && !(entity instanceof CustomEntityWither))).forEach(entity -> {
                        EntityInsentient entityInsentient = ((EntityInsentient) entity);
                        entityInsentient.addEffect(new MobEffect(MobEffects.FASTER_MOVEMENT, 6000, 3));
                        entityInsentient.setGoalTarget(this.nmsPlayer, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                        entityInsentient.getBukkitEntity().setCustomName("Won't despawn");
                    });
                } else if (rand < 0.965) {
                    this.nmsWorld.getEntities(this.nmsEgg, this.nmsEgg.getBoundingBox().g(32.0), entity -> ((entity instanceof EntityMonster || entity instanceof EntityGolem) && !(entity instanceof CustomEntityWither))).forEach(entity -> {
                        EntityInsentient entityInsentient = ((EntityInsentient) entity);
                        Mob bukkitEntityInsentient = (Mob)entityInsentient.getBukkitEntity();
                        bukkitEntityInsentient.setMaxHealth(bukkitEntityInsentient.getMaxHealth() * 2.0);
                        entityInsentient.setHealth(entityInsentient.getHealth() * 2.0F);
                        entityInsentient.setGoalTarget(this.nmsPlayer, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                        bukkitEntityInsentient.setCustomName("Won't despawn");
                    });
                } else if (rand < 0.9675) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.POISONOUS_POTATO, 512));
                } else if (rand < 0.97) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2));
                } else if (rand < 0.9725) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.GOLDEN_CARROT, 10));
                } else if (rand < 0.975) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.EMERALD, 32));
                } else if (rand < 0.9775) {
                    new RunnableTornado(this.nmsWorld, new BlockPosition(this.eggLoc.getX(), this.eggLoc.getY(), this.eggLoc.getZ()), 50, 140).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
                } else if (rand < 0.9795) {
                    new RunnableMeteorRain(this.nmsPlayer, 1, 70.0, 90).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
                    new RunnableMeteorRain(this.nmsPlayer, 2, 70.0, 90).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
                    new RunnableMeteorRain(this.nmsPlayer, 3, 70.0, 110).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
                } else if (rand < 0.9815) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityGhast(this.nmsWorld), 5, null, this.eggLoc, false);
                } else if (rand < 0.9835) {
                    if (this.bukkitPlayer.getEquipment().getHelmet() != null) {
                        this.bukkitPlayer.getEquipment().getHelmet().addEnchantment(Enchantment.VANISHING_CURSE, 1);
                        this.bukkitPlayer.getEquipment().getHelmet().addEnchantment(Enchantment.BINDING_CURSE, 1);
                    }

                    if (this.bukkitPlayer.getEquipment().getChestplate() != null) {
                        this.bukkitPlayer.getEquipment().getChestplate().addEnchantment(Enchantment.VANISHING_CURSE, 1);
                        this.bukkitPlayer.getEquipment().getChestplate().addEnchantment(Enchantment.BINDING_CURSE, 1);
                    }

                    if (this.bukkitPlayer.getEquipment().getLeggings() != null) {
                        this.bukkitPlayer.getEquipment().getLeggings().addEnchantment(Enchantment.VANISHING_CURSE, 1);
                        this.bukkitPlayer.getEquipment().getLeggings().addEnchantment(Enchantment.BINDING_CURSE, 1);
                    }

                    if (this.bukkitPlayer.getEquipment().getBoots() != null) {
                        this.bukkitPlayer.getEquipment().getBoots().addEnchantment(Enchantment.VANISHING_CURSE, 1);
                        this.bukkitPlayer.getEquipment().getBoots().addEnchantment(Enchantment.BINDING_CURSE, 1);
                    }
                } else if (rand < 0.9855) {
                    this.nmsWorld.getEntities(this.nmsEgg, this.nmsEgg.getBoundingBox().g(32.0), entity -> ((entity instanceof EntityMonster || entity instanceof EntityGolem) && !(entity instanceof CustomEntityWither))).forEach(entity -> {
                        EntityInsentient entityInsentient = ((EntityInsentient) entity);
                        AttributeModifiable nmsAttribute = entityInsentient.getAttributeInstance(GenericAttributes.ATTACK_DAMAGE);

                        if (nmsAttribute != null) {
                            nmsAttribute.setValue(nmsAttribute.getValue() * 2.0);
                        }

                        entityInsentient.setGoalTarget(this.nmsPlayer, EntityTargetEvent.TargetReason.CLOSEST_PLAYER, false);
                        entityInsentient.getBukkitEntity().setCustomName("Won't despawn");
                    });
                } else if (rand < 0.9875) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.TOTEM_OF_UNDYING));
                } else if (rand < 0.9895) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.ENDER_EYE));
                } else if (rand < 0.991) {
                    this.bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 12000, 1));
                } else if (rand < 0.992) {
                    for (Player bukkitPlayer : Bukkit.getServer().getOnlinePlayers()) {
                        new SpawnEntity(this.nmsWorld, new CustomEntityWitherMini(this.nmsWorld, false), 1, null, bukkitPlayer.getLocation(), false);
                    }
                } else if (rand < 0.993) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityWither(this.nmsWorld), 1, null, this.eggLoc, false);
                } else if (rand < 0.994) {
                    new SpawnEntity(this.nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize, new CustomEntityPhantom(this.nmsWorld, (int) ListenerMobSpawnAndReplaceWithCustom.phantomSize), 30, null, this.eggLoc, false);
                } else if (rand < 0.995) {
                    this.bukkitPlayer.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 140, 4));
                } else if (rand < 0.996) {
                    new SpawnEntity(this.nmsWorld, new CustomEntityVex(this.nmsWorld), 25, null, this.eggLoc, false);
                } else if (rand < 0.99695) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.DIAMOND_BLOCK));
                } else if (rand < 0.9977) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.NETHERITE_SCRAP, 12));
                } else if (rand < 0.9982) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.ENDER_EYE, 4));
                } else if (rand < 0.9986) {
                    new RunnableConstantlySpawnBlocksEntities(this.nmsPlayer, null, new EntityTNTPrimed(EntityTypes.TNT, this.nmsWorld), 0, 0, 0, 0.0, false, 50).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 1L);
                    Bukkit.broadcastMessage("5");
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> Bukkit.broadcastMessage("4"), 20);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> Bukkit.broadcastMessage("3"), 40);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> Bukkit.broadcastMessage("2"), 60);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> Bukkit.broadcastMessage("1"), 80);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(ExtremeDifficultyPlugin.plugin, () -> this.nmsWorld.createExplosion(this.nmsEgg, this.playerLoc.getX(), this.playerLoc.getY(), this.playerLoc.getZ(), 150.0F, true, Explosion.Effect.DESTROY), 100);
                } else if (rand < 0.999) {
                    ItemStack itemStack = new ItemStack(Material.CROSSBOW);
                    itemStack.addUnsafeEnchantment(Enchantment.QUICK_CHARGE, 5);
                    itemStack.addUnsafeEnchantment(Enchantment.PIERCING, 256);
                    this.bukkitWorld.dropItem(this.eggLoc, itemStack);
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.ARROW, 256));
                } else if (rand < 0.9994) {
                    new RunnableMobRain(nmsWorld, 130.0, new BlockPosition(0.0, 0.0, 0.0), 55.0, 1).runTaskTimer(ExtremeDifficultyPlugin.plugin, 0L, 3L);
                    new RunnableMobRain(nmsWorld, 130.0, new BlockPosition(0.0, 0.0, 0.0), 50.0, 2).runTaskTimer(ExtremeDifficultyPlugin.plugin, 400L, 10L);
                } else if (rand < 0.9997) {
                    this.bukkitWorld.dropItem(this.eggLoc, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 64));
                } else {
                    new SpawnEntity(this.nmsWorld, new CustomEntityWither(this.nmsWorld), 3, null, this.eggLoc, false);
                }
            }
        }
    }
}
