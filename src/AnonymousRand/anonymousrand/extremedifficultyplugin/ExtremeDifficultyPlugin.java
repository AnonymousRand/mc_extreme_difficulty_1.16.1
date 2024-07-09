package AnonymousRand.anonymousrand.extremedifficultyplugin;

import AnonymousRand.anonymousrand.extremedifficultyplugin.listeners.*;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.BlockOverride;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.CustomMathHelper;
import net.minecraft.server.v1_16_R1.Blocks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtremeDifficultyPlugin extends JavaPlugin {

    public static JavaPlugin plugin; // used by all classes that need a plugin for runnables etc.

    @Override
    public void onLoad() {}

    @Override
    public void onEnable() { // this runs when the plugin is first enabled (when the server starts up)
        this.initializeStaticPlugin();
        this.initializeListeners();
        this.changeProperties();
        this.changeRecipes();
        CustomMathHelper.initTrigTables();
    }

    @Override
    public void onDisable() {}

    ///////////////////////////////////////////////////////////////////////////////////////////////

    // Initialize static plugin field
    private void initializeStaticPlugin() {
        plugin = this;
    }

    // Register listeners
    private void initializeListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        
        pluginManager.registerEvents(new ListenerBlockPlaceAndBreak(), this);
        pluginManager.registerEvents(new ListenerDragonFight(), this);
        pluginManager.registerEvents(new ListenerDropItem(),this);
        pluginManager.registerEvents(new ListenerEggs(), this);
        pluginManager.registerEvents(new ListenerMobDamage(), this);
        pluginManager.registerEvents(new ListenerMobDeath(),this);
        pluginManager.registerEvents(new ListenerMobSpawnAndReplaceWithCustom(), this);
        pluginManager.registerEvents(new ListenerLightningStrike(),this);
        pluginManager.registerEvents(new ListenerPiglinBarter(), this);
        pluginManager.registerEvents(new ListenerPlayerDamage(),this);
        pluginManager.registerEvents(new ListenerPlayerDeathAndRespawn(), this);
        pluginManager.registerEvents(new ListenerPlayerEat(),this);
        pluginManager.registerEvents(new ListenerPlayerInteract(),this);
        pluginManager.registerEvents(new ListenerPlayerJoinAndQuit(),this);
        pluginManager.registerEvents(new ListenerPlayerMovementAndFallDamage(), this);
        pluginManager.registerEvents(new ListenerPotionEffect(), this);
        pluginManager.registerEvents(new ListenerProjectile(), this);
        pluginManager.registerEvents(new ListenerRaidAndVillager(),this);
        pluginManager.registerEvents(new ListenerSheepDye(), this);
        pluginManager.registerEvents(new ListenerSleep(), this);
        pluginManager.registerEvents(new ListenerVehicleCreate(), this);
    }

    private void changeProperties() {
        changeProperties_BlastResistance();
    }

    private void changeRecipes() {
        changeRecipes_eyeOfEnder();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////

    private void changeProperties_BlastResistance() {
        BlockOverride endStone = new BlockOverride(Blocks.END_STONE); /* end stone now has a blast resistance of 16 */
        endStone.set("durability", 16.0F);
        BlockOverride obsidian = new BlockOverride(Blocks.OBSIDIAN); /* obsidian, crying obsidian, anvils, enchanting tables, ancient debris and respawn anchors now has a blast resistance of 6, the same as cobblestone */
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
        BlockOverride spawner = new BlockOverride(Blocks.SPAWNER); /* spawners are now indestructible by explosions */
        spawner.set("durability", 3600000.0F);
        BlockOverride conduit = new BlockOverride(Blocks.CONDUIT); /* conduits are now indestructible by explosions */
        conduit.set("durability", 3600000.0F);
    }

    /* Eye of Ender recipe is much harder */
    private void changeRecipes_eyeOfEnder() {
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
        newRecipe.addIngredient(Material.DAMAGED_ANVIL);
        Bukkit.getServer().addRecipe(newRecipe);
    }
}