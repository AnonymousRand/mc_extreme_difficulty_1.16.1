package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customgoals.attack;

import AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util.ICustomHostile;
import AnonymousRand.anonymousrand.extremedifficultyplugin.util.NMSUtil;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.Bukkit;

import java.util.EnumSet;

public class CustomPathfinderGoalRangedCrossbowAttack<T extends EntityInsentient & IRangedEntity & ICrossbow
        & ICustomHostile /* & IAttackLevelingMob*/> extends CustomPathfinderGoalRangedHandheldAttack<T> {

    public CustomPathfinderGoalRangedCrossbowAttack(T goalOwner, int attackCooldown) {
        this(goalOwner, attackCooldown, 1.0);
    }

    public CustomPathfinderGoalRangedCrossbowAttack(T goalOwner, int attackCooldown, double moveSpeed) {
        super(goalOwner, Items.CROSSBOW, attackCooldown, moveSpeed);
    }

    @Override
    protected void stopExecutingAttack() {
        super.stopExecutingAttack();

        // uncharge crossbow
        this.goalOwner.b(false);                               // setCharging()
        ItemCrossbow.a(this.goalOwner.getActiveItem(), false); // setCrossbowCharged()
    }

    @Override
    protected void tickAttack(EntityLiving target) {
        // animate crossbow: uncharged -> charging, charging -> charged, and charged -> uncharged states respectively
        if (this.remainingAttackCooldown == this.attackCooldown - 1) {
            this.goalOwner.c(ProjectileHelper.a(this.goalOwner, Items.CROSSBOW)); // setActiveHand()
            this.goalOwner.b(true);                                               // setCharging()
        } else if (this.remainingAttackCooldown == 1) {
            this.goalOwner.releaseActiveItem(); // todo test need?
            this.goalOwner.b(false);                                              // setCharging()
        } else if (this.remainingAttackCooldown == 0) {
            // getActiveItem() still seems to work despite releaseActiveItem() and literally setting it to
            // ItemStack.EMPTY; vanilla does this too in d() though so not that worried
            ItemCrossbow.a(this.goalOwner.getActiveItem(), false);                // setCrossbowCharged()
        }

        super.tickAttack(target);
    }

    @Override
    protected void attack(EntityLiving target) {
        this.goalOwner.a(target, 1.0F); // shoot()
    }
}
