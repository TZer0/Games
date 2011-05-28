/*
 * 
 */
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

public class Board {
    int def;

    int x, z;
    int [][]field;
    Games plugin;
    Block startblock;
    LinkedList<CellType> races;
    Configuration conf;
    String pre;
    String name;
    public Board(String name, World world, Location pos1, Location pos2, boolean imported, Games plugin, Configuration conf) {
        races = new LinkedList<CellType>();
        this.conf = conf;
        this.plugin = plugin;
        pre = String.format("boards.%s.%s.", world.getName(), name);
        if (imported) {
            this.x = conf.getInt(pre+"x", 0);
            this.z = conf.getInt(pre+"z", 0);
            this.def = conf.getInt(pre+"def", 20);
            startblock = world.getBlockAt(conf.getInt(pre+"sx",-1), conf.getInt(pre+"sy",-1), conf.getInt(pre+"sz",-1));
            Map<String, ConfigurationNode> types = conf.getNodes(pre+"types");
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
            move(pos1, pos2);
            save();
        }
        field = new int[x][z];
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                if (imported) {
                    field[i][j] = startblock.getRelative(i, 0, j).getTypeId();
                    boolean found = false;
                    for (CellType type : races) {
                        if (type.material == field[i][j]) {
                            found = true;
                        }
                    }
                    if (!found) {
                        field[i][j] = def;
                    }
                } else {
                    field[i][j] = def;                    
                }
            }
        }
        update();
        this.name = name;
    }

    public void save() {
        conf.setProperty(pre+"x", x);
        conf.setProperty(pre+"z", z);
        conf.setProperty(pre+"def", def);
        conf.setProperty(pre+"sx", startblock.getX());
        conf.setProperty(pre+"sy", startblock.getY());
        conf.setProperty(pre+"sz", startblock.getZ());
        conf.save();
    }

    public void move(Location pos1, Location pos2) {
        int xmin, zmin, xmax, zmax, ylevel;
        xmin = Math.min(pos1.getBlockX(), pos2.getBlockX());
        zmin = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        xmax = Math.max(pos1.getBlockX(), pos2.getBlockX()) + 1;
        zmax = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + 1;
        ylevel = Math.min(pos1.getBlockY(), pos2.getBlockY())-1;
        x = Math.abs(xmax-xmin);
        z = Math.abs(zmax-zmin);
        startblock = pos1.getWorld().getBlockAt(xmin, ylevel, zmin);
    }
    public void update() {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                startblock.getRelative(i, 0, j).setTypeId(field[i][j]);
            }
        }
    }

    public void clear(Player pl) {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                field[i][j] = def;
            }
        }
        update();
    }

    public void GOL(Player pl, int iter) {
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
                    if (field[i][j] != def) {
                        if (!calcmap.get(field[i][j])[i][j]) {
                            field[i][j] = def;
                        }

                    }
                    if (field[i][j] == def) {
                        for (int t : calcmap.keySet()) {
                            if (calcmap.get(t)[i][j]) {
                                if (field[i][j] == def) {
                                    field[i][j] = t;
                                } else {
                                    field[i][j] = def;
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


    public boolean[][] analyze(CellType type) {
        boolean [][]tmp = new boolean[x][z];
        int cells;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                cells = getGOLCells(i, j, type.material);
                if (field[i][j] == type.material) {
                    if (cells > type.survMax || cells <= type.survMin) {
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
                if (field[i+xc][j+zc] == mat) {
                    cells += 1;
                }
            }
        }
        return cells;
    }
    public boolean isInBoard(Block pos) {
        if (pos.getY() == startblock.getY() && pos.getX() >= startblock.getX() && pos.getX() < (startblock.getX()+x) &&
                pos.getZ() >= startblock.getZ() && pos.getZ() < (startblock.getZ()+z)) {
            return true;
        }
        return false;
    }
    public void modField(Block pos) {
        int cx = pos.getX()-startblock.getX();
        int cz = pos.getZ()-startblock.getZ();
        nextType(cx, cz);
        update();
    }

    public void nextType(int cx, int cz) {
        if (field[cx][cz] == def) {
            if (races.size() != 0) {
                field[cx][cz] = races.get(0).material;
            }
        } else {
            for (int i = 0; i < races.size(); i++ ) {
                if (races.get(i).material == field[cx][cz]) {
                    if (i != races.size()-1) {
                        field[cx][cz] = races.get(i + 1).material;
                    } else {
                        field[cx][cz] = def;
                    }
                    return;
                }
            }
            field[cx][cz] = def;
        }
    }

    public void info(Player pl) {
        pl.sendMessage(ChatColor.GREEN + String.format("Name: %s", name));
        pl.sendMessage(ChatColor.GREEN + String.format("Location: %d-%d,%d,%d-%d", startblock.getX(), 
                startblock.getX()+x, startblock.getY(), startblock.getZ(), startblock.getZ()+z));
        pl.sendMessage(ChatColor.GREEN + String.format("Default-block (empty): %d", def));
        if (races.size() != 0) {
            pl.sendMessage(ChatColor.GREEN + "Types on this board:");
            for (CellType tmp : races) {
                pl.sendMessage(ChatColor.GREEN + 
                        String.format("Material: %d, cmin: %d, cmax: %d, smin: %d, smax: %d",
                                tmp.material, tmp.creaMin, tmp.creaMax, tmp.survMin, tmp.survMax));
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
                if (field[i][j] == def) {
                    field[i][j] = newdef;
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
            survMax = 3;
            save();
        }
        public CellType(int material, ConfigurationNode node) {
            this.material = material;
            savepre = pre + "types." + material + "."; 
            creaMin = conf.getInt(savepre + "cmin" , 3);
            creaMax = conf.getInt(savepre + "cmax" , 3);
            survMin = conf.getInt(savepre + "smin" , 1);
            survMax = conf.getInt(savepre + "smax" , 3);
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
                    if (field[i][j] == material) {
                        field[i][j] = def; 
                    }
                }
            }
            update();
        }
    }
}
