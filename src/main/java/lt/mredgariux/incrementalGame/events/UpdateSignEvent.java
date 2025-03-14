package lt.mredgariux.incrementalGame.events;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UpdateSignEvent implements Listener {
    List<Location> signLocations = new ArrayList<>(
            Arrays.asList(
                    new Location(Bukkit.getWorld("world"), 18, 85, 7),
                    new Location(Bukkit.getWorld("world"), 17, 85, 7),
                    new Location(Bukkit.getWorld("world"), 16, 86, 7)
            )
    );

    @EventHandler
    public void onSignChange(PlayerInteractEvent event) {
        if (event.getInteractionPoint() == null) return;
        Block block = event.getInteractionPoint().getBlock();
        if (block.getType() != Material.OAK_WALL_SIGN) return;
        Location loc = block.getLocation();
        if (signLocations.contains(loc)) {
            event.setCancelled(true);
        }
    }
}
