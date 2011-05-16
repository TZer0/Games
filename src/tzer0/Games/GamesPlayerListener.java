package tzer0.Games;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
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
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || plugin.temp == null) {
            return;
        }
        Player pl = event.getPlayer();
        if (plugin.temp.isInBoard(event.getClickedBlock())) {
            event.setCancelled(true);
            plugin.temp.modField(event.getClickedBlock());
        }
    }
}