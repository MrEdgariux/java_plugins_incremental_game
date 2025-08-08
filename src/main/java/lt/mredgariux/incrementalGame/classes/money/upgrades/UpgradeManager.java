package lt.mredgariux.incrementalGame.classes.money.upgrades;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.classes.PlayerData;
import lt.mredgariux.incrementalGame.classes.money.Currency;
import org.bukkit.Bukkit;

import java.lang.instrument.IllegalClassFormatException;
import java.util.*;
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

        List<Upgrade> upgradesForPlayer = new ArrayList<>(playerData.getPurchasedUpgrades());

        for (Upgrade upgrade : this.upgrades) {
            if (!purchasedUpgradeIds.contains(upgrade.getId())) {
                if (!upgrade.canBeUpgraded(playerData.getCurrency())) continue;
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
        // Patikrinam, ar tas upgrade egzistuoja
        boolean upgradeExistsInList = upgrades.stream()
                .anyMatch(u -> u.getId().equals(upgrade.getId()));

        if (!upgradeExistsInList) {
            return new UpgradeResult(upgrade, false, "Upgrade not found");
        }

        // Išsitraukiam jau turimą upgrade (jei turi)
        Optional<Upgrade> existingUpgrade = playerData.getPurchasedUpgrades().stream()
                .filter(x -> x.getId().equals(upgrade.getId()))
                .findFirst();

        Upgrade targetUpgrade = existingUpgrade.orElse(new Upgrade(upgrade));

        if (!targetUpgrade.canBeUpgraded(playerData.getCurrency())) {
            return new UpgradeResult(targetUpgrade, false, "Upgrade is at maximum, disabled or unknown error occurred");
        }

        // Kiek realiai gali įsigyti
        int possibleAmount = getPossibleUpgradeAmount(targetUpgrade, playerData);
        amount = Math.min(amount, possibleAmount);

        if (amount <= 0) {
            return new UpgradeResult(targetUpgrade, false, "Not enough currency");
        }

        for (int i = 0; i < amount; i++) {
            if (!targetUpgrade.canBeUpgraded(playerData.getCurrency())) {
                break;
            }

            Currency price = targetUpgrade.getPrice();

            // Patikrinam ar užtenka VISŲ valiutų
            boolean canAfford = price.getAllCurrencies().entrySet().stream()
                    .allMatch(entry -> playerData.getCurrency().getCurrency(entry.getKey())
                            .compareTo(entry.getValue()) >= 0);

            if (!canAfford) {
                break;
            }

            // Atimam VISAS kainas
            for (Map.Entry<String, LargeNumbers> entry : price.getAllCurrencies().entrySet()) {
                if (entry.getValue().compareTo(new LargeNumbers(0, 0)) > 0) {
                    playerData.getCurrency().getCurrency(entry.getKey())
                            .subtractInPlace(entry.getValue());
                }
            }

            targetUpgrade.increaseLevel();
            targetUpgrade.increaseUpgradePrice();
            targetUpgrade.increaseUpgradeCostMultiplier();
        }

        // Jei tai naujas upgrade – pridedam prie žaidėjo sąrašo
        if (existingUpgrade.isEmpty()) {
            playerData.addUpgrade(targetUpgrade);
        }

        return new UpgradeResult(targetUpgrade, true);
    }


    public int getPossibleUpgradeAmount(Upgrade upgrade, PlayerData playerData) {
        if (upgrade == null || playerData == null) {
            return 0;
        }

        Currency currencies = upgrade.getPrice();
        Currency playerCurrency = new Currency(playerData.getCurrency());
        int maxUpgrades = Integer.MAX_VALUE;

        for (Map.Entry<String, LargeNumbers> entry : currencies.getAllCurrencies().entrySet()) {
            String currencyType = entry.getKey();
            LargeNumbers price = entry.getValue();
            if (price.compareTo(new LargeNumbers(0, 0)) <= 0) continue;

            int possibleAmount = playerCurrency.getCurrency(currencyType)
                    .divide(price)
                    .toInt();
            if (possibleAmount < maxUpgrades) {
                maxUpgrades = possibleAmount;
            }
        }

        return maxUpgrades;
    }


}
