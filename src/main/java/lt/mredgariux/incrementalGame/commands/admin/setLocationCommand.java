package lt.mredgariux.incrementalGame.commands.admin;

import lt.mredgariux.incrementalGame.main;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class setLocationCommand implements CommandExecutor {
    Plugin plugin = main.getPlugin(main.class);

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] strings) {

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("This command can only be used by players.");
            return true;
        }
        Player player = (Player) commandSender;
        if (strings.length != 1) {
            player.sendMessage("Usage: /setloc <type>");
            return true;
        }

        Block lookingAtBlock = player.getTargetBlockExact(5);
        if (lookingAtBlock == null || lookingAtBlock.getType().isAir() || !isSign(lookingAtBlock.getType())) {
            player.sendMessage("You must be looking at a sign.");
            return true;
        }
        Location lookingAt = lookingAtBlock.getLocation();

        String type = strings[0].toLowerCase();
        switch (type) {
            case "money" -> {
                plugin.getConfig().set("game.signs.money", lookingAt);
                plugin.saveConfig();
                player.sendMessage("Money sign location set to: " + lookingAt.getBlockX() + ", " + lookingAt.getBlockY() + ", " + lookingAt.getBlockZ());
                return true;
            }
            case "coal" -> {
                plugin.getConfig().set("game.signs.coal", lookingAt);
                plugin.saveConfig();
                player.sendMessage("Coal sign location set to: " + lookingAt.getBlockX() + ", " + lookingAt.getBlockY() + ", " + lookingAt.getBlockZ());
                return true;
            }
            case "iron" -> {
                plugin.getConfig().set("game.signs.iron", lookingAt);
                plugin.saveConfig();
                player.sendMessage("Iron sign location set to: " + lookingAt.getBlockX() + ", " + lookingAt.getBlockY() + ", " + lookingAt.getBlockZ());
                return true;
            }
            case "gold" -> {
                plugin.getConfig().set("game.signs.gold", lookingAt);
                plugin.saveConfig();
                player.sendMessage("Gold sign location set to: " + lookingAt.getBlockX() + ", " + lookingAt.getBlockY() + ", " + lookingAt.getBlockZ());
                return true;
            }
            case "diamond" -> {
                plugin.getConfig().set("game.signs.diamond", lookingAt);
                plugin.saveConfig();
                player.sendMessage("Diamond sign location set to: " + lookingAt.getBlockX() + ", " + lookingAt.getBlockY() + ", " + lookingAt.getBlockZ());
                return true;
            }
            case "ruby" -> {
                plugin.getConfig().set("game.signs.ruby", lookingAt);
                plugin.saveConfig();
                player.sendMessage("Ruby sign location set to: " + lookingAt.getBlockX() + ", " + lookingAt.getBlockY() + ", " + lookingAt.getBlockZ());
                return true;
            }
            case "upgrades" -> {
                plugin.getConfig().set("game.signs.upgrades", lookingAt);
                plugin.saveConfig();
                player.sendMessage("Upgrades sign location set to: " + lookingAt.getBlockX() + ", " + lookingAt.getBlockY() + ", " + lookingAt.getBlockZ());
                return true;
            }
            default -> {
                player.sendMessage("Invalid type. Valid types are: money, coal, iron, gold, upgrades.");
                return true;
            }
        }
    }

    private boolean isSign(Material material) {
        return material.toString().toLowerCase().contains("sign");
    }
}
