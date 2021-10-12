package AnonymousRand.ExtremeDifficultyPlugin.customGoals;

import net.minecraft.server.v1_16_R1.*;

import java.util.EnumSet;

public class CustomPathfinderGoalHurtByTarget extends PathfinderGoalHurtByTarget { //todo: copy to all applicable mobs

    private int c;
    private final Class<?>[] d;

    public CustomPathfinderGoalHurtByTarget(EntityCreature entitycreature, Class<?>... aclass) {
        super(entitycreature, aclass);
        this.d = aclass;
        this.a(EnumSet.of(PathfinderGoal.Type.TARGET));
    }

    @Override
    public boolean a() {
        int i = this.e.cZ();
        EntityLiving entityliving = this.e.getLastDamager();

        if (i != this.c && entityliving != null) {
            if (entityliving.getEntityType() == EntityTypes.PLAYER && this.e.world.getGameRules().getBoolean(GameRules.UNIVERSAL_ANGER)) {
                return false;
            } else {
                Class[] aclass = this.d;
                int j = aclass.length;

                for (int k = 0; k < j; ++k) {
                    Class<?> oclass = aclass[k];

                    if (oclass.isAssignableFrom(entityliving.getClass())) {
                        return false;
                    }
                }

                return (entityliving instanceof EntityPlayer); //mobs only retaliate against players

            }
        } else {
            return false;
        }
    }
}
