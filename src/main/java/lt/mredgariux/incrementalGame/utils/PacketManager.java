package lt.mredgariux.incrementalGame.utils;

import lt.mredgariux.incrementalGame.classes.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;

public class PacketManager {
    public void updateSign(Location location, PlayerData playerData, String... lines) {
        if (location.getBlock().getType() != Material.OAK_WALL_SIGN) return;

        Sign signas = (Sign) location.getBlock().getState();
        Side side = Side.FRONT;

        // Užpildome eiles iki 4 tuščiomis reikšmėmis
        for (int i = 0; i < 4; i++) {
            signas.getSide(side).line(i, i < lines.length ? Component.text(lines[i]) : Component.empty());
        }

        // Atnaujina ženklą tik tam žaidėjui
        playerData.getPlayer().sendSignChange(location, signas.getSide(side).lines());
    }
}
