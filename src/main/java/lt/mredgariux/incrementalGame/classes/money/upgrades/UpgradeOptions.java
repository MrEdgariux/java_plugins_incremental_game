package lt.mredgariux.incrementalGame.classes.money.upgrades;

import org.bson.Document;

public class UpgradeOptions {
    public int moneyIncrementalMultiplier = 1;
    public int moneyExponentalMultiplier = 0;

    // Builder class for optional parameters
    public static class Builder {
        private int moneyIncrementalMultiplier = 1; // Default value
        private int moneyExponentalMultiplier = 0; // Default value

        // Setters for optional parameters
        public Builder setMoneyIncrementalMultiplier(int moneyIncrementalMultiplier) {
            this.moneyIncrementalMultiplier = moneyIncrementalMultiplier;
            return this;
        }

        public Builder setMoneyExponentalMultiplier(int moneyExponentalMultiplier) {
            this.moneyExponentalMultiplier = moneyExponentalMultiplier;
            return this;
        }

        // Build method
        public UpgradeOptions build() {
            return new UpgradeOptions(this);
        }
    }

    // Private constructor to be used by Builder
    private UpgradeOptions(Builder builder) {
        this.moneyIncrementalMultiplier = builder.moneyIncrementalMultiplier;
        this.moneyExponentalMultiplier = builder.moneyExponentalMultiplier;
    }

    public UpgradeOptions() {}

    public UpgradeOptions(UpgradeOptions other) {
        this.moneyIncrementalMultiplier = other.moneyIncrementalMultiplier;
        this.moneyExponentalMultiplier = other.moneyExponentalMultiplier;
    }

    public Document toDocument() {
        return new Document()
                .append("moneyIncrementalMultiplier", moneyIncrementalMultiplier)
                .append("moneyExponentalMultiplier", moneyExponentalMultiplier);
    }

    public void fromDocument(Document document) {
        this.moneyIncrementalMultiplier = document.getInteger("moneyIncrementalMultiplier");
        this.moneyExponentalMultiplier = document.getInteger("moneyExponentalMultiplier");
    }

    @Override
    public String toString() {
        return "UpgradeOptions{" +
                "moneyIncrementalMultiplier=" + moneyIncrementalMultiplier +
                ", moneyExponentalMultiplier=" + moneyExponentalMultiplier +
                '}';
    }
}
