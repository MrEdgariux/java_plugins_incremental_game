package lt.mredgariux.incrementalGame.classes.money.upgrades;

public class UpgradeOptions {
    public int moneyIncrementalMultiplier = 0;
    public int moneyExponentalMultiplier = 0;

    public UpgradeOptions() {}

    public UpgradeOptions(UpgradeOptions other) {
        this.moneyIncrementalMultiplier = other.moneyIncrementalMultiplier;
        this.moneyExponentalMultiplier = other.moneyExponentalMultiplier;
    }
}
