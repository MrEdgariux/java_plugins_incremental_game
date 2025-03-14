package lt.mredgariux.incrementalGame.commands;

import lt.mredgariux.incrementalGame.classes.PlayerData;
import lt.mredgariux.incrementalGame.classes.money.upgrades.Upgrade;
import lt.mredgariux.incrementalGame.classes.money.upgrades.UpgradeResult;
import lt.mredgariux.incrementalGame.main;
import lt.mredgariux.incrementalGame.utils.ChatManager;
import lt.mredgariux.incrementalGame.utils.UpgradesFunction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.instrument.IllegalClassFormatException;
import java.util.List;

public class incrementalUpgradeCommand implements CommandExecutor {
    UpgradesFunction function = new UpgradesFunction();
    Plugin plugin = main.getPlugin(main.class);
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }

        /* PlayerData receival */
        PlayerData plData = ((main) plugin).playerDataList.get(player.getUniqueId());

        if (plData == null) {
            ChatManager.sendMessage(player, "&4You are not in the game.");
            return true;
        }

        List<Upgrade> upgradeList = function.getUpgradeManager().getUpgradesForPlayer(plData);

        if (args.length == 0) {
            for (Upgrade upgrade : upgradeList) {
                ChatManager.sendMessage(player, String.format("&2(&6%s&2)&a %s &6-&a %s &6(Price: %s | Level: %s)", upgrade.getId(), upgrade.getName(), upgrade.getDescription(), upgrade.getUpgradePriceFormatted(), upgrade.getLevel()));
            }
        } else {
            try {
                String id = args[0];
                if (id.isBlank()) {
                    return false;
                }
                Upgrade upgrade = function.getUpgradeById(id, upgradeList);
                if (upgrade == null) {
                    ChatManager.sendMessage(player, "&cUpgrade with the id &6" + id + "&c does not exist");
                    return false;
                }

                try {
                    int amountInt = 1;

                    if (args.length > 1) {
                        String amount = args[1];
                        try {
                            amountInt = Integer.parseInt(amount);
                        } catch (NumberFormatException e) {
                            return false;
                        }
                    }
                    UpgradeResult results = function.getUpgradeManager().buyUpgrade(upgrade, plData, amountInt);
                    if (results.isSuccessful) {
                        ChatManager.sendMessage(player, "&aUpgrade purchased successfully!");
                    } else {
                        ChatManager.sendMessage(player, "&c" + results.message);
                    }
                } catch (IllegalClassFormatException e) {
                    ChatManager.sendMessage(player, "&cPlugin catched some problems nx... Report them now you idiot!");
                    throw e; // Tolinesnėj funkcijai nx
                }
            } catch (Exception e) {
                plugin.getLogger().severe(e.getMessage());
                return false;
            }
        }

        return false;
    }
}
