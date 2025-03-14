package lt.mredgariux.incrementalGame.classes;

import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeOptions;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class PlayerData {
    private final Player player;
    private BukkitTask runnableTask;

    /* Incremental Things (Values) */
    public double money = 0;
    public LargeNumbers largeNumbers = new LargeNumbers(0,0);

    public List<Upgrade> upgrades = new ArrayList<>();

    public PlayerData(Player player) {
        this.player = player;
    }

    public void setRunnableTask(BukkitTask task) {
        this.runnableTask = task;
    }

    public void cancelTask() {
        if (runnableTask != null) {
            runnableTask.cancel();
        }
    }

    public Player getPlayer() {
        return player;
    }

    /* Incremental Stuff */

    public void increaseMoney() {
        double incremental = 1;

        for (Upgrade upgrade : this.upgrades) {
            UpgradeOptions upgradeOptions = upgrade.getUpgradeOptions();

            if (upgradeOptions.moneyIncrementalMultiplier > 1) {
                incremental *= Math.pow(upgradeOptions.moneyIncrementalMultiplier, upgrade.getLevel());
            }

            if (upgradeOptions.moneyExponentalMultiplier > 1) {
                incremental *= Math.pow(upgradeOptions.moneyExponentalMultiplier, upgrade.getLevel());
            }
        }

        money += incremental;
    }


    public double getMoney() {
        return money;
    }

    public void removeMoney(double amount) {
        if (money <= amount) {
            money = 0;
        } else {
            money -= amount;
        }
    }

    /* Upgrade stuff */

    public List<Upgrade> getPurchasedUpgrades() {
        return upgrades;
    }

    public void addUpgrade(Upgrade upgrade) {
        upgrades.removeIf(upg -> upg.getId().equals(upgrade.getId()));
        upgrades.add(upgrade);
    }

    public void removeUpgrade(Upgrade upgrade) {
        upgrades.removeIf(upg -> upg.getId().equals(upgrade.getId()));
    }

    public void setUpgrades(List<Upgrade> upgrades) {
        this.upgrades = upgrades;
    }
}
