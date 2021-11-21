package AnonymousRand.anonymousrand.extremedifficultyplugin.util.bukkitrunnables;

import org.bukkit.scheduler.BukkitRunnable;

public class RunnableGenerateBarrierFountain extends BukkitRunnable {

    private final org.bukkit.World bukkitWorld;

    public RunnableGenerateBarrierFountain(org.bukkit.World bukkitWorld) {
        this.bukkitWorld = bukkitWorld;
        this.run();
    }

    @Override
    public void run() {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                this.bukkitWorld.getBlockAt(x, 60, z).setType(org.bukkit.Material.BARRIER);
            }
        }

        for (int y = 61; y <= 64; y++) {
            this.bukkitWorld.getBlockAt(0, y, 0).setType(org.bukkit.Material.BARRIER);
        }

        for (int i = -1; i <= 1; i++) {
            this.bukkitWorld.getBlockAt(3, 61, i).setType(org.bukkit.Material.BARRIER);
        }

        for (int i = -1; i <= 1; i++) {
            this.bukkitWorld.getBlockAt(i, 61, -3).setType(org.bukkit.Material.BARRIER);
        }

        for (int i = -1; i <= 1; i++) {
            this.bukkitWorld.getBlockAt(-3, 61, i).setType(org.bukkit.Material.BARRIER);
        }

        for (int i = -1; i <= 1; i++) {
            this.bukkitWorld.getBlockAt(i, 61, 3).setType(org.bukkit.Material.BARRIER);
        }

        this.bukkitWorld.getBlockAt(2, 61, 2).setType(org.bukkit.Material.BARRIER);
        this.bukkitWorld.getBlockAt(-2, 61, 2).setType(org.bukkit.Material.BARRIER);
        this.bukkitWorld.getBlockAt(-2, 61, -2).setType(org.bukkit.Material.BARRIER);
        this.bukkitWorld.getBlockAt(2, 61, -2).setType(org.bukkit.Material.BARRIER);
    }
}
