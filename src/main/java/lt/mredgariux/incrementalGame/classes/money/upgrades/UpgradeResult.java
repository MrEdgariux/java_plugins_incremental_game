package lt.mredgariux.incrementalGame.classes.money.upgrades;

import org.jetbrains.annotations.NotNull;

import java.lang.instrument.IllegalClassFormatException;

public class UpgradeResult {
    public Upgrade upgrade;
    public boolean isSuccessful;
    public String message = "";

    public UpgradeResult(@NotNull Upgrade upgrade, boolean isSuccessful) throws IllegalClassFormatException {
        this.upgrade = upgrade;
        this.isSuccessful = isSuccessful;
        if (!isSuccessful) {
            throw new IllegalClassFormatException("Incorrect upgrade result function used. Must provide message for unsuccessful upgrades");
        }
    }

    public UpgradeResult(@NotNull Upgrade upgrade, boolean isSuccessful, String message) {
        this.upgrade = upgrade;
        this.isSuccessful = isSuccessful;
        this.message = message;
    }
}
