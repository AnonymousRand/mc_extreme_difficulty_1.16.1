package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

public interface IAttackLevelingMob {
    /* Unfortunately, we can't force shared instance variables because we don't get multiple inheritance :(
     * so every IAttackLevelingMob is going to manually have to initialize an AttackLevelingController
     * sucks that the fundamental function of an interface neq multiple inheritance (interfacing vs. code sharing) */
    int getAttacks();
    void increaseAttacks(int increment);
//    int[] getAttacksThreshs(); // todo uncomment
}
