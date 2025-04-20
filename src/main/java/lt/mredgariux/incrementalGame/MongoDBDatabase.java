package lt.mredgariux.incrementalGame;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.classes.PlayerData;
import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeOptions;
import org.bson.Document;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.json.simple.JSONObject;

import javax.naming.ConfigurationException;
import java.util.ArrayList;
import java.util.List;

public class MongoDBDatabase {
    Plugin plugin = (Plugin) main.getPlugin(main.class);
    private MongoClient mongoClient;
    private MongoCollection<Document> playersCollection;
    private MongoCollection<Document> upgradesCollection;

    public MongoDBDatabase() {}

    private final String CONNECTION_STRING = plugin.getConfig().getString("mongodb_uri");
    private final String DATABASE_NAME = plugin.getConfig().getString("mongodb_db");

    public void connect() {
        try {
            if (CONNECTION_STRING == null || CONNECTION_STRING.isEmpty() || DATABASE_NAME == null || DATABASE_NAME.isEmpty()) {
                throw new ConfigurationException("MongoDB configuration is missing!");
            }
            mongoClient = MongoClients.create(CONNECTION_STRING);
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            playersCollection = database.getCollection("players");
            upgradesCollection = database.getCollection("upgrades");
            plugin.getLogger().info("[MongoDB] Successfully connected to db!");
        } catch (Exception e) {
            plugin.getLogger().severe("[MongoDB] Could not connect to db: " + e.getMessage());
        }
    }

    public void disconnect() {
        if (mongoClient != null) {
            mongoClient.close();
            plugin.getLogger().info("[MongoDB] Fuckin disconnected nx.");
        }
    }

    public void saveToDatabase(PlayerData playerData) {
        Document doc = playerData.save();

        playersCollection.replaceOne(
                Filters.eq("userId", playerData.getPlayer().getUniqueId().toString()),
                doc,
                new ReplaceOptions().upsert(true)
        );

        plugin.getLogger().info("[MongoDB] Player data saved: " + playerData.getPlayer().getName());
    }

    public PlayerData loadFromDatabase(Player player) {
        Document docData = playersCollection.find(Filters.eq("userId", player.getUniqueId().toString())).first();
        if (docData == null) {
            plugin.getLogger().info("[MongoDB] No player data for user found: " + player.getName() + " (" + player.getUniqueId().toString() + ")");
            return null;
        }

        PlayerData playerData = new PlayerData(player);

        boolean success = playerData.load(docData, loadUpgrades());
        if (success) {
            plugin.getLogger().info("[MongoDB] Player data loaded: " + player.getName());
        } else {
            plugin.getLogger().severe("[MongoDB] Unable to load playr data: " + player.getName());
        }

        return playerData;
    }

    public List<Upgrade> loadUpgrades() {
        FindIterable<Document> docData = upgradesCollection.find();
        List<Upgrade> upgrades = new ArrayList<>();

        for (Document doc : docData) {
            try {
                String upgradeName = doc.getString("name");
                String upgradeDescription = doc.getString("description");
                long limit = doc.getLong("limit");
                double costMultiplier = doc.getDouble("costMultiplier");
                double costMultiplieradder = doc.getDouble("costMultiplierAdder");

                Document priceDoc = (Document) doc.get("price");
                LargeNumbers upgradePrice = new LargeNumbers(priceDoc.getDouble("mantisa"), priceDoc.getLong("exponent"));
                UpgradeOptions upgradeOptions = new UpgradeOptions();
                upgradeOptions.fromDocument(doc.get("options", Document.class));

                Upgrade upgrade = new Upgrade.Builder(upgradeName, upgradeDescription, upgradePrice, upgradeOptions)
                        .setUpgradeLevelMax(limit)
                        .setUpgradeCostMultiplier(costMultiplier)
                        .setUpgradeCostMultiplierAdder(costMultiplieradder)
                        .build();

                upgrades.add(upgrade);
            } catch (Exception e) {
                plugin.getLogger().severe("[MongoDB] Unable to load upgrade (Maybe bad structure of upgrade?): " + e.getMessage());
            }
        }

        return upgrades;
    }

    public void createUpgrade(Upgrade upgrade) {
        if (upgradesCollection.find(Filters.eq("id", upgrade.getId())).first() != null) {
            updateUpgrade(upgrade);
            return;
        }
        try {
            Document upgradeDoc = new Document()
                    .append("id", upgrade.getId())
                    .append("name", upgrade.getName())
                    .append("description", upgrade.getDescription())
                    .append("limit", upgrade.getMaxLevel())
                    .append("costMultiplier", upgrade.getUpgradeCostMultiplier())
                    .append("costMultiplierAdder", upgrade.getUpgradeCostMultiplierAdder())
                    .append("price", new Document()
                            .append("mantisa", upgrade.getPrice().getMantissa())
                            .append("exponent", upgrade.getPrice().getExponent()))
                    .append("options", upgrade.getUpgradeOptions().toDocument());

            upgradesCollection.insertOne(upgradeDoc);
            plugin.getLogger().info("[MongoDB] Upgrade added successfully: " + upgrade.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("[MongoDB] Unable to create upgrade: " + e.getMessage());
        }
    }

    public void updateUpgrade(Upgrade upgrade) {
        Document existingDoc = upgradesCollection.find(Filters.eq("id", upgrade.getId())).first();

        // Jei tokio įrašo nėra, tiesiog sukurk naują
        if (existingDoc == null) {
            return;
        }

        // Sukuriame naują dokumentą pagal dabartinę objekto būseną
        Document newDoc = new Document()
                .append("id", upgrade.getId())
                .append("name", upgrade.getName())
                .append("description", upgrade.getDescription())
                .append("limit", upgrade.getMaxLevel())
                .append("costMultiplier", upgrade.getUpgradeCostMultiplier())
                .append("costMultiplierAdder", upgrade.getUpgradeCostMultiplierAdder())
                .append("price", new Document()
                        .append("mantisa", upgrade.getPrice().getMantissa())
                        .append("exponent", upgrade.getPrice().getExponent()))
                .append("options", upgrade.getUpgradeOptions().toDocument());

        // Jei naujas dokumentas skiriasi nuo seno, atnaujink
        if (!newDoc.equals(existingDoc)) {
            try {
                upgradesCollection.replaceOne(Filters.eq("id", upgrade.getId()), newDoc);
                plugin.getLogger().info("[MongoDB] Upgrade updated: " + upgrade.getName());
            } catch (Exception e) {
                plugin.getLogger().severe("[MongoDB] Unable to update upgrade: " + e.getMessage());
            }
        }
    }


    public void deleteUpgrade(Upgrade upgrade) {
        try {
            upgradesCollection.deleteOne(Filters.eq("id", upgrade.getId()));
            plugin.getLogger().info("[MongoDB] Upgrade deleted successfully: " + upgrade.getName() + " (" + upgrade.getId() + ")");
        } catch (Exception e) {
            plugin.getLogger().severe("[MongoDB] Unable to delete upgrade: " + e.getMessage());
        }
    }
}
