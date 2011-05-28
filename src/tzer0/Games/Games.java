package tzer0.Games;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
import org.bukkit.util.config.ConfigurationNode;

import tzer0.Games.Board.CellType;

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
    LinkedHashSet<Integer> invalid;
    HashMap<World, LinkedList<Board>> boards;
    HashMap<Player, PlayerStorage> selected;

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
        invalid = new LinkedHashSet<Integer>();
        invalid.add(Material.TORCH.getId());
        invalid.add(Material.TNT.getId());
        invalid.add(Material.BEDROCK.getId());
        invalid.add(Material.SEEDS.getId());
        invalid.add(Material.LEVER.getId());
        invalid.add(Material.RAILS.getId());
        invalid.add(Material.POWERED_RAIL.getId());
        invalid.add(Material.PORTAL.getId());
        invalid.add(Material.LAVA.getId());
        invalid.add(Material.WATER.getId());
        invalid.add(Material.CAKE.getId());
        invalid.add(Material.BED_BLOCK.getId());
        invalid.add(Material.MOB_SPAWNER.getId());
        invalid.add(Material.FURNACE.getId());
        invalid.add(Material.CHEST.getId());
        invalid.add(Material.DISPENSER.getId());
        invalid.add(Material.BURNING_FURNACE.getId());
        invalid.add(Material.STATIONARY_LAVA.getId());
        invalid.add(Material.STATIONARY_WATER.getId());
        invalid.add(Material.SAND.getId());
        invalid.add(Material.GRAVEL.getId());
        invalid.add(Material.SAPLING.getId());
        invalid.add(Material.LEAVES.getId());
        invalid.add(Material.YELLOW_FLOWER.getId());
        invalid.add(Material.RED_ROSE.getId());
        invalid.add(Material.RED_MUSHROOM.getId());
        invalid.add(Material.BROWN_MUSHROOM.getId());
        invalid.add(Material.REDSTONE_TORCH_OFF.getId());
        invalid.add(Material.REDSTONE_TORCH_ON.getId());
        invalid.add(Material.FIRE.getId());
        invalid.add(Material.REDSTONE_WIRE.getId());
        invalid.add(Material.WORKBENCH.getId());
        conf = getConfiguration();
        reload();
        pdfFile = this.getDescription();
        //   playerListener.setPointers(getConfiguration(), permissions);
        PluginManager tmp = getServer().getPluginManager();
        //tmp.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Priority.Normal, this);
        //tmp.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Normal, this);
        //tmp.registerEvent(Event.Type.BLOCK_PLACE, blockListener, Priority.Normal, this);
        tmp.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
        tmp.registerEvent(Event.Type.SIGN_CHANGE, blockListener, Priority.Normal, this);
        System.out.println(pdfFile.getName() + " version "
                + pdfFile.getVersion() + " is enabled!");
    }

    public void reload() {
        selected = new HashMap<Player, PlayerStorage>();
        boards = new HashMap<World, LinkedList<Board>>();
        setupPermissions();
        for (World world : getServer().getWorlds()) {
            LinkedList<Board> tmp = new LinkedList<Board>();
            boards.put(world, tmp);
            Map<String, ConfigurationNode> nodes = conf.getNodes(String.format("boards.%s", world.getName()));
            if (nodes != null) {
                for (String node : nodes.keySet()) {
                    tmp.add(new Board(node, world, null, null, true, this, conf));
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            PlayerStorage store = selected.get(pl);
            if (store == null) {
                store = new PlayerStorage();
                selected.put(pl, store);
            }
            int l = args.length;
            if (l >= 1) {
                if (args[0].equalsIgnoreCase("node") || args[0].equalsIgnoreCase("n")) {
                    if (l == 1) {
                        posInfo(store.pos1, "Node 1", pl);
                        posInfo(store.pos2, "Node 2", pl);
                    } else {
                        if (args[1].equals("1")) {
                            store.pos1 = pl.getLocation();
                            posInfo(store.pos1, "Node 1", pl);
                        } else if (args[1].equals("2")) {
                            store.pos2 = pl.getLocation();
                            posInfo(store.pos2, "Node 2", pl);
                        } else {
                            pl.sendMessage(ChatColor.RED + "Argument must be 1 or 2.");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("addboard") || args[0].equalsIgnoreCase("ab")) {
                    if (store.pos1 == null || store.pos2 == null) {
                        pl.sendMessage("Invalid position.");
                        posInfo(store.pos1, "Node 1", pl);
                        posInfo(store.pos2, "Node 2", pl);
                        return true;
                    } else if (store.pos1.getWorld() != store.pos2.getWorld()) {
                        pl.sendMessage("Locations are in different worlds");
                        posInfo(store.pos1, "Node 1", pl);
                        posInfo(store.pos2, "Node 2", pl);
                        return true;
                    }
                    if (l == 2) {
                        if (findBoard(args[1]) == null) {
                            store.board = new Board(args[1], store.pos1.getWorld(), store.pos1, store.pos2, false, this, conf);
                            boards.get(store.pos1.getWorld()).add(store.board);
                            pl.sendMessage(ChatColor.GREEN+"New board created and selected.");
                        } else {
                            pl.sendMessage(ChatColor.RED+"Board with that name already exists!");
                        }
                    } else {
                        pl.sendMessage("Please provide area-name.");
                    }

                } else if (args[0].equalsIgnoreCase("deleteboard") || args[0].equalsIgnoreCase("db")) {
                    if (l == 2) {
                        Board tmp = findBoard(args[1]);
                        if (tmp != null) {
                            boards.get(tmp.startblock.getWorld()).remove(tmp);
                            conf.removeProperty(String.format("boards.%s.%s",tmp.startblock.getWorld().getName(), tmp.name));
                            conf.save();
                            pl.sendMessage(ChatColor.GREEN + "Removed board.");
                        } else {
                            pl.sendMessage(ChatColor.RED + "No such board.");
                        }
                    } else {
                        pl.sendMessage(ChatColor.RED + "Please provide board-name");
                    }
                } else if (args[0].equalsIgnoreCase("addtype") || args[0].equalsIgnoreCase("at")) {
                    if (store.board != null) {
                        if (l == 2) {
                            store.type = store.board.addCell(toInt(args[1]), pl);
                        } else {
                            pl.sendMessage(ChatColor.RED + "Please provide material type.");
                        }
                    } else {
                        pl.sendMessage(ChatColor.RED + "No board selected, please select one.");
                    }
                } else if (args[0].equalsIgnoreCase("select") ||  args[0].equalsIgnoreCase("s")) {
                    if (l >= 2) {
                        store.board = findBoard(args[1]);
                        if (store.board == null) {
                            pl.sendMessage(ChatColor.RED + "Not found.");
                        } else {
                            pl.sendMessage(ChatColor.GREEN + "Selected.");
                        }
                    }
                } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("r")) {
                    reload();
                } else if (args[0].equalsIgnoreCase("info") || args[0].equalsIgnoreCase("i")) {
                    if (checkBoard(store, pl)) {
                        store.board.info(pl);
                    } else {
                    }
                } else if (args[0].equalsIgnoreCase("iterateboard") || args[0].equalsIgnoreCase("ib")) {
                    if (checkBoard(store, pl)) {
                        if (l == 2) {
                            store.board.GOL(null, toInt(args[1]));
                        } else {
                            store.board.GOL(null, 1);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("default") || args[0].equalsIgnoreCase("d")) {
                    if (checkBoard(store, pl)) {
                        if (l == 2) {
                            store.board.changeDefault(toInt(args[1  ]), pl);
                        } else {
                            store.board.info(pl);
                        }
                    }
                } else if (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("c")) {
                    if (checkBoard(store, pl)) {
                        store.board.clear(null);
                    }
                } else if (args[0].equalsIgnoreCase("deletetype") || args[0].equalsIgnoreCase("dt")) {
                    if (checkBoard(store, pl)) {
                        if (l == 2) {
                            if (store.type == store.board.findCell(toInt(args[1]))) {
                                store.type = null;
                            }
                            store.board.removeCell(toInt(args[1]), pl);
                        }
                    }
                } else {
                    pl.sendMessage("No such command.");
                }
            }
        }
        return true;
    }

    public Board findBoard(String bname) {
        for (World w : boards.keySet()) {
            for (Board brd : boards.get(w)) {
                if (brd.name.equalsIgnoreCase(bname)) {
                    return brd;
                }
            }
        }
        return null;
    }

    public boolean checkBoard(PlayerStorage store, Player pl) {
        if (store.board == null) {
            pl.sendMessage(ChatColor.RED + "No board selected, please select one.");
            return false;
        }
        return true;
    }
    public boolean checkNode(PlayerStorage store, Player pl) {
        return true;
    }
    public boolean checkType(PlayerStorage store, Player pl) {
        return true;
    }
    public void posInfo(Location pos, String name, Player pl) {
        if (pos != null) {
            pl.sendMessage(ChatColor.YELLOW + String.format("%s: (%d, %d, %d) in %s", 
                    name, pos.getBlockX(), pos.getBlockY(), pos.getBlockZ(), pos.getWorld().getName()));
        } else {
            pl.sendMessage(ChatColor.RED + String.format("%s is not set.", name));
        }
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
        int out = -1;
        if (checkInt(in) && in.length() != 0) {
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

    public boolean isValidMaterial(int m) {
        if (Material.getMaterial(m) == null || invalid.contains(m) || m > 255) {
            return false;
        }
        return true;
    }

    class PlayerStorage {
        Location pos1, pos2;
        Board board;
        CellType type;
        public PlayerStorage() {
            pos1 = pos2 = null;
            board = null;
            type = null;
        }
    }
}