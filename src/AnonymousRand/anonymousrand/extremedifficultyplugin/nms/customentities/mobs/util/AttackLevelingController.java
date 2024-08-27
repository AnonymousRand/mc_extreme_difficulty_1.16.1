package AnonymousRand.anonymousrand.extremedifficultyplugin.nms.customentities.mobs.util;

import java.util.ArrayList;

public class AttackLevelingController {

    private int attacks;
    private int[] attacksThreshs;

    public AttackLevelingController(int... attacksThreshs) {
        this.attacksThreshs = attacksThreshs;
        this.attacks = 0;
    }

    /**
     * Increases <code>attacks</code>.
     *
     * @return all newly met or exceeded thresholds
     */
    public int[] increaseAttacksAndCheckThreshs(int increment) {
        if (increment == 1) {
            this.attacks += increment;
            for (int attackThresh : this.attacksThreshs) {
                if (this.attacks == attackThresh) {
                    return new int[]{this.attacks};
                }
            }
        } else {
            // check for multiple crossed threshs if incrementing by multiple
            ArrayList<Integer> metThreshs = new ArrayList<>();
            int attacksBefore = this.attacks;
            this.attacks += increment;
            for (int attackThresh : this.attacksThreshs) {
                if (attacksBefore < attackThresh && this.attacks >= attackThresh) {
                    metThreshs.add(attackThresh);
                }
            }

            // dankly convert ArrayList<Integer> to int[]
            return metThreshs.stream().mapToInt(Integer::intValue).toArray();
        }

        return new int[]{};
    }

    public int getAttacks() {
        return this.attacks;
    }

    public int[] getAttacksThreshs() {
        return this.attacksThreshs;
    }
}
