package tzer0.Games;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

public class GOL extends Board implements Interactable, SignalReceiver {
    int def;
    LinkedList<CellType> races;
    Block winpos;
    CodeExecutor ex;
    
    public GOL(String name, World world, Location pos1, Location pos2,
            boolean imported, Games plugin, Configuration conf) {
        super(name, world, pos1, pos2, imported, plugin, conf);
        this.type = "gol";
        y = 1;
        races = new LinkedList<CellType>();
        if (imported) {
            int wx, wy, wz;
            wx = conf.getInt(pre + "wx", 0);
            wy = conf.getInt(pre + "wy", -1);
            wz = conf.getInt(pre + "wz", 0);
            if (wy == -1) {
                winpos = null;
            } else {
                winpos = world.getBlockAt(wx, wy, wz);
            }
            Map<String, ConfigurationNode> types = conf.getNodes(pre + "types");
            this.def = conf.getInt(pre + "def", 20);

            if (types != null) {
                for (String tmp : types.keySet()) {
                    if (plugin.isValidMaterial(plugin.toInt(tmp))) {
                        races.add(new CellType(plugin.toInt(tmp), types
                                .get(tmp)));
                    } else {
                        conf.removeProperty(pre + "types." + tmp);
                        conf.save();
                    }
                }
            }
        } else {
            winpos = null;
            this.def = 20;
        }
        field = new int[y][x][z];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                if (imported) {
                    field[0][i][j] = startBlock.getRelative(i, 0, j)
                    .getTypeId();
                    boolean found = false;
                    for (CellType type : races) {
                        if (type.material == field[0][i][j]) {
                            found = true;
                        }
                    }
                    if (!found) {
                        field[0][i][j] = def;
                    }
                } else {
                    field[0][i][j] = def;
                }
            }
        }
        save();
        update();
    }

    public void save() {
        super.save();
        conf.setProperty(pre + "def", def);
        conf.setProperty(pre + "type", type);
        if (winpos != null) {
            conf.setProperty(pre+"wx", winpos.getX());
            conf.setProperty(pre+"wy", winpos.getY());
            conf.setProperty(pre+"wz", winpos.getZ());
        } else {
            conf.removeProperty(pre+"wx");
            conf.removeProperty(pre+"wy");
            conf.removeProperty(pre+"wz");
        }
        conf.save();
    }

    public void clear(Player pl) {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                field[0][i][j] = def;
            }
        }
        update();
    }

    public void handleSignal(Sign sign, Player pl) {
        if (ex != null) {
            ex.interrupt();
        }
        ex = new CodeExecutor(sign, pl);
        ex.start();
    }    

    public void executeCode(LinkedList<String> code, Player pl, CodeExecutor exec) {
        int limit = 10000;
        Storage storage = new Storage();
        LinkedList<FlowNode> wh = new LinkedList<FlowNode>();
        for (int line = 0; line < code.size(); line++) {
            if (line > limit) {
                pl.sendMessage(ChatColor.RED + "Iteration limit reached - terminating!");
                return;
            }
            if (exec.isInterrupted()) {
                break;
            }
            String cmd = code.get(line);
            pl.sendMessage(cmd);
            if (!cmd.equalsIgnoreCase("")) {
                String[] splitCmd = cmd.split(":");
                int l = splitCmd.length;
                if (splitCmd[0].equalsIgnoreCase("if")) {
                    if (!checkParams(splitCmd, 2, 4, pl)) {
                        return;
                    }
                    int res = performTest(storage, splitCmd, pl);
                    if (res == 1) {
                        storage.cifLevel++;
                    } else if (res == -1) {
                        return;
                    } else if (res == 0) {
                    }
                    storage.ifLevel++;

                } else if (splitCmd[0].equalsIgnoreCase("end")) {
                    storage.ifLevel--;
                    if (storage.cifLevel > storage.ifLevel) {
                        storage.cifLevel = storage.ifLevel;
                    }
                } else if (splitCmd[0].equalsIgnoreCase("else") ||  splitCmd[0].equalsIgnoreCase("el")) {
                    if (storage.ifLevel-1 == storage.cifLevel) {
                        storage.cifLevel++;
                    } else {
                        storage.cifLevel--;
                    }
                } else if (storage.cifLevel < storage.ifLevel) {
                    continue;
                } else if (splitCmd[0].equalsIgnoreCase("wh") || splitCmd[0].equalsIgnoreCase("while")) {
                    if (!checkParams(splitCmd, 2, 4, pl)) {
                        return;
                    }
                    int res = performTest(storage, splitCmd, pl);
                    pl.sendMessage(ChatColor.YELLOW + "" + res);
                    if (res == -1) {
                        return;
                    } else if (res == 1) {
                        boolean found = false;
                        for (FlowNode tmp : wh) {
                            if (tmp.line == line) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            continue;
                        } else {
                            wh.addFirst(new FlowNode(true, line));
                        }
                    } else if (res == 0) {
                        boolean found = false;
                        for (FlowNode tmp : wh) {
                            if (tmp.line == line) {
                                tmp.active = false;
                                found = true;
                                break;
                            }
                        }
                        pl.sendMessage(ChatColor.BLUE + "" + found);
                        if (!found) {
                            wh.addFirst(new FlowNode(false, line));
                        }
                    }
                } else if (splitCmd[0].equalsIgnoreCase("wend")) {
                    FlowNode fn = wh.getFirst();
                    if (fn.active) {
                        line = fn.line-1;
                    } else {
                        wh.removeFirst();
                    }
                } else if (wh.size() != 0 && !wh.getFirst().active) {
                    continue;
                } else if (splitCmd[0].equalsIgnoreCase("i")) {
                    if (!checkParams(splitCmd, 1, 2, pl)) {
                        return;
                    }
                    int steps = 1;
                    if (l == 2) {
                        steps = plugin.toInt(splitCmd[1]);
                    }
                    iterate(pl, steps);
                } else if (splitCmd[0].equalsIgnoreCase("win") || splitCmd[0].equalsIgnoreCase("w")) {
                    if (!checkParams(splitCmd, 1, pl)) {
                        return;
                    }
                    if (winpos != null) {
                        winpos.setType(Material.REDSTONE_TORCH_ON);
                    }
                    pl.sendMessage(ChatColor.GREEN + "Congratulations! You win!");
                } else if (splitCmd[0].equalsIgnoreCase("reset") || splitCmd[0].equalsIgnoreCase("r")) {
                    if (!checkParams(splitCmd, 1, pl)) {
                        return;
                    }
                    clear(pl);
                    if (winpos != null) {
                        winpos.setType(Material.AIR);
                    }
                    pl.sendMessage(ChatColor.GREEN + "Cleared.");
                } else if (splitCmd[0].equalsIgnoreCase("s")) {
                    if (!checkParams(splitCmd, 3, pl)) {
                        return;
                    }
                    try {
                        storage.var.put(splitCmd[1], Integer.parseInt(splitCmd[2]));
                    } catch (NumberFormatException e) {
                        pl.sendMessage(ChatColor.RED + 
                                String.format("Invalid assignment, %s is not integer.", 
                                        splitCmd[2]));
                        return;
                    }
                } else if (splitCmd[0].equalsIgnoreCase("g")) {
                    if (!checkParams(splitCmd, 2, pl)) {
                        return;
                    }
                    Integer val = storage.var.get(splitCmd[1]);
                    if (val != null) {
                        pl.sendMessage(ChatColor.YELLOW + String.format("%s is %d", splitCmd[1], val));
                    } else {
                        pl.sendMessage(ChatColor.YELLOW + String.format("%s is not set!"));
                    }
                } else if (splitCmd[0].equalsIgnoreCase("+")) {
                    if (!checkParams(splitCmd, 3, pl)) {
                        return;
                    }
                    if (!modValue(storage, splitCmd, pl, true)) {
                        return;
                    }
                } else if (splitCmd[0].equalsIgnoreCase("-")) {
                    if (!checkParams(splitCmd, 3, pl)) {
                        return;
                    }
                    if (!modValue(storage, splitCmd, pl, false)) {
                        return;
                    }
                } else if (splitCmd[0].equalsIgnoreCase("t")) {
                    pl.sendMessage(ChatColor.RED + "Reached t, terinating.");
                } else {
                    pl.sendMessage(ChatColor.RED + "No such command - " + cmd);
                }
            }
        }
        pl.sendMessage(ChatColor.GREEN + "Done.");
    }

    public int performTest(Storage storage, String[] splitCmd, Player pl) {
        if (splitCmd[1].equalsIgnoreCase("cn")) {
            if (!checkParams(splitCmd, 4, pl)) {
                return -1;
            }
            String out = splitCmd[2];
            if (plugin.toInt(splitCmd[2]) == -1) {
                out = "*";
            }
            if (!checkIfConnected(plugin.toInt(splitCmd[2]), plugin.toInt(splitCmd[3]), pl)) {
                pl.sendMessage(ChatColor.RED + 
                        String.format("Failure! Connector %d was not connected using %s", plugin.toInt(splitCmd[3]), out));
                return 0;
            } else {
                pl.sendMessage(ChatColor.GREEN + 
                        String.format("Success! You connected %d using %s", plugin.toInt(splitCmd[3]), out));
                storage.cifLevel++;
                return 1;
            }
        } else if (splitCmd[1].equalsIgnoreCase("c")) {
            if (!checkParams(splitCmd, 4, pl)) {
                return -1;
            }
            int count = 0;
            int actual = 0;
            try {
                count = Integer.parseInt(splitCmd[3]);
                actual = countCell(Integer.parseInt(splitCmd[2]));
            } catch (NumberFormatException e) {
                pl.sendMessage(ChatColor.RED + String.format("%s or is not an integer", splitCmd[2], splitCmd[3]));
                return -1;
            }
            if (actual != count) {
                pl.sendMessage(ChatColor.RED + String.format("You failed, target: %d, got %d", count, actual));
                return 0;
            } else {
                pl.sendMessage(ChatColor.GREEN + String.format("You passed a test, you had %d of %s!", count, splitCmd[2]));
                storage.cifLevel++;
                return 1;
            }
        } else {
            try {
                int v1 = getVarOrValue(storage, splitCmd[2], pl);
                int v2 = getVarOrValue(storage, splitCmd[3], pl);
                if (splitCmd[1].equalsIgnoreCase("==")) {
                    return (v1 == v2 ? 1 : 0);
                } else if (splitCmd[1].equalsIgnoreCase("<")) {
                    return (v1 < v2 ? 1 : 0);
                } else if (splitCmd[1].equalsIgnoreCase(">")) {
                    return (v1 > v2 ? 1 : 0);
                } else if (splitCmd[1].equalsIgnoreCase("<=")) {
                    return (v1 <= v2 ? 1 : 0);
                } else if (splitCmd[1].equalsIgnoreCase(">=")) {
                    return (v1 >= v2 ? 1 : 0);
                } else if (splitCmd[1].equalsIgnoreCase("!=")) {
                    return (v1 != v2 ? 1 : 0);
                } else {
                    pl.sendMessage(ChatColor.RED + "No such test");
                    return -1;
                }
            } catch (GOLIllegalStateError e) {
                pl.sendMessage(ChatColor.RED + 
                        String.format("No such variable or invalid integer: %s and/or %s",
                                splitCmd[2], splitCmd[3]));
                return -1;
            }
        }
    }

    public boolean modValue(Storage storage, String[] splitCmd, Player pl, boolean action) {
        Integer i = storage.var.get(splitCmd[1]);
        if (i == null) {
            pl.sendMessage(ChatColor.RED + "No such variable.");
            return false;
        }
        try {
            int mod = Integer.parseInt(splitCmd[2]);
            storage.var.put(splitCmd[1], i + (action ? mod : -mod));
        } catch (NumberFormatException e) {
            pl.sendMessage(ChatColor.RED + String.format("%s is not an integer!", splitCmd[1]));
            return false;
        }
        return true;
    }

    public int getVarOrValue(Storage storage, String target, Player pl) throws GOLIllegalStateError {
        Integer out;
        try {
            out = Integer.parseInt(target);
            return out;
        } catch (NumberFormatException e) {
            out = storage.var.get(target);
            if (out == null) {
                throw new GOLIllegalStateError();
            }
            return out;
        }
    }

    public boolean checkParams(String[] splitCmd, int num, Player pl) {
        boolean ret = splitCmd.length == num;
        if (!ret) {
            pl.sendMessage(ChatColor.RED + String.format("Error in count for %s, requires %d, got %d!", 
                    splitCmd[0], num, splitCmd.length));
        }
        return ret;
    }
    public boolean checkParams(String[] splitCmd, int fnum, int tnum, Player pl) {
        boolean ret = splitCmd.length <= tnum && splitCmd.length >= fnum;
        if (!ret) {
            pl.sendMessage(ChatColor.RED + String.format("Error in count for %s, requires between %d and %d, got %d!", 
                    splitCmd[0], fnum, tnum, splitCmd.length));
        }
        return ret;
    }
    public LinkedList<String> getCode(LinkedList<Sign> included, Sign current, int from, Player pl) throws GOLIllegalIncludeException, GOLIllegalStateError {
        if (included.contains(current)) {
            throw new GOLIllegalIncludeException(current);
        }
        included.push(current);
        String lines[] = current.getLines();
        LinkedList<String> out = new LinkedList<String>();
        for (int i = from; i < 4; i++) {
            for (String cmd : lines[i].split(",")) {
                if (cmd.contains("e:") || cmd.contains("exec:")) {
                    String splitCmd[] = cmd.split(":");
                    if (splitCmd.length != 4) {
                        pl.sendMessage(ChatColor.RED + "Error in count - invalid arg-count (needs 4)");
                        throw new GOLIllegalStateError();
                    }
                    Block bl = current.getBlock().getRelative(plugin.toInt(splitCmd[1]),
                            plugin.toInt(splitCmd[2]), plugin.toInt(splitCmd[3]));
                    if (bl.getType() == Material.SIGN_POST || bl.getType() == Material.WALL_SIGN) {
                        Sign newSign = (Sign) bl.getState();
                        if (newSign.getLine(0).equalsIgnoreCase(ChatColor.DARK_GREEN + "[exp]")) {
                            for (String tcmd : getCode(included, newSign, 1, pl)) {
                                out.addLast(tcmd);
                            }
                            included.remove(newSign);
                        } else {
                            pl.sendMessage(ChatColor.RED + "First line must be " + ChatColor.DARK_GREEN + "[EXP]");
                            throw new GOLIllegalStateError();
                        }
                    } else {
                        pl.sendMessage(ChatColor.RED + "Target is not a sign!");
                        throw new GOLIllegalStateError();
                    }
                } else {
                    out.addLast(cmd);
                }
            }
        }

        return out;
    }

    public boolean checkIfConnected(int type, int sign, Player pl) {
        Block pos1 = null, pos2 = null;
        boolean done = false;
        for (int i = 0; i < x && !done; i++) {
            for (int j = 0; j < z; j++) {
                Block bl = startBlock.getRelative(i, 1, j);
                if (bl.getType() == Material.SIGN_POST) {
                    Sign s = (Sign) bl.getState();
                    String[] lines = s.getLines();
                    if (lines[0].equalsIgnoreCase(ChatColor.DARK_GREEN+"[conn]") && lines[1].equalsIgnoreCase(name)
                            && lines[2].equalsIgnoreCase(""+sign)) {
                        Block nb = startBlock.getRelative(i, 0, j);
                        if (type == -1) {
                            if (nb.getTypeId() == def) {
                                return false;
                            }
                        } else {
                            if (nb.getTypeId() != type) {
                                return false;
                            }
                        }
                        if (pos1 == null) {
                            pos1 = nb;
                        } else {
                            pos2 = nb;
                            done = true;
                            break;
                        }
                    }
                }
            }
        }
        if (pos2 == null) {
            pl.sendMessage(ChatColor.RED + "Could not find one or more connectors");
            return false;
        } else {
            boolean [][]visited = new boolean[x][z];
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < z; j++) {
                    visited[i][j] = false;
                }
            }
            return fastFill(pos1.getX()-startBlock.getX(), pos1.getZ()-startBlock.getZ(),
                    pos2.getX()-startBlock.getX(), pos2.getZ()-startBlock.getZ(), 
                    type, visited);
        }
    }

    public boolean fastFill(int cx, int cz, int tx, int tz, int type, boolean [][]visited) {
        if (cx < 0 || cx >= x || cz < 0 || cz >= z) {
            return false;
        } else {
            if (visited[cx][cz]) {
                return false;
            } else if (cx == tx && cz == tz) {
                return true;
            } else {
                visited[cx][cz] = true;
                if (type == -1) {
                    if (field[0][cx][cz] != def) {
                        for (int i = 0; i < 4; i++) {
                            if (fastFill(modX(cz, i), modZ(cz,i), tx, tz, type, visited)) {
                                return true;
                            }
                        }
                    }
                    return false;
                } else {
                    if (field[0][cx][cz] == type) {
                        for (int i = 0; i < 4; i++) {
                            if (fastFill(modX(cx, i), modZ(cz, i), tx, tz, type, visited)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }
        }
    }

    public int modX(int cx, int i) {
        return cx + (i == 0 ? -1 : 0) + (i == 1 ? 1 : 0);
    }

    public int modZ(int cz, int i) {
        return cz + (i == 2 ? -1 : 0) + (i == 3 ? 1 : 0);
    }

    public int countCell(int type) {
        int sum = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                if (field[0][i][j] == type) {
                    sum++;
                }
            }
        }
        return sum;
    }

    public void iterate(Player pl, int iter) {
        if (iter <= 0) {
            iter = 1;
        }
        for (int k = 0; k < iter; k++) {
            HashMap<Integer, boolean[][]> calcmap = new HashMap<Integer, boolean[][]>();
            for (CellType tmp : races) {
                calcmap.put(tmp.material, analyze(tmp));
            }
            boolean[][] changed = new boolean[x][z];
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < z; j++) {
                    changed[i][j] = false;
                }
            }
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < z; j++) {
                    if (field[0][i][j] != def) {
                        if (!calcmap.get(field[0][i][j])[i][j]) {
                            field[0][i][j] = def;
                        }
                    }
                    field[0][i][j] = contestField(i, j, field[0][i][j], calcmap);
                }
            }
        }
        update();
    }

    public int contestField(int i, int j, int type, HashMap<Integer, boolean[][]> calcmap) {
        boolean found = false;
        if (type != def) {
            for (int weak : findCell(type).weakness) {
                if (calcmap.get(weak)[i][j]) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                return type;
            }
        }

        LinkedList<Integer> contesters = new LinkedList<Integer>();
        LinkedList<Integer> finalContesters = new LinkedList<Integer>();
        for (CellType ty : races) {
            if (calcmap.get(ty.material)[i][j]) {
                contesters.add(ty.material);
            }
        }

        for (int k : contesters) {
            CellType temp = findCell(k);
            found = false;
            for (int l : contesters) {
                if (temp.weakness.contains(l)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                finalContesters.add(k);
            }
        }
        if (finalContesters.size() == 1) {
            return finalContesters.get(0);
        } else {
            return def;
        }
    }

    public void CircuitBoard(Block pos) {
        pos.setType(Material.REDSTONE_TORCH_ON);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
            }
        }
    }

    public boolean[][] analyze(CellType type) {
        boolean[][] tmp = new boolean[x][z];
        int cells;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                cells = getGOLCells(i, j, type.material);
                if (field[0][i][j] == type.material) {
                    if (cells >= type.survMax || cells <= type.survMin) {
                        tmp[i][j] = false;
                    } else {
                        tmp[i][j] = true;
                    }
                } else {
                    if (cells >= type.creaMin && cells <= type.creaMax) {
                        tmp[i][j] = true;
                    } else {
                        tmp[i][j] = false;
                    }
                }
            }
        }
        return tmp;
    }

    public int getGOLCells(int xc, int zc, int mat) {
        int cells = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (xc + i < 0 || xc + i >= x || zc + j < 0 || zc + j >= z
                        || (i == 0 && j == 0)) {
                    continue;
                }
                if (field[0][i + xc][j + zc] == mat) {
                    cells += 1;
                }
            }
        }
        return cells;
    }

    public void modField(Block pos) {
        int cx = pos.getX() - startBlock.getX();
        int cz = pos.getZ() - startBlock.getZ();
        nextType(cx, cz);
        update();
    }

    public void nextType(int cx, int cz) {
        if (field[0][cx][cz] == def) {
            if (races.size() != 0) {
                field[0][cx][cz] = races.get(0).material;
            }
        } else {
            for (int i = 0; i < races.size(); i++) {
                if (races.get(i).material == field[0][cx][cz]) {
                    if (i != races.size() - 1) {
                        field[0][cx][cz] = races.get(i + 1).material;
                    } else {
                        field[0][cx][cz] = def;
                    }
                    return;
                }
            }
            field[0][cx][cz] = def;
        }
    }

    public void info(Player pl) {
        super.info(pl);
        pl.sendMessage(ChatColor.GREEN + "Type: Game of Life");
        pl.sendMessage(ChatColor.GREEN
                + String.format("Default-block (empty): %d", def));
        if (winpos != null) {
            pl.sendMessage(ChatColor.YELLOW + String.format("Winpos is set to (%d, %d, %d)", 
                    winpos.getX(), winpos.getY(), winpos.getZ()));
        } else {
            pl.sendMessage(ChatColor.RED + "No winpos set.");
        }
        if (races.size() != 0) {
            pl.sendMessage(ChatColor.GREEN + "Types on this board:");
            for (CellType tmp : races) {
                tmp.info(pl);
            }
        }
    }

    public void changeDefault(int newdef, Player pl) {
        if (findCell(newdef) != null) {
            pl.sendMessage(ChatColor.RED + "Type already exists!");
            return;
        } else if (!plugin.isValidMaterial(newdef)) {
            pl.sendMessage(ChatColor.RED + "Invalid type.");
            return;
        }
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                if (field[0][i][j] == def) {
                    field[0][i][j] = newdef;
                }
            }
        }
        def = newdef;
        save();
        update();
    }

    public CellType addCell(int cell, Player pl) {
        if (findCell(cell) != null) {
            pl.sendMessage(ChatColor.RED + "Type already exists!");
            return null;
        } else if (!plugin.isValidMaterial(cell)) {
            pl.sendMessage(ChatColor.RED + "Invalid type.");
            return null;
        } else if (def == cell) {
            pl.sendMessage(ChatColor.RED
                    + "New cell-type can't be same as default.");
            return null;
        }
        pl.sendMessage(ChatColor.GREEN + "Added and selected.");
        CellType tmp = new CellType(cell);
        races.add(tmp);
        return tmp;

    }

    public void removeCell(int cell, Player pl) {
        CellType tmp = findCell(cell);
        if (tmp != null) {
            tmp.remove();
            races.remove(tmp);
            pl.sendMessage(ChatColor.GREEN + "Removed.");
        } else {
            pl.sendMessage(ChatColor.RED + "No such type.");
        }
    }

    public CellType findCell(int cell) {
        for (CellType tmp : races) {
            if (tmp.material == cell) {
                return tmp;
            }
        }
        return null;
    }

    public CellType selectCell(int cell, Player pl) {
        CellType tmp = findCell(cell);
        if (tmp != null) {
            pl.sendMessage(ChatColor.GREEN + "Selected.");
        } else {
            pl.sendMessage(ChatColor.RED + "Could not find type.");
        }
        return tmp;
    }

    public boolean isInBoard(Block pos) {
        if ((pos.getY() == startBlock.getY()
                || pos.getY() == startBlock.getY() + 1 || pos.getY() == startBlock
                .getY() - 1)
                && pos.getX() >= startBlock.getX()
                && pos.getX() < (startBlock.getX() + x)
                && pos.getZ() >= startBlock.getZ()
                && pos.getZ() < (startBlock.getZ() + z)) {
            return true;
        }
        return false;
    }

    public void interact(Block pos, Player pl) {
        modField(pos);
    }

    class CellType {
        int creaMin, creaMax, survMin, survMax;
        int material;
        String savepre;
        List<Integer> weakness;

        public CellType(int material) {
            this.material = material;
            savepre = pre + "types." + material + ".";
            weakness = conf.getIntList(savepre + "kills", null);
            creaMin = 3;
            creaMax = 3;
            survMin = 1;
            survMax = 4;
            save();
        }

        public CellType(int material, ConfigurationNode node) {
            this.material = material;
            savepre = pre + "types." + material + ".";
            creaMin = conf.getInt(savepre + "cmin", 3);
            creaMax = conf.getInt(savepre + "cmax", 3);
            survMin = conf.getInt(savepre + "smin", 1);
            survMax = conf.getInt(savepre + "smax", 4);
            weakness = conf.getIntList(savepre + "weakness", null);
            save();
        }

        public void save() {
            conf.setProperty(savepre + "cmin", creaMin);
            conf.setProperty(savepre + "cmax", creaMax);
            conf.setProperty(savepre + "smin", survMin);
            conf.setProperty(savepre + "smax", survMax);
            conf.setProperty(savepre + "weakness", weakness);
            conf.save();
        }

        public void remove() {
            conf.removeProperty(pre + "types." + material);
            conf.save();
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < z; j++) {
                    if (field[0][i][j] == material) {
                        field[0][i][j] = def;
                    }
                }
            }
            update();
        }

        public void removeOther(int i, Player pl) {
            if (!weakness.contains(i)) {
                if (pl != null) {
                    pl.sendMessage(ChatColor.RED + "No such cell-type in list.");
                }
            } else {
                if (pl != null) {
                    pl.sendMessage(ChatColor.GREEN + "Done.");
                    info(pl);
                }
                weakness.remove(i);
                save();
            }
        }

        public void addOther(int i, Player pl) {
            if (i == material) {
                if (pl != null) {
                    pl.sendMessage("Can't have priority under self.");
                }
            } else {
                if (findCell(i) != null) {
                    weakness.add(i);
                    save();
                } else {
                    pl.sendMessage("No such type available");
                }
            }
        }

        public void info(Player pl) {

            if (pl != null) {
                pl.sendMessage(ChatColor.GREEN
                        + String.format(
                                "Material: %d, cmin: %d, cmax: %d, smin: %d, smax: %d",
                                material, creaMin, creaMax, survMin, survMax));
                if (weakness.size() != 0) {
                    String out = "";
                    for (int i = 0; i < weakness.size(); i++) {
                        if (i != 0) {
                            out += ", ";
                        }
                        out += weakness.get(i);
                    }
                    pl.sendMessage(ChatColor.YELLOW + String.format("Has priority under: %s", out));
                }
            }
        }
    }

    class GOLIllegalIncludeException extends Exception {
        Sign sg;
        public GOLIllegalIncludeException(Sign sg) {
            this.sg = sg;
        }
        /**
         * 
         */
        private static final long serialVersionUID = 7692458386475734831L;
    }

    class GOLIllegalStateError extends Exception {

        /**
         * 
         */
        private static final long serialVersionUID = -8737217633680412889L;

    }

    class CodeExecutor extends Thread {
        Sign sign;
        Player pl;
        public CodeExecutor(Sign sign, Player pl) {
            this.sign = sign;
            this.pl = pl;
        }
        public void run() {
            try {
                LinkedList<String> code = getCode(new LinkedList<Sign>(), sign, 2, pl);
                executeCode(code, pl, this);
            } catch (GOLIllegalIncludeException e) {
                Block bl = e.sg.getBlock();
                pl.sendMessage(ChatColor.RED + "You have a circular dependency.");
                pl.sendMessage(ChatColor.RED + String.format("The sign at (%d,%d,%d) has been included in itself.",
                        bl.getX(), bl.getY(), bl.getZ()));
            } catch (GOLIllegalStateError e) {
            }
        }
    }

    class FlowNode {
        boolean active;
        int line;
        public FlowNode(boolean active, int line) {
            this.active = active;
            this.line = line;
        }
    }
    
    class Storage {
        HashMap<String, Integer> var;
        int ifLevel,  cifLevel;
        public Storage() {
            ifLevel = cifLevel = 0;
            var = new HashMap<String, Integer>();
        }
    }
}