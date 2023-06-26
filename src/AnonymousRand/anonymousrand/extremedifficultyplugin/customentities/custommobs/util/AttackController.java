package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util;

import java.util.ArrayList;

public class AttackController {

    private int attacks;
    private int[] attackThresholds;

    public AttackController(int... attackThresholds) {
        this.attackThresholds = attackThresholds;
        this.attacks = 0;
    }

    // increments attacks and returns all met or exceeded thresholds
    public int[] increaseAttacks(int increment) {
        if (increment == 1) {
            this.attacks += increment;
            for (int attackThreshold : this.attackThresholds) {
                if (this.attacks == attackThreshold) {
                    return new int[]{this.attacks};
                }
            }
        } else {
            // check for multiple crossed thresholds if incrementing by multiple
            ArrayList<Integer> metThresholds = new ArrayList<>();
            int attacksBefore = this.attacks;
            this.attacks += increment;
            for (int attackThreshold : this.attackThresholds) {
                if (attacksBefore < attackThreshold && this.attacks >= attackThreshold) {
                    metThresholds.add(attackThreshold);
                }
            }
            // dankly convert ArrayList<Integer> to int[]
            return metThresholds.stream().mapToInt(Integer::intValue).toArray(); // still hate Java :: btw; like ????
        }

        return new int[]{};
    }

    public int getAttacks() {
        return this.attacks;
    }

    public int[] getAttackThresholds() {
        return this.attackThresholds;
    }
}
