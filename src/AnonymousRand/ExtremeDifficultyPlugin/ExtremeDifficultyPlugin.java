package AnonymousRand.ExtremeDifficultyPlugin;

import AnonymousRand.ExtremeDifficultyPlugin.listeners.*;
import org.bukkit.plugin.java.JavaPlugin;

public class ExtremeDifficultyPlugin extends JavaPlugin {
    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() { //this runs when the plugin is first enabled (when the server starts up)
        getServer().getPluginManager().registerEvents(new EntitySpawnListeners(this), this); //registers the listeners
        getServer().getPluginManager().registerEvents(new EntityDamageListeners(this), this);
        getServer().getPluginManager().registerEvents(new ProjectileListeners(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMovementAndInteractionListeners(), this);
        getServer().getPluginManager().registerEvents(new VehicleCreateListeners(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceAndBreakListeners(), this);
        getServer().getPluginManager().registerEvents(new SleepListeners(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListeners(this), this);
    }

    @Override
    public void onDisable() {

    }
}
