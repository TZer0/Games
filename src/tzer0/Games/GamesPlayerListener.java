package tzer0.Games;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;

// TODO: Auto-generated Javadoc
/**
 * The is a listener interface for receiving PlayerCommandPreprocessEvent events.
 * 
 */
public class GamesPlayerListener extends PlayerListener  {
    Configuration conf;
    private final Games plugin;
    public PermissionHandler permissions;

    public GamesPlayerListener(final Games plugin) {
        this.plugin = plugin;
    }

    /**
     * Sets the pointers so that they can be referenced later in the code
     *
     * @param permissions Permissions-handler (if available)
     */
    public void setPointers(Configuration config, PermissionHandler permissions) {
        conf = config;
        this.permissions = permissions;
    }
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        Player pl = event.getPlayer();
        if (event.getClickedBlock().getType() == Material.WALL_SIGN || event.getClickedBlock().getType() == Material.SIGN_POST) {
            Sign tmp = (Sign)event.getClickedBlock().getState();
            Board brd = plugin.findBoard(tmp.getLine(1));
            if (tmp.getLine(0).equalsIgnoreCase(ChatColor.DARK_GREEN + "[gol]")) {
                if (brd == null) {
                    pl.sendMessage(ChatColor.RED + "Could not find board.");
                } else {
                    brd.GOL(pl, plugin.toInt(tmp.getLine(2)));
                }
            } else if (tmp.getLine(0).equalsIgnoreCase(ChatColor.DARK_RED + "[CLEAR]")) {
                if (brd == null) {
                    pl.sendMessage(ChatColor.RED + "Could not find board.");
                } else {
                    brd.clear(pl);
                }
                
            }
        }
        LinkedList<Board> list = plugin.boards.get(pl.getWorld());
        if (list != null) {
            for (Board tmp : list) {
                    if (tmp.isInBoard(event.getClickedBlock())) {
                        tmp.modField(event.getClickedBlock());
                    }
                }
        }
        /*if (plugin.temp.isInBoard(event.getClickedBlock())) {
            event.setCancelled(true);
            plugin.temp.modField(event.getClickedBlock());
        }*/
    }
}