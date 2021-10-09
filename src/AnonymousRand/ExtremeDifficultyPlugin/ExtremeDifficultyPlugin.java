package AnonymousRand.ExtremeDifficultyPlugin;

import AnonymousRand.ExtremeDifficultyPlugin.listeners.EntityDamageListeners;
import AnonymousRand.ExtremeDifficultyPlugin.listeners.EntitySpawnListeners;
import AnonymousRand.ExtremeDifficultyPlugin.listeners.ProjectileListeners;
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
    }

    @Override
    public void onDisable() {

    }
}
