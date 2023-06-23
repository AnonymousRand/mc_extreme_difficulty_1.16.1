package AnonymousRand.anonymousrand.extremedifficultyplugin.customentities.custommobs.util;

public class AttackController {

    private int attacks;
    private int[] thresholds;

    public AttackController(int[] thresholds) {
        this.thresholds = thresholds;
        this.attacks = 0;
    }

    public int incrementAttacks(int increment) {
        this.attacks += increment;
        for (int threshold : this.thresholds) {
            if (this.attacks == threshold) {
                return this.attacks;
            }
        }

        return 0;
    }

    public int getAttacks() {
        return this.attacks;
    }
}
