package lt.mredgariux.incrementalGame.utils;

import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeManager;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class UpgradesFunction {
    UpgradeManager upgradeManager = new UpgradeManager();

    @Nullable
    public Upgrade getUpgradeByName(final String name, List<Upgrade> upgradeList) {
        for (Upgrade upgrade : upgradeList) {
            if (upgrade.getName().equals(name)) {
                return upgrade;
            }
        }
        return null;
    }

    @Nullable
    public Upgrade getUpgradeById(final String id, List<Upgrade> upgrades) {
        for (Upgrade upgrade : upgrades) {
            if (upgrade.getId().equals(id.toLowerCase())) {
                return upgrade;
            }
        }
        return null;
    }

    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }
}
