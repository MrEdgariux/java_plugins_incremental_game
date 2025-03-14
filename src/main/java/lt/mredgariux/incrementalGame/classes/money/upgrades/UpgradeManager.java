package lt.mredgariux.incrementalGame.classes.money.upgrades;

import lt.mredgariux.incrementalGame.classes.PlayerData;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UpgradeManager {
    private List<Upgrade> upgrades = new ArrayList<>();

    public UpgradeManager() {
        UpgradeOptions upgradeOptions = new UpgradeOptions();
        upgradeOptions.moneyIncrementalMultiplier = 2;

        this.upgrades.add(new Upgrade("Money I", "Increases your earnings by x2", 10, upgradeOptions));
    }

    public List<Upgrade> getUpgrades() {
        return upgrades;
    }

    public List<Upgrade> getUpgradesForPlayer(PlayerData playerData) {

        // Gauti visų įsigytų patobulinimų ID
        Set<String> purchasedUpgradeIds = playerData.getPurchasedUpgrades().stream()
                .map(Upgrade::getId)
                .collect(Collectors.toSet());

        // Pridėti įsigytus patobulinimus
        List<Upgrade> upgradesForPlayer = new ArrayList<>(playerData.getPurchasedUpgrades());

        // Pridėti galimus įsigyti patobulinimus, bet tik jei jų dar neturi
        for (Upgrade upgrade : this.upgrades) {
            if (!purchasedUpgradeIds.contains(upgrade.getId())) {
                if (!upgrade.canBeUpgraded()) continue;
                upgradesForPlayer.add(upgrade);
            }
        }

        return upgradesForPlayer;
    }

    public void addUpgrade(Upgrade upgrade) {
        this.upgrades.add(upgrade);
    }

    public void removeUpgrade(Upgrade upgrade) {
        this.upgrades.remove(upgrade);
    }

    public UpgradeResult buyUpgrade(Upgrade upgrade, PlayerData playerData, int amount) throws IllegalClassFormatException {
        // Patikrinam, atnaujinimas iš vis egzistuoja
        boolean upgradeExistsInList = upgrades.stream()
                .anyMatch(u -> u.getId().equals(upgrade.getId()));

        if (!upgradeExistsInList) {
            return new UpgradeResult(upgrade, false, "Upgrade not found");
        }

        // Patikrinam, ar žaidėjas jau turi šį upgrade
        Optional<Upgrade> existingUpgrade = playerData.getPurchasedUpgrades().stream()
                .filter(x -> x.getId().equals(upgrade.getId()))
                .findFirst();

        if (existingUpgrade.isPresent()) {
            // Jei jau turi - redaguojam jo egzistuojančią kopiją
            Upgrade userUpgrade = existingUpgrade.get();
            if (!userUpgrade.canBeUpgraded()) {
                return new UpgradeResult(userUpgrade, false, "Upgrade is at maximum, disabled or unknown error occurred");
            }

            double price = userUpgrade.getPrice();

            if (price <= 0) {
                return new UpgradeResult(userUpgrade, false, "Upgrade price is incorrect");
            }

            int possibleAmount = getPossibleUpgradeAmount(userUpgrade, playerData);
            if (amount > possibleAmount) {
                amount = possibleAmount;
            }

            for (int i = 0; i < amount; i++) {
                if (!userUpgrade.canBeUpgraded()) {
                    return new UpgradeResult(userUpgrade, false, "Upgrade is at maximum level.");
                }

                if (playerData.getMoney() >= price) {
                    playerData.removeMoney(price);
                    userUpgrade.increaseLevel();
                    userUpgrade.increaseUpgradePrice();
                } else {
                    return new UpgradeResult(userUpgrade, false, "Not enough money");
                }
            }

            return new UpgradeResult(userUpgrade, true);
        } else {
            // Jei neturi - sukuriam NAUJĄ kopiją ir pridedam žaidėjui
            Upgrade newUpgrade = new Upgrade(upgrade);

            if (!newUpgrade.canBeUpgraded()) {
                return new UpgradeResult(newUpgrade, false, "Upgrade is at maximum, disabled or unknown error occurred");
            }

            double price = newUpgrade.getPrice();
            if (price <= 0) {
                return new UpgradeResult(newUpgrade, false, "Upgrade price is incorrect");
            }

            int possibleAmount = getPossibleUpgradeAmount(newUpgrade, playerData);
            if (amount > possibleAmount) {
                amount = possibleAmount;
            }

            for (int i = 0; i < amount; i++) {
                if (!newUpgrade.canBeUpgraded()) {
                    return new UpgradeResult(newUpgrade, false, "Upgrade cannot be upgraded.");
                }

                if (playerData.getMoney() >= price) {
                    playerData.removeMoney(price);
                    newUpgrade.increaseLevel();
                    newUpgrade.increaseUpgradePrice();
                } else {
                    playerData.addUpgrade(newUpgrade);
                    return new UpgradeResult(newUpgrade, false, "Not enough money");
                }
            }
            playerData.addUpgrade(newUpgrade);
            return new UpgradeResult(newUpgrade, true);
        }
    }

    public int getPossibleUpgradeAmount(Upgrade upgrade, PlayerData playerData) {
        double playerMoney = playerData.getMoney();
        double upgradePrice = upgrade.getPrice();
        int maxAmount = 0;

        while (playerMoney >= upgradePrice) {
            maxAmount++;
            playerMoney -= upgradePrice; // Sumokam už upgrade
            upgradePrice *= upgrade.getUpgradeCostMultiplier(); // Padidinam kainą pagal augimo faktorių
        }

        return maxAmount;
    }
}
