package lt.mredgariux.incrementalGame;

import lt.mredgariux.incrementalGame.classes.LargeNumbers;
import lt.mredgariux.incrementalGame.classes.PlayerData;
import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeOptions;
import lt.mredgariux.incrementalGame.commands.incrementalUpgradeCommand;
import lt.mredgariux.incrementalGame.events.UpdateSignEvent;
import lt.mredgariux.incrementalGame.utils.BasicFunctions;
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

    MongoDBDatabase mongoDb;

    public UpgradesFunction upgradesFunction = new UpgradesFunction();

    @Override
    public void onEnable() {
        saveDefaultConfig();

        try {
            mongoDb = new MongoDBDatabase();
            mongoDb.connect();
            // Main of the document creations xD
            mongoDb.createUpgrade(new Upgrade.Builder("Money I", "Gives 2x money", new LargeNumbers(10,0), new UpgradeOptions.Builder()
                    .setMoneyIncrementalMultiplier(2)
                    .build()
            ).build());

            mongoDb.createUpgrade(new Upgrade.Builder("Money II", "Adds 2x money exponent", new LargeNumbers(2.5,1), new UpgradeOptions.Builder()
                    .setMoneyExponentalMultiplier(1)
                    .build()
            )
                    .setUpgradeCostMultiplierAdder(1.5)
                    .build());

            // Coming soon upgrades

            mongoDb.createUpgrade(new Upgrade.Builder("Hole in the money", "Sacrifices the money to generate coal", new LargeNumbers(1,306), new UpgradeOptions.Builder()
                    .setMoneyExponentalMultiplier(1)
                    .build()
            ).setUpgradeLevelMax(0).build());

            mongoDb.createUpgrade(new Upgrade.Builder("Coal generator I", "Burns coal to generate even more money", new LargeNumbers(1,306), new UpgradeOptions.Builder()
                    .setMoneyExponentalMultiplier(1)
                    .build()
            ).setUpgradeLevelMax(0).build());

            upgradesFunction.getUpgradeManager().loadUpgrades(mongoDb.loadUpgrades());
        } catch (Exception e) {
            getLogger().severe("MongoDB connection failed. " + e.getMessage());
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new UpdateSignEvent(), this);
        getCommand("upgrades").setExecutor(new incrementalUpgradeCommand());

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
            iterator.remove(); // Saugiai pašalina elementą
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

                Location l = new Location(Bukkit.getWorld("world"), 18,85,7);
                packetManager.updateSign(l, playerData, "Money", "", playerData.getCurrency("money").toString());
                packetManager.updateSign(l.subtract(0,1,0), playerData, "Coal", "", playerData.getCurrency("coal").toString());
                
                l = new Location(Bukkit.getWorld("world"), 17,85,7);

                for (Upgrade upgrade : upgradesFunction.getUpgradeManager().getUpgradesForPlayer(playerData)){
                    packetManager.updateSign(l, playerData, upgrade.getName(), upgrade.getPrice().toString(), String.valueOf(upgrade.getLevel()), String.valueOf(upgradesFunction.getUpgradeManager().getPossibleUpgradeAmount(upgrade, playerData)));
                    l.subtract(1,0,0);
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
