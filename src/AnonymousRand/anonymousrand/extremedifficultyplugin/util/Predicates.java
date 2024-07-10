package AnonymousRand.anonymousrand.extremedifficultyplugin.util;

import org.bukkit.Material;

import java.util.function.Predicate;

public class Predicates {
    public static Predicate<Material> blockBreakableDefault = (type) ->
            type != Material.AIR
            && type != Material.BARRIER
            && type != Material.COBWEB
            && type != Material.COMMAND_BLOCK
            && type != Material.COMMAND_BLOCK_MINECART
            && type != Material.END_GATEWAY
            && type != Material.END_PORTAL
            && type != Material.END_PORTAL_FRAME
            && type != Material.JIGSAW
            && type != Material.NETHER_PORTAL
            && type != Material.SPAWNER
            && type != Material.STRUCTURE_BLOCK;

    public static Predicate<Material> notBedrock = (type) ->
            type != Material.BEDROCK;

    public static Predicate<Material> notFireOrWitherRose = (type) ->
            type != Material.FIRE
            && type != Material.WITHER_ROSE;

    public static Predicate<Material> notFluid = (type) ->
            type != Material.LAVA
            && type != Material.WATER;

    public static Predicate<Material> notPreciousBlocks = (type) ->
            type != Material.BEACON
            && type != Material.DIAMOND_BLOCK
            && type != Material.EMERALD_BLOCK
            && type != Material.NETHERITE_BLOCK;

    public static Predicate<Material> notHardBlocks = (type) ->
            type != Material.ANCIENT_DEBRIS
            && type != Material.CRYING_OBSIDIAN
            && type != Material.END_STONE
            && type != Material.NETHERITE_BLOCK
            && type != Material.OBSIDIAN
            && type != Material.RESPAWN_ANCHOR;
}