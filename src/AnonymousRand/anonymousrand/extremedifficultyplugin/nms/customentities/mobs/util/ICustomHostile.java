package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

public interface ICustomHostile {
    double getDetectionRange();
    boolean ignoresLOS();
    boolean ignoresY();
    /* Mobs are willing to take any fall to reach the player as they don't take fall damage */
    int bL(); // vanilla override (getMaxFallHeight()) (gets max height mobs are willing to drop from)
}
