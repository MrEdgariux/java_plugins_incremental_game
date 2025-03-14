package lt.mredgariux.incrementalGame.classes.money.upgrades;

import lt.mredgariux.incrementalGame.utils.BasicFunctions;
import org.jetbrains.annotations.NotNull;

public class Upgrade {
    private final String id;
    private final String upgradeName;
    private final String upgradeDescription;
    private double upgradePrice;
    private int upgradeLevel = 0;
    private double upgradeCostMultiplier = 1.5;
    private int upgradeLevelMax = -1; // (-1) Unlimited | (0) Disabled | (1 - Inf) Limit xD

    /* Information about the upgrade itself. It may be kinda complex xD */
    /**
     * (0) - Do not affect
     * (1 - Inf) - Affect
     */

    private final UpgradeOptions upgradeOptions;

    public Upgrade(@NotNull String upgradeName, @NotNull String upgradeDescription, double upgradePrice, @NotNull UpgradeOptions upgradeOptions) {
        this.id = upgradeName.trim().toLowerCase().replace(" ", "_");
        this.upgradeName = upgradeName;
        this.upgradeDescription = upgradeDescription;
        this.upgradePrice = upgradePrice;
        this.upgradeOptions = upgradeOptions;
    }

    // Kopijuojantis konstruktorius
    public Upgrade(Upgrade other) {
        this.id = other.getId();
        this.upgradeName = other.getName();
        this.upgradeDescription = other.getDescription();
        this.upgradePrice = other.getPrice();
        this.upgradeLevel = other.getLevel();
        this.upgradeOptions = new UpgradeOptions(other.getUpgradeOptions()); // Svarbu: ir `options` reikia nukopijuoti
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

    public int getMaxLevel() {
        return this.upgradeLevelMax;
    }

    public int getLevel() {
        return upgradeLevel;
    }

    public void setLevel(int level) {
        upgradeLevel = level;
    }

    public boolean canBeUpgraded() {
        return (this.upgradeLevel < this.upgradeLevelMax || this.upgradeLevelMax != 0 || this.upgradePrice > 0);
    }

    public String getName() {
        return upgradeName;
    }

    public String getDescription() {
        return upgradeDescription;
    }

    public double getPrice() {
        return upgradePrice;
    }

    public String getUpgradePriceFormatted() {
        return BasicFunctions.format(this.upgradePrice);
    }

    public double getUpgradeCostMultiplier() {
        return upgradeCostMultiplier;
    }

    public void setUpgradeCostMultiplier(double upgradeCostMultiplier) {
        this.upgradeCostMultiplier = upgradeCostMultiplier;
    }

    public void increaseUpgradePrice() {
        upgradePrice *= this.upgradeCostMultiplier;
    }

    /* */

    public UpgradeOptions getUpgradeOptions() {
        return upgradeOptions;
    }
}
