package tzer0.Games;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;


public class GOL  extends Board implements Interactable {
    int def;
    LinkedList<CellType> races;

    public GOL(String name, World world, Location pos1, Location pos2,
            boolean imported, Games plugin, Configuration conf) {
        super(name, world, pos1, pos2, imported, plugin, conf);
        this.type = "gol";
        y = 1;
        races = new LinkedList<CellType>();
        if (imported) {
            Map<String, ConfigurationNode> types = conf.getNodes(pre+"types");
            this.def = conf.getInt(pre+"def", 20);

            if (types != null) {
                for (String tmp : types.keySet()) {
                    if (plugin.isValidMaterial(plugin.toInt(tmp))) {
                        races.add(new CellType(plugin.toInt(tmp), types.get(tmp)));
                    } else {
                        conf.removeProperty(pre + "types." + tmp);
                        conf.save();
                    }
                }
            }
        } else {
            this.def = 20;
        }
        field = new int[y][x][z];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                if (imported) {
                    field[0][i][j] = startblock.getRelative(i, 0, j).getTypeId();
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
    public void iterate(Player pl, int iter) {
        if (iter <= 0) {
            iter = 1;
        }
        for (int k = 0; k < iter; k ++) {
            HashMap<Integer, boolean[][]> calcmap = new HashMap<Integer, boolean[][]>();
            for (CellType tmp : races) {
                calcmap.put(tmp.material, analyze(tmp));
            }
            boolean [][]changed = new boolean[x][z];
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
                    if (field[0][i][j] == def) {
                        for (int t : calcmap.keySet()) {
                            if (calcmap.get(t)[i][j]) {
                                if (field[0][i][j] == def) {
                                    field[0][i][j] = t;
                                } else {
                                    field[0][i][j] = def;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        update();
    }

    public void CircuitBoard(Block pos) {
        pos.setType(Material.REDSTONE_TORCH_ON);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
            }
        }
    }

    public boolean[][] analyze(CellType type) {
        boolean [][]tmp = new boolean[x][z];
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
                if (xc + i < 0 || xc + i >= x || zc + j < 0 || zc + j >= z || (i == 0 && j == 0)) {
                    continue;
                }
                if (field[0][i+xc][j+zc] == mat) {
                    cells += 1;
                }
            }
        }
        return cells;
    }
    public void modField(Block pos) {
        int cx = pos.getX()-startblock.getX();
        int cz = pos.getZ()-startblock.getZ();
        nextType(cx, cz);
        update();
    }

    public void nextType(int cx, int cz) {
        if (field[0][cx][cz] == def) {
            if (races.size() != 0) {
                field[0][cx][cz] = races.get(0).material;
            }
        } else {
            for (int i = 0; i < races.size(); i++ ) {
                if (races.get(i).material == field[0][cx][cz]) {
                    if (i != races.size()-1) {
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
        pl.sendMessage(ChatColor.GREEN + String.format("Default-block (empty): %d", def));
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
            pl.sendMessage(ChatColor.RED + "New cell-type can't be same as default.");
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
        if ((pos.getY() == startblock.getY() || pos.getY() == startblock.getY() +1) && pos.getX() >= startblock.getX() && pos.getX() < (startblock.getX()+x) &&
                pos.getZ() >= startblock.getZ() && pos.getZ() < (startblock.getZ()+z)) {
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
        public CellType(int material) {
            this.material = material;
            savepre = pre + "types." + material + "."; 
            creaMin = 3;
            creaMax = 3;
            survMin = 1;
            survMax = 4;
            save();
        }
        public CellType(int material, ConfigurationNode node) {
            this.material = material;
            savepre = pre + "types." + material + "."; 
            creaMin = conf.getInt(savepre + "cmin" , 3);
            creaMax = conf.getInt(savepre + "cmax" , 3);
            survMin = conf.getInt(savepre + "smin" , 1);
            survMax = conf.getInt(savepre + "smax" , 4);
            def = conf.getInt(savepre + "def" , 20);
        }
        public void save() {
            conf.setProperty(savepre + "cmin" , creaMin);
            conf.setProperty(savepre + "cmax" , creaMax);
            conf.setProperty(savepre + "smin" , survMin);
            conf.setProperty(savepre + "smax" , survMax);
            conf.setProperty(savepre + "def" , def);
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

        public void info(Player pl) {
            pl.sendMessage(ChatColor.GREEN + 
                    String.format("Material: %d, cmin: %d, cmax: %d, smin: %d, smax: %d",
                            material, creaMin, creaMax, survMin, survMax));
        }
    }
}
