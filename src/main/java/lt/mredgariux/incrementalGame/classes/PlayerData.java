package lt.mredgariux.incrementalGame.classes;

import lt.mredgariux.incrementalGame.classes.money.Currency;
import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeOptions;
import lt.mredgariux.incrementalGame.utils.BasicFunctions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerData {
    private final Player player;
    private BukkitTask runnableTask;

    /* Incremental Things (Values) */
    public LargeNumbers money = new LargeNumbers(0,0);

    // BETA THING XD
    public Currency currency = new Currency();

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
        LargeNumbers incremental = new LargeNumbers(1,0);

        for (Upgrade upgrade : this.upgrades) {
            UpgradeOptions upgradeOptions = upgrade.getUpgradeOptions();

            if (upgradeOptions.moneyIncrementalMultiplier > 1 || upgradeOptions.moneyExponentalMultiplier > 0) {
                LargeNumbers one = new LargeNumbers(upgradeOptions.moneyIncrementalMultiplier, upgradeOptions.moneyExponentalMultiplier);
                LargeNumbers two = one.pow(upgrade.getLevel());
                incremental.multiply(two);
            }
        }

        Bukkit.getLogger().info(incremental.getMantissa() + " " + incremental.getExponent());

        money.add(incremental);
    }


    public LargeNumbers getMoney() {
        return money;
    }

    public Currency getCurrency() { return currency; }

    public LargeNumbers getCurrency(String currency) throws IllegalArgumentException {
        switch (currency.toLowerCase()) {
            case "money": return this.currency.money;
            case "coal": return this.currency.coal;
            case "iron": return this.currency.iron;
            case "gold": return this.currency.gold;
            case "diamond": return this.currency.diamond;
            case "ruby": return this.currency.ruby;
            default: throw new IllegalArgumentException("Unknown currency type: " + currency);
        }
    }

    public void removeMoney(LargeNumbers amount) {
        if (money.compareTo(amount) <= 0) {
            money = new LargeNumbers(0, 0);
        } else {
            money = money.subtract(amount);
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

    /* Išsaugojimas / Įkrovimas */

    public Document save() {
        Document doc = new Document();
        doc.put("userId", player.getUniqueId().toString());

        doc.put("money", BasicFunctions.convertNumbers(this.getMoney()));

        List<Document> upgradesList = new ArrayList<>();
        for (Upgrade upgrade : this.upgrades) {
            upgradesList.add(upgrade.toDocument());
        }

        doc.put("upgrades", upgradesList);
        return doc;
    }


    public boolean load(Document docData) {
        if (!docData.containsKey("userId")) return false;

        String userId = docData.getString("userId");

        if (!this.player.getUniqueId().toString().equals(userId)) return false;
        if (!docData.containsKey("money")) return false;

        this.money = LargeNumbers.fromDocument((Document) docData.get("money"));

        if (!docData.containsKey("upgrades")) return false;
        List<Document> upgradesList = docData.getList("upgrades", Document.class);

        // Įkeliame visus patobulinimus
        for (Document upgradeDoc : upgradesList) {
            Upgrade upgrade = Upgrade.fromDocument(upgradeDoc);
            Bukkit.getLogger().info("[Player Data Loader] -> Loaded upgrade " + upgrade.getId() + " with level " + upgrade.getLevel() + " for player " + getPlayer().getName());
            this.upgrades.add(upgrade);
        }

        return true;
    }
}
