package lt.mredgariux.incrementalGame;

import lt.mredgariux.incrementalGame.classes.PlayerData;
import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeManager;
import lt.mredgariux.incrementalGame.commands.incrementalUpgradeCommand;
import lt.mredgariux.incrementalGame.events.UpdateSignEvent;
import lt.mredgariux.incrementalGame.utils.BasicFunctions;
import lt.mredgariux.incrementalGame.utils.ChatManager;
import lt.mredgariux.incrementalGame.utils.PacketManager;
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

    private final UpgradeManager upgradeManager = new UpgradeManager();

    @Override
    public void onEnable() {

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
            ChatManager.sendMessage(data.getPlayer(), "&cGame was temporarily paused. (Reason: PLUGIN-RELOAD)");
            iterator.remove(); // Saugiai pašalina elementą
        }
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
        PlayerData playerData = new PlayerData(player);

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, () -> {
            try {
                playerData.increaseMoney(); // Fixed will be in future

                BossBar existingBossBar = null;
                for (BossBar bossBar : player.activeBossBars()) {
                    existingBossBar = bossBar;
                    break;
                }

                if (existingBossBar != null) {
                    existingBossBar.name(ChatManager.decodeLegacyMessage("&cBalance: " + BasicFunctions.format(playerData.getMoney())));
                } else {
                    BossBar newBossBar = BossBar.bossBar(ChatManager.decodeLegacyMessage("&cBalance: " + BasicFunctions.format(playerData.getMoney())), 1, BossBar.Color.GREEN, BossBar.Overlay.PROGRESS);
                    player.showBossBar(newBossBar);
                }

                Location l = new Location(Bukkit.getWorld("world"), 18,85,7);
                packetManager.updateSign(l, playerData, "Balance", "", BasicFunctions.format(playerData.getMoney()));

                l = new Location(Bukkit.getWorld("world"), 17,85,7);
                Upgrade upgrade = upgradeManager.getUpgradesForPlayer(playerData).getFirst();
                packetManager.updateSign(l, playerData, upgrade.getName(), BasicFunctions.format(upgrade.getPrice()), BasicFunctions.format(upgrade.getLevel()), BasicFunctions.format(upgradeManager.getPossibleUpgradeAmount(upgrade, playerData)));

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
    }
}
