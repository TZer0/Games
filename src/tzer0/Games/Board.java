package tzer0.Games;

import org.bukkit.block.Block;

public class Board {
    int[] cells;
    int def, other;
    int x, z;
    int [][]field;
    Games plugin;
    Block startblock;

    public Board(int def, Block pos1, Block pos2, boolean imported, Games plugin) {
        int xmin, zmin, xmax, zmax, ylevel;
        cells = null;
        xmin = Math.min(pos1.getX(), pos2.getX());
        zmin = Math.min(pos1.getZ(), pos2.getZ());
        xmax = Math.max(pos1.getX(), pos2.getX()) + 1;
        zmax = Math.max(pos1.getZ(), pos2.getZ()) + 1;
        ylevel = Math.min(pos1.getY(), pos2.getY())-1;
        x = Math.abs(xmax-xmin);
        z = Math.abs(zmax-zmin);
        this.plugin = plugin;
        this.def = def;
        this.other = 89;
        field = new int[x][z];
        startblock = pos1.getWorld().getBlockAt(xmin, ylevel, zmin);
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                if (imported) {
                    field[i][j] = startblock.getRelative(i, 0, j).getTypeId();
                } else {
                    field[i][j] = def;                    
                }
            }
        }
    }
    public void update() {
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                startblock.getRelative(i, 0, j).setTypeId(field[i][j]);
            }
        }
    }
    public void GOL() {
        int [][]tmp = new int[x][z];
        int cells = 0;
        for (int i = 0; i < x; i++) {
            for (int j = 0; j < z; j++) {
                cells = getGOLCells(i, j);
                if (field[i][j] == def) {
                    if (cells == 3) {
                        tmp[i][j] = other;
                    } else {
                        tmp[i][j] = def;
                    }
                } else {
                    if (cells > 3 || cells <= 1) {
                        tmp[i][j] = def;
                    } else {
                        tmp[i][j] = other;
                    }
                }
            }
        }
        field = tmp;
        update();
    }
    public int getGOLCells(int xc, int zc) {
        int cells = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (xc + i < 0 || xc + i >= x || zc + j < 0 || zc + j >= z) {
                    continue;
                }
                if (i == 0 && j == 0) {
                    continue;
                }
                if (field[i+xc][j+zc] != def) {
                    cells += 1;
                }
            }
        }
        return cells;
    }
    public void SetupGOL(int []death, int []life, int []type, boolean []ignore) {
    }
    public boolean isInBoard(Block pos) {
        if (pos.getY() == startblock.getY() && pos.getX() >= startblock.getX() && pos.getX() < (startblock.getX()+x) &&
                pos.getZ() >= startblock.getZ() && pos.getZ() < (startblock.getZ()+z)) {
            return true;
        }
        return false;
    }
    public void modField(Block pos) {
        if (field[pos.getX()-startblock.getX()][pos.getZ()-startblock.getZ()] == def) {
            field[pos.getX()-startblock.getX()][pos.getZ()-startblock.getZ()] = other;
        } else {
            field[pos.getX()-startblock.getX()][pos.getZ()-startblock.getZ()] = def;
        }
        update();
    }
}
