package tzer0.Games;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

// TODO: Auto-generated Javadoc
/**
 * Plugin for bukkit allowing players to purchase mcMMO-experience points using iConomy/BOSEconomy-money.
 * 
 * @author TZer0
 */
public class Games extends JavaPlugin {
    PluginDescriptionFile pdfFile;
    Configuration conf;
    private final GamesPlayerListener playerListener = new GamesPlayerListener(this);
    private final GamesBlockListener blockListener = new GamesBlockListener(this);
    public PermissionHandler permissions;
    @SuppressWarnings("unused")
    private final String name = "Games";
    Board temp;
    Block pos;

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onDisable()
     */
    public void onDisable() {
        PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println(pdfFile.getName() + " disabled.");
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    public void onEnable() {
        temp = null;
        pos = null;
        pdfFile = this.getDescription();
        conf = getConfiguration();
        setupPermissions();
        //   playerListener.setPointers(getConfiguration(), permissions);
        PluginManager tmp = getServer().getPluginManager();
        //tmp.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        tmp.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        tmp.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        tmp.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        System.out.println(pdfFile.getName() + " version "
                + pdfFile.getVersion() + " is enabled!");
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        if (sender instanceof Player) {
            if (args.length == 1) {
                if (temp != null) {
                    temp.GOL();
                }
                return true;
            }
            Player pl = (Player) sender;
            if (pos == null) {
                pos = pl.getLocation().getBlock();
            } else {
                temp = new Board(20, pos, pl.getLocation().getBlock(), false, this);
                pos = null;
                temp.update();
            }
        }
        return true;
    }
    /**
     * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
     */
    private void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

        if (this.permissions == null) {
            if (test != null) {
                this.permissions = ((Permissions) test).getHandler();
            } else {
                System.out.println(ChatColor.YELLOW
                        + "Permissons not detected - defaulting to OP!");
            }
        }
    }
    /**
     * Converts to int if valid, if not: returns 0
     * @param in
     * @param sender
     * @return
     */
    public int toInt(String in) {
        int out = 0;
        if (checkInt(in)) {
            out = Integer.parseInt(in);
        }
        return out;
    }
    /**
     * Checks if a string is valid as a representation of an unsigned int.
     */
    public boolean checkInt(String in) {
        char chars[] = in.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!Character.isDigit(chars[i])) {
                return false;
            }
        }
        return true;
    }
}