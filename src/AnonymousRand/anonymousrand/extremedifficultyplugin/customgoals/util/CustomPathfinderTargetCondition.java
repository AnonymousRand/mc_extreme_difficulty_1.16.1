package AnonymousRand.anonymousrand.extremedifficultyplugin.customgoals.util;

import net.minecraft.server.v1_16_R1.EntityLiving;
import net.minecraft.server.v1_16_R1.PathfinderTargetCondition;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class CustomPathfinderTargetCondition extends PathfinderTargetCondition {

    public static final CustomPathfinderTargetCondition DEFAULT = new CustomPathfinderTargetCondition(-1.0); // negative detectionRange means don't check distance

    public CustomPathfinderTargetCondition(double detectionRange) {
        this(detectionRange, null);
    }

    public CustomPathfinderTargetCondition(double detectionRange, @Nullable Predicate<EntityLiving> targetPredicate) {
        super();

        // a() is setDetectionRange()
        // c() sets noLineOfSightRequired() to true (MCP-Reborn has it backwards, it seems)
        // e() sets useVisibilityModifier (invis/skulls) to false (default true)
        // a() is setCustomPredicate()
        // the other booleans are false by default
        this.a(detectionRange).c().e().a(targetPredicate);
    }
}