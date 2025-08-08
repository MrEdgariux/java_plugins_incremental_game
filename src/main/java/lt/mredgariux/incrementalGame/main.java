package lt.mredgariux.incrementalGame;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.classes.PlayerData;
import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeOptions;
import lt.mredgariux.incrementalGame.commands.admin.setLocationCommand;
import lt.mredgariux.incrementalGame.commands.incrementalUpgradeCommand;
import lt.mredgariux.incrementalGame.classes.money.Currency;
import lt.mredgariux.incrementalGame.events.UpdateSignEvent;
import lt.mredgariux.incrementalGame.utils.ChatManager;
import lt.mredgariux.incrementalGame.utils.PacketManager;
import lt.mredgariux.incrementalGame.utils.UpgradesFunction;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.*;

public final class main extends JavaPlugin implements Listener {

    public HashMap<UUID, PlayerData> playerDataList = new HashMap<>();

    public List<PlayerData> dataList = new ArrayList<>();

    PacketManager packetManager = new PacketManager();

    public HashMap<String, Location> signLocations = new HashMap<>();

    MongoDBDatabase mongoDb;

    public UpgradesFunction upgradesFunction = new UpgradesFunction();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            mongoDb = new MongoDBDatabase();
            mongoDb.connect();

            Currency money_i = new Currency.Builder()
                    .setMoney(new LargeNumbers(10, 0))
                    .build();

            Currency money_ii = new Currency.Builder()
                    .setMoney(new LargeNumbers(2.5, 1))
                    .build();

            Currency coal = new Currency.Builder()
                    .setMoney(new LargeNumbers(1, 306))
                    .build();

            Currency coal_gen = new Currency.Builder()
                    .setCoal(new LargeNumbers(10, 0))
                    .build();


            // Main of the document creations xD
            mongoDb.createUpgrade(new Upgrade.Builder("Money I", "Gives 2x money", money_i, new UpgradeOptions.Builder()
                    .setMoneyIncrementalMultiplier(2)
                    .build()
            ).build());

            mongoDb.createUpgrade(new Upgrade.Builder("Money II", "Adds 2x money exponent", money_ii, new UpgradeOptions.Builder()
                    .setMoneyExponentalMultiplier(1)
                    .build()
            )
                    .setUpgradeCostMultiplierAdder(1.5)
                    .build());

            // Coming soon upgrades

            mongoDb.createUpgrade(new Upgrade.Builder("Hole in the money", "Sacrifices the money to generate coal", coal, new UpgradeOptions.Builder()
                    .setMoneyExponentalMultiplier(1)
                    .build()
            ).setUpgradeLevelMax(5).build());

            mongoDb.createUpgrade(new Upgrade.Builder("Coal generator I", "Burns coal to generate even more money", coal_gen, new UpgradeOptions.Builder()
                    .setMoneyExponentalMultiplier(1)
                    .build()
            ).setUpgradeLevelMax(5).build());

            upgradesFunction.getUpgradeManager().loadUpgrades(mongoDb.loadUpgrades());
        } catch (Exception e) {
            getLogger().severe("MongoDB connection failed. " + e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Loading signs from config
        if (getConfig().getConfigurationSection("game") == null || getConfig().getConfigurationSection("game.signs") == null) {
            getLogger().warning("No signs found in the config! Please set them up at the game ;)");
        } else {
            for (String key : Objects.requireNonNull(getConfig().getConfigurationSection("game.signs")).getKeys(false)) {
                if (!getConfig().isLocation("game.signs." + key)) {
                    getLogger().warning("Sign location for " + key + " is set incorrectly!");
                    continue;
                }
                Location loc = getConfig().getLocation("game.signs." + key);
                if (loc != null) {
                    signLocations.put(key, loc);
                } else {
                    getLogger().warning("Sign location for " + key + " is not set in the config!");
                }
            }
        }

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new UpdateSignEvent(), this);
        getCommand("upgrades").setExecutor(new incrementalUpgradeCommand());
        getCommand("setloc").setExecutor(new setLocationCommand());

        for (Player player : Bukkit.getOnlinePlayers()) {
            startUser(player);
            ChatManager.sendMessage(player, "&aGame was resumed!");
        }
    }

    @Override
    public void onDisable() {
        Iterator<PlayerData> iterator = dataList.iterator();
        while (iterator.hasNext()) {
            PlayerData data = iterator.next();
            endUser(data);
            ChatManager.sendMessage(data.getPlayer(), "&cGame paused (&6Caused by: &nPLUGIN_RELOAD&c)");
            iterator.remove();
        }

        mongoDb.disconnect();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!playerDataList.containsKey(player.getUniqueId())) {
            startUser(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerDataList.containsKey(player.getUniqueId())) {
            endUser(playerDataList.get(player.getUniqueId()));
        }
    }

    /**
     * @param player - Player which will be starting incremental game on
     */
    public void startUser(Player player) {
        PlayerData loadedPlayerData = mongoDb.loadFromDatabase(player);

        Location upgradeSignLocation = signLocations.get("upgrades");

        PlayerData playerData;
        playerData = Objects.requireNonNullElseGet(loadedPlayerData, () -> new PlayerData(player));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                playerData.increaseMoney();

                BossBar existingBossBar = null;
                for (BossBar bossBar : player.activeBossBars()) {
                    existingBossBar = bossBar;
                    break;
                }

                if (existingBossBar != null) {
                    existingBossBar.name(ChatManager.decodeLegacyMessage("&cBalance: " + playerData.getMoney().toString()));
                } else {
                    BossBar newBossBar = BossBar.bossBar(ChatManager.decodeLegacyMessage("&cBalance: " + playerData.getMoney().toString()), 1, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
                    player.showBossBar(newBossBar);
                }

                for (Map.Entry<String, Location> entry : signLocations.entrySet()) {
                    String currencyType = entry.getKey();
                    Location signLocation = entry.getValue();

                    if (currencyType.equals("upgrades")) {
                        continue; // Skip upgrades sign, handled separately
                    }

                    if (signLocation == null) {
                        getLogger().warning("Sign location for " + currencyType + " is not set in the config!");
                        continue;
                    }
                    String signCurrencyType = currencyType.substring(0, 1).toUpperCase() + currencyType.substring(1).toLowerCase();
                    String value = playerData.getCurrency(currencyType).toString();
                    packetManager.updateSign(signLocation, playerData, signCurrencyType, "", value);
                }

                if (upgradeSignLocation != null) {
                    Location upgradeSignLocationClone = upgradeSignLocation.clone();

                    for (Upgrade upgrade : upgradesFunction.getUpgradeManager().getUpgradesForPlayer(playerData)){
                        packetManager.updateSign(upgradeSignLocationClone, playerData, upgrade.getName(), upgrade.getPrice().toString(), String.valueOf(upgrade.getLevel()), String.valueOf(upgradesFunction.getUpgradeManager().getPossibleUpgradeAmount(upgrade, playerData)));
                        upgradeSignLocationClone.add(1,0,0);
                    }
                }


            } catch (Exception e) {
                getLogger().severe("Error: " + e.getMessage() + " from user " + player.getName() + " (UUID: " + player.getUniqueId() + ")" );
                for (BossBar bosas : player.activeBossBars()) {
                    player.hideBossBar(bosas);
                }
                player.showTitle(Title.title(ChatManager.decodeLegacyMessage("&4Incremental Error"), ChatManager.decodeLegacyMessage("&cIssue has been sent to the developer. Incremental game paused"), Title.Times.times(Duration.ZERO, Duration.ofSeconds(10), Duration.ZERO)));
                endUser(playerData);
            }
        }, 0L, 20L);

        playerData.setRunnableTask(task);
        dataList.add(playerData);
        playerDataList.put(player.getUniqueId(), playerData);

    }

    public void endUser(PlayerData playerData) {
        playerData.cancelTask();
        Player p = playerData.getPlayer();
        for (BossBar b : p.activeBossBars()) {
            p.hideBossBar(b);
        }
        playerDataList.remove(playerData.getPlayer().getUniqueId());
        mongoDb.saveToDatabase(playerData);
    }
}
