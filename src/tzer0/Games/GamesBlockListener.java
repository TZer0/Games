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
        boolean access = (plugin.permissions == null && pl.isOp()) || (plugin.permissions != null && plugin.permissions.has(pl, "games.admin"));
        boolean changed = false;
        String lines[] = event.getLines();
        if (lines[0].equalsIgnoreCase("[gol]")) {
            event.setLine(0, ChatColor.DARK_GREEN+"[GOL]");
            changed = true;
        } else if (lines[0].equalsIgnoreCase("[clear]")) {
            event.setLine(0, ChatColor.DARK_RED+"[CLEAR]");
            changed = true;
        } else if (lines[0].equalsIgnoreCase("[tetris]")) {
            event.setLine(0, ChatColor.DARK_GREEN + "[Tetris]");
            changed = true;
        } else if (lines[0].equalsIgnoreCase("[exp]")) {
            event.setLine(0, ChatColor.DARK_GREEN + "[EXP]");
            changed = true;
        } else if (lines[0].equalsIgnoreCase("[conn]")) {
            event.setLine(0, ChatColor.DARK_GREEN + "[CONN]");
            changed = true;
        }

        if (changed) {
            if (!access) {
                event.setLine(0, ChatColor.RED + "No access.");
            }
            ((Sign)event.getBlock().getState()).update(true);
        }
    }
}
