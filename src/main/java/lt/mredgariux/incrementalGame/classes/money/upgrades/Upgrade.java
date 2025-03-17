package lt.mredgariux.incrementalGame.classes.money.upgrades;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.classes.game.Requirement;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;

import java.util.HashMap;

public class Upgrade {
    private final String id;
    private final String upgradeName;
    private final String upgradeDescription;

    // Valiutos configas
    private LargeNumbers upgradePrice;
    private Requirement requirements;
    private long upgradeLevel = 0;
    private double upgradeCostMultiplier = 1.5;
    private long upgradeLevelMax = -1; // (-1) Unlimited | (0) Disabled | (1 - Inf) Limit xD

    private final UpgradeOptions upgradeOptions;

    // Builder class for optional parameters
    public static class Builder {
        private final String upgradeName;
        private final String upgradeDescription;
        private LargeNumbers upgradePrice;
        private Requirement requirements;
        private long upgradeLevel = 0;
        private double upgradeCostMultiplier = 1.5;
        private long upgradeLevelMax = -1; // Default: unlimited
        private UpgradeOptions upgradeOptions;

        // Constructor for required fields
        public Builder(@NotNull String upgradeName, @NotNull String upgradeDescription, @NotNull LargeNumbers price, @NotNull UpgradeOptions upgradeOptions) {
            this.upgradeName = upgradeName;
            this.upgradeDescription = upgradeDescription;
            this.upgradePrice = price;
            // TODO: Change this as soon as possible xD
            this.requirements = new Requirement.Builder().setMoney(price).build();
            this.upgradeOptions = upgradeOptions;
        }

        public Builder setUpgradeLevel(int upgradeLevel) {
            this.upgradeLevel = upgradeLevel;
            return this;
        }

        public Builder setRequirements(Requirement requirements) {
            this.requirements = requirements;
            return this;
        }

        public Builder setUpgradeCostMultiplier(double upgradeCostMultiplier) {
            this.upgradeCostMultiplier = upgradeCostMultiplier;
            return this;
        }

        public Builder setUpgradeLevelMax(int upgradeLevelMax) {
            this.upgradeLevelMax = upgradeLevelMax;
            return this;
        }

        // Build method
        public Upgrade build() {
            return new Upgrade(this);
        }
    }

    // Private constructor to be used by Builder
    private Upgrade(Builder builder) {
        this.id = builder.upgradeName.trim().toLowerCase().replace(" ", "_");
        this.upgradeName = builder.upgradeName;
        this.upgradeDescription = builder.upgradeDescription;
        this.upgradePrice = builder.upgradePrice;
        this.requirements = builder.requirements;
        this.upgradeLevel = builder.upgradeLevel;
        this.upgradeCostMultiplier = builder.upgradeCostMultiplier;
        this.upgradeLevelMax = builder.upgradeLevelMax;
        this.upgradeOptions = builder.upgradeOptions;
    }

    // Kopijuojantis konstruktorius
    public Upgrade(Upgrade other) {
        this.id = other.getId();
        this.upgradeName = other.getName();
        this.upgradeDescription = other.getDescription();
        this.upgradePrice = new LargeNumbers(other.getPrice());
        this.requirements = other.getRequirements();
        this.upgradeLevel = other.getLevel();
        this.upgradeOptions = new UpgradeOptions(other.getUpgradeOptions());
    }

    public String getId() {
        return id;
    }

    public void increaseLevel() {
        if (this.upgradeLevelMax == 0 || (this.upgradeLevelMax != -1 && this.upgradeLevel >= this.upgradeLevelMax)) return;
        upgradeLevel++;
    }

    public void setMaxLevel(int maxLevel) {
        this.upgradeLevelMax = maxLevel;
    }

    public long getMaxLevel() {
        return this.upgradeLevelMax;
    }

    public long getLevel() {
        return upgradeLevel;
    }

    public void setLevel(int level) {
        upgradeLevel = level;
    }

    public boolean canBeUpgraded() {
        return (this.upgradeLevel < this.upgradeLevelMax || this.upgradeLevelMax != 0 || upgradePrice.compareTo(new LargeNumbers(0, 0)) > 0);
    }

    public String getName() {
        return upgradeName;
    }

    public String getDescription() {
        return upgradeDescription;
    }

    public LargeNumbers getPrice() {
        return upgradePrice;
    }

    public Requirement getRequirements() {
        return requirements;
    }

    public void setRequirements(Requirement requirements) {
        this.requirements = requirements;
    }

    public String getUpgradePriceFormatted() {
        return upgradePrice.toString();
    }

    public double getUpgradeCostMultiplier() {
        return upgradeCostMultiplier;
    }

    public void setUpgradeCostMultiplier(double upgradeCostMultiplier) {
        this.upgradeCostMultiplier = upgradeCostMultiplier;
    }

    public void increaseUpgradePrice() {
        upgradePrice.multiply(upgradeCostMultiplier);
    }

    public UpgradeOptions getUpgradeOptions() {
        return upgradeOptions;
    }

    public Document toDocument() {
        Document doc = new Document();

        doc.put("id", id);
        doc.put("name", this.upgradeName);
        doc.put("description", this.upgradeDescription);

        doc.put("price", this.upgradePrice.toDocument());
        doc.put("requirements", requirements.toDocument());

        doc.put("upgradeCostMultiplier", upgradeCostMultiplier);
        doc.put("levelMax", this.upgradeLevelMax);
        doc.put("level", this.upgradeLevel);

        doc.put("options", this.upgradeOptions.toDocument());

        return doc;
    }

    public static Upgrade fromDocument(Document doc) {
        String name = doc.getString("name");
        String description = doc.getString("description");

        Document priceDoc = (Document) doc.get("price");
        Requirement requirements = Requirement.fromDocument((Document) doc.get("requirements"));

        double upgradeCostMultiplier = doc.getDouble("upgradeCostMultiplier");
        int levelMax = doc.getInteger("levelMax", -1);
        int level = doc.getInteger("level", 0);

        UpgradeOptions upgradeOptionss = new UpgradeOptions();
        upgradeOptionss.fromDocument((Document) doc.get("options"));

        return new Builder(name, description, LargeNumbers.fromDocument(priceDoc), upgradeOptionss)
                .setUpgradeLevelMax(levelMax)
                .setRequirements(requirements)
                .setUpgradeLevel(level)
                .setUpgradeCostMultiplier(upgradeCostMultiplier)
                .build();
    }
}
