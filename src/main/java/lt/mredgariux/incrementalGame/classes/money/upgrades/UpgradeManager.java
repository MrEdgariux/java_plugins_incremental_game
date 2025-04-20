package lt.mredgariux.incrementalGame.classes.money.upgrades;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.classes.PlayerData;
import org.bukkit.Bukkit;

import java.lang.instrument.IllegalClassFormatException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class UpgradeManager {
    private List<Upgrade> upgrades = new ArrayList<>();

    public UpgradeManager() {
        this.upgrades = new ArrayList<>();
    }

    public void loadUpgrades(List<Upgrade> upgradess) {
        if (this.upgrades.addAll(upgradess)) {
            Bukkit.getLogger().info("[Upgrade Manager] -> Loaded " + upgradess.size() + " upgrades. In total we have " + this.upgrades.size() + " upgrades");
        } else {
            Bukkit.getLogger().severe("[Upgrade Manager] -> Cannot load " + upgradess.size() + " upgrades...");
        }
    }

    public List<Upgrade> getUpgradesForPlayer(PlayerData playerData) {
        if (this.upgrades.isEmpty()) {
            Bukkit.getLogger().severe("[Upgrade Manager] -> No upgrades loaded. Cannot proceed to the upgrade returning function.");
            return null;
        }
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

            LargeNumbers price = userUpgrade.getPrice();

            if (price.compareTo(new LargeNumbers(0,0)) <= 0) {
                return new UpgradeResult(userUpgrade, false, "Upgrade price is incorrect");
            }

            int possibleAmount = getPossibleUpgradeAmount(userUpgrade, playerData);
            if (amount > possibleAmount) {
                amount = possibleAmount;
            }

            if (amount == 0) {
                return new UpgradeResult(userUpgrade, false, "Not enough money");
            }

            for (int i = 0; i < amount; i++) {
                if (!userUpgrade.canBeUpgraded()) {
                    return new UpgradeResult(userUpgrade, false, "Upgrade is at maximum level.");
                }

                int results = playerData.getMoney().compareTo(price);

                if (results >= 0) {
                    playerData.removeMoney(price);
                    userUpgrade.increaseLevel();
                    userUpgrade.increaseUpgradePrice();
                    userUpgrade.increaseUpgradeCostMultiplier();
                } else {
                    break;
                }
            }

            return new UpgradeResult(userUpgrade, true);
        } else {
            // Jei neturi - sukuriam NAUJĄ kopiją ir pridedam žaidėjui
            Upgrade newUpgrade = new Upgrade(upgrade);

            if (!newUpgrade.canBeUpgraded()) {
                return new UpgradeResult(newUpgrade, false, "Upgrade is at maximum, disabled or unknown error occurred");
            }

            LargeNumbers price = newUpgrade.getPrice();
            if (price.compareTo(new LargeNumbers(0,0)) <= 0) {
                return new UpgradeResult(newUpgrade, false, "Upgrade price is incorrect");
            }

            int possibleAmount = getPossibleUpgradeAmount(newUpgrade, playerData);
            if (amount > possibleAmount) {
                amount = possibleAmount;
            }

            if (amount == 0) {
                return new UpgradeResult(newUpgrade, false, "Not enough money");
            }

            for (int i = 0; i < amount; i++) {
                if (!newUpgrade.canBeUpgraded()) {
                    return new UpgradeResult(newUpgrade, false, "Upgrade cannot be upgraded.");
                }

                if (playerData.getMoney().compareTo(price) >= 0) {
                    playerData.removeMoney(price);
                    newUpgrade.increaseLevel();
                    newUpgrade.increaseUpgradePrice();
                    newUpgrade.increaseUpgradeCostMultiplier();
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
        LargeNumbers playerMoney = new LargeNumbers(playerData.getMoney());
        LargeNumbers upgradePrice = new LargeNumbers(upgrade.getPrice());
        double multiplier = upgrade.getUpgradeCostMultiplier();

        if (playerMoney.compareTo(upgradePrice) < 0) {
            return 0; // Neturim pinigų net pirmai upgrade
        }

        if (multiplier <= 0) {
            return Integer.MAX_VALUE; // Jei nėra kainos augimo, galima pirkti be ribojimų
        }

        int maxUpgrades = 0;

        while (playerMoney.compareTo(upgradePrice) >= 0) {
            maxUpgrades++;
            playerMoney.subtract(upgradePrice);
            upgradePrice.multiply((multiplier * (maxUpgrades * 0.01 + 1)));
//            Bukkit.getLogger().info("Upgrade: " + upgrade.getName() + " cost " + upgradePrice + " can be bought " + maxUpgrades + " times until now. " + playerMoney + " player money xD");
        }

        return maxUpgrades;
    }


}
