package tzer0.Games;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;

public class GamesBlockListener extends BlockListener {
    private final Games plugin;

    public GamesBlockListener(final Games plugin) {
        this.plugin = plugin;
    }

    public void onBlockBreak(BlockBreakEvent event) {
    }
    public void onBlockPlace(BlockPlaceEvent event) {
    }
    public void onSignChange(SignChangeEvent event) {
        Player pl = event.getPlayer();
        if (event.getLine(0).equalsIgnoreCase("[gol]")) {
            if (plugin.permissions.has(pl, "games.admin")) {
                event.setLine(0, ChatColor.DARK_GREEN+"[GOL]");
            } else {
                event.setLine(0, ChatColor.RED + "No access.");
            }
            ((Sign)event.getBlock().getState()).update(true);
        } else if (event.getLine(0).equalsIgnoreCase("[clear]")) {
            if (plugin.permissions.has(pl, "games.admin")) {
                event.setLine(0, ChatColor.DARK_RED+"[CLEAR]");
            } else {
                event.setLine(0, ChatColor.RED + "No access.");
            }
            ((Sign)event.getBlock().getState()).update(true);
        }
    }
}
