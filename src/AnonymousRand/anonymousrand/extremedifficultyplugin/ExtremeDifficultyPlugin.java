package AnonymousRand.anonymousrand.extremedifficultyplugin;

import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.customprojectiles.CustomEntityLargeFireball;
import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.BlockOverride;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables.RunnableTornado;
import net.minecraft.server.v1_16_R1.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtremeDifficultyPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        this.changeBlocksBlastResistance();
        CustomMathHelper.initTrigTables();
    }

    @Override
    public void onEnable() { //this runs when the plugin is first enabled (when the server starts up)
        this.initializeListeners();
        this.initializePluginFields();
        this.addEyeOfEnderRecipe();
    }

    @Override
    public void onDisable() {

    }

    private void changeBlocksBlastResistance() {
        BlockOverride endStone = new BlockOverride(Blocks.END_STONE); /**end stone now has a blast resistance of 16*/
        endStone.set("durability", 16.0F);

        BlockOverride obsidian = new BlockOverride(Blocks.OBSIDIAN); /**obsidian, crying obsidian, anvils, enchanting tables, ancient debris and respawn anchors now has a blast resistance of 6, the same as cobblestone*/
        obsidian.set("durability", 6.0F);

        BlockOverride cryingObsidian = new BlockOverride(Blocks.CRYING_OBSIDIAN);
        cryingObsidian.set("durability", 6.0F);

        BlockOverride anvil = new BlockOverride(Blocks.ANVIL);
        anvil.set("durability", 6.0F);

        BlockOverride enchantingTable = new BlockOverride(Blocks.ENCHANTING_TABLE);
        enchantingTable.set("durability", 6.0F);

        BlockOverride ancientDebris = new BlockOverride(Blocks.ANCIENT_DEBRIS);
        ancientDebris.set("durability", 6.0F);

        BlockOverride respawnAnchor = new BlockOverride(Blocks.RESPAWN_ANCHOR);
        respawnAnchor.set("durability", 6.0F);

        BlockOverride spawner = new BlockOverride(Blocks.SPAWNER); /**spawners are now indestructible by explosions*/
        spawner.set("durability", 3600000.0F);

        BlockOverride conduit = new BlockOverride(Blocks.CONDUIT); /**conduits are now indestructible by explosions*/
        conduit.set("durability", 3600000.0F);
    }

    private void initializeListeners() { //registers the listeners
        getServer().getPluginManager().registerEvents(new BlockPlaceAndBreakListeners(), this);
        getServer().getPluginManager().registerEvents(new DropItemListeners(),this);
        getServer().getPluginManager().registerEvents(new MobDamageListeners(), this);
        getServer().getPluginManager().registerEvents(new MobDeathListeners(),this);
        getServer().getPluginManager().registerEvents(new MobSpawnAndReplaceWithCustomListeners(), this);
        getServer().getPluginManager().registerEvents(new LightningStrikeListeners(),this);
        getServer().getPluginManager().registerEvents(new PiglinBarterListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerDamageListeners(),this);
        getServer().getPluginManager().registerEvents(new PlayerDeathAndRespawnListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerEatListeners(),this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListeners(),this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListeners(),this);
        getServer().getPluginManager().registerEvents(new PlayerMovementAndFallDamageListeners(), this);
        getServer().getPluginManager().registerEvents(new PotionEffectListeners(), this);
        getServer().getPluginManager().registerEvents(new ProjectileListeners(), this);
        getServer().getPluginManager().registerEvents(new RaidAndVillagerListeners(),this);
        getServer().getPluginManager().registerEvents(new SheepDyeListeners(), this);
        getServer().getPluginManager().registerEvents(new SleepListeners(), this);
        getServer().getPluginManager().registerEvents(new VehicleCreateListeners(), this);
        getServer().getPluginManager().registerEvents(new VillagerTradeListeners(),this);
    }

    private void initializePluginFields() { //initializes static plugin fields
        CustomEntityEvoker.plugin = this;
        CustomEntityGhast.plugin = this;
        CustomEntitySkeleton.plugin = this;
        CustomEntitySkeletonWither.plugin = this;
        CustomEntityWither.plugin = this;
        CustomEntityZoglin.plugin = this;
        CustomEntityZombie.plugin = this;
        CustomEntityZombieThor.plugin = this;
        CustomEntityZombieVillager.plugin = this;
        CustomEntityLargeFireball.plugin = this;

        BlockPlaceAndBreakListeners.plugin = this;
        DropItemListeners.plugin = this;
        MobDeathListeners.plugin = this;
        MobSpawnAndReplaceWithCustomListeners.plugin = this;
        LightningStrikeListeners.plugin = this;
        LightningStrikeListeners.storm = false;
        LightningStrikeListeners.numberOfThors = 0;
        PlayerDamageListeners.plugin = this;
        PlayerDeathAndRespawnListeners.plugin = this;
        PlayerEatListeners.plugin = this;
        PlayerInteractListeners.plugin = this;
        PotionEffectListeners.plugin = this;
        ProjectileListeners.plugin = this;
        RaidAndVillagerListeners.plugin = this;
        SheepDyeListeners.plugin = this;

        RunnableTornado.plugin = this;
    }

    private void addEyeOfEnderRecipe() { /**changes eye of ender recipe*/
        Bukkit.getServer().removeRecipe(NamespacedKey.minecraft("ender_eye"));
        NamespacedKey key = new NamespacedKey(this, "eye_of_ender");
        ShapelessRecipe newRecipe = new ShapelessRecipe(key, new ItemStack(Material.ENDER_EYE));
        newRecipe.addIngredient(Material.BLAZE_ROD);
        newRecipe.addIngredient(Material.ENDER_PEARL);
        newRecipe.addIngredient(Material.NETHERITE_HOE);
        newRecipe.addIngredient(Material.BEETROOT_SOUP);
        newRecipe.addIngredient(Material.SCUTE);
        newRecipe.addIngredient(Material.WRITABLE_BOOK);
        newRecipe.addIngredient(Material.DAYLIGHT_DETECTOR);
        newRecipe.addIngredient(Material.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        newRecipe.addIngredient(Material.DEAD_BRAIN_CORAL);
        Bukkit.getServer().addRecipe(newRecipe);
    }
}
