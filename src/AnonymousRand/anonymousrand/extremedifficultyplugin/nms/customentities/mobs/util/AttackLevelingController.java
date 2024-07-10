package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import java.util.ArrayList;

public class AttackLevelingController {

    private int attacks;
    private int[] attacksThresholds;

    public AttackLevelingController(int... attacksThresholds) {
        this.attacksThresholds = attacksThresholds;
        this.attacks = 0;
    }

    /**
     * Increase <code>attacks</code>.
     *
     * @return all newly met or exceeded thresholds
     */
    public int[] increaseAttacks(int increment) {
        if (increment == 1) {
            this.attacks += increment;
            for (int attackThreshold : this.attacksThresholds) {
                if (this.attacks == attackThreshold) {
                    return new int[]{this.attacks};
                }
            }
        } else {
            // check for multiple crossed thresholds if incrementing by multiple
            ArrayList<Integer> metThresholds = new ArrayList<>();
            int attacksBefore = this.attacks;
            this.attacks += increment;
            for (int attackThreshold : this.attacksThresholds) {
                if (attacksBefore < attackThreshold && this.attacks >= attackThreshold) {
                    metThresholds.add(attackThreshold);
                }
            }
            return metThresholds.stream().mapToInt(Integer::intValue).toArray(); // dankly convert ArrayList<Integer> to int[]
        }

        return new int[]{};
    }

    public int getAttacks() {
        return this.attacks;
    }

    public int[] getAttacksThresholds() {
        return this.attacksThresholds;
    }
}
