package tzer0.Games;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockListener;
import org.bukkit.event.block.BlockPlaceEvent;

public class GamesBlockListener extends BlockListener {
    private final Games plugin;

    public GamesBlockListener(final Games plugin) {
        this.plugin = plugin;
    }

    public void onBlockBreak(BlockBreakEvent event) {
    }
    public void onBlockPlace(BlockPlaceEvent event) {
    }
}
