package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import org.bukkit.plugin.java.JavaPlugin;

public abstract class StaticPlugin {
    public static JavaPlugin plugin; // used by all classes that need a plugin for runnables etc.
}
