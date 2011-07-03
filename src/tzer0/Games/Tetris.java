package tzer0.Games;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class Tetris extends Board implements SignalReceiver {
    char color1, color2;
    int blockmin, blockmax;
    char tdir;
    boolean running;
    int gr;
    int p;
    Sign points;
    LinkedList<FallingBrick> falling;
    public Tetris(String name, World world, Location pos1, Location pos2,
            boolean imported, Games plugin, Configuration conf) {
        super(name, world, pos1, pos2, imported, plugin, conf);
        points = null;
        gr = -1;
        running = true;
        blockmin = conf.getInt(pre + "bmin", 4);
        blockmax = conf.getInt(pre + "bmax", 4);
        this.type = "Tetris";
        save();
        color1 = 0;
        tdir = 0;
        p = 0;
        color2 = 15;
        this.field = new int[y][x][z];
        this.data = new byte[y][x][z];
        falling = new LinkedList<FallingBrick>();
        initEmpty();
        update();
    }

    public void addBlock() {
        if (x*y*z < blockmax) {
            return;
        }
        int target = blockmin + (int) (Math.random()*(blockmax-blockmin))-1;
        filledDetect();

        falling.clear();
        byte d = (byte) (Math.random()*16);
        falling.add(new FallingBrick(startY(), startX(), startZ(), d));
        for (int i = 0; i < target;) {
            if (mutate(d)) {
                if (running) {
                    i++;
                } else {
                    break;
                }
            }
        }
        updateFalling();
    }

    public void filledDetect() {
        for (int i = 0; i < dir1Selector(tdir, y, x, z); i++) {
            boolean found = false;
            if (tdir == 0 || tdir == 1) {
                if (checkYPlane(i)) {
                    pullYDir(i);
                    found = true;
                }
            } else if (tdir == 2 || tdir == 3) {
                if (checkXPlane(i)) {
                    pullXDir(i);
                    found = true;
                }
            } else if (tdir == 4 || tdir == 5) {
                if (checkZPlane(i)) {
                    pullZDir(i);
                    found = true;
                }
            }
            if (found) {
                p += 10;
                i--;
                updatePoints();
            }
        }
    }

    public void pullYDir(int l) {
        int i;
        for (i = l; (tdir == 0 ? i < y-1 : i > 1); i += (tdir == 0 ? 1 : -1)) {
            for (int k = 0; k < x; k++) {
                for (int j = 0; j < z; j++) {
                    field[i][k][j] = field[(tdir == 0 ? i + 1 : i - 1)][k][j];
                    data[i][k][j] = data[(tdir == 0 ? i + 1 : i - 1)][k][j];
                }
            }
        }
        for (int k = 0; k < x; k++) {
            for (int j = 0; j < z; j++) {
                field[i][k][j] = 0;
                data[i][k][j] = 0;
            }
        }
    }

    public void pullXDir(int l) {
        int i;
        for (i = l; (tdir == 2 ? i < x-1 : i > 1); i += (tdir == 2 ? 1 : -1)) {
            for (int k = 0; k < y; k++) {
                for (int j = 0; j < z; j++) {
                    field[k][i][j] = field[k][(tdir == 2 ? i + 1 : i - 1)][j];
                    data[k][i][j] = data[k][(tdir == 2 ? i + 1 : i - 1)][j];
                }
            }
        }
        for (int k = 0; k < x; k++) {
            for (int j = 0; j < z; j++) {
                field[k][i][j] = 0;
                data[k][i][j] = 0;
            }
        }
    }

    public void pullZDir(int l) {
        int i;
        for (i = l; (tdir == 4 ? i < z-1 : i > 1); i += (tdir == 4 ? 1 : -1)) {
            for (int k = 0; k < y; k++) {
                for (int j = 0; j < x; j++) {
                    field[k][j][i] = field[k][j][(tdir == 4 ? i + 1 : i - 1)];
                    data[k][j][i] = data[k][j][(tdir == 4 ? i + 1 : i - 1)];
                }
            }
        }
        for (int k = 0; k < y; k++) {
            for (int j = 0; j < x; j++) {
                field[k][j][i] = 0;
                data[k][j][i] = 0;
            }
        }
    }

    boolean checkYPlane(int level) {
        for (int j = 0; j < x; j++ ) {
            for (int k = 0; k < z; k++) {
                if (field[level][j][k] != 35) {
                    return false;
                }
            }
        }
        return true;
    }
    boolean checkXPlane(int level) {
        for (int j = 0; j < y; j++ ) {
            for (int k = 0; k < z; k++) {
                if (field[j][level][k] != 35) {
                    return false;
                }
            }
        }
        return true;
    }    

    boolean checkZPlane(int level) {
        for (int j = 0; j < y; j++ ) {
            for (int k = 0; k < x; k++) {
                if (field[j][k][level] != 35) {
                    return false;
                }
            }
        }
        return true;
    }
    public int dir1Selector(int dir, int y, int x, int z) {
        return ((dir == 0 || dir == 1) ? y : 0) + ((dir == 2 || dir == 3) ? x : 0) + ((dir == 4 || dir == 5) ? z : 0);
    }

    public int dir2Selector(int dir, int y, int x, int z) {
        return ((dir == 0 || dir == 1) ? x : 0) + ((dir == 2 || dir == 3) ? y : 0) + ((dir == 4 || dir == 5) ? y : 0);
    }

    public int dir3Selector(int dir, int y, int x, int z) {
        return ((dir == 0 || dir == 1) ? z : 0) + ((dir == 2 || dir == 3) ? z : 0) + ((dir == 4 || dir == 5) ? x : 0);
    }

    public boolean mutate(byte d) {
        int cdir = (int) (Math.random()*6);
        FallingBrick f = falling.get((int)(Math.random()*falling.size()));
        int ny = f.y+modY(cdir);
        int nx = f.x+modX(cdir);
        int nz = f.z+modZ(cdir);
        if (!outOfBounds(ny, nx, nz)) {
            //System.out.println(""+ ny +" " + nx + " "+ nz + " " + cdir);
            if (collides(ny, nx, nz)) {
                running = false;
            }
            for (FallingBrick tmp : falling) {
                if (tmp.x == nx && tmp.y == ny && tmp.z == nz) {
                    return false;
                }
            }
            falling.add(new FallingBrick(ny, nx, nz, d));
            return true;
        }
        return false;
    }

    public void save() {
        super.save();
        conf.setProperty(pre + "type", type);
        conf.setProperty(pre + "bmin", blockmin);
        conf.setProperty(pre + "bmax", blockmax);
        conf.save();
    }

    public boolean checkStop(int dir) {
        for (FallingBrick f : falling) {
            if (collides(f.y+modY(dir), f.x+modX(dir), f.z+modZ(dir))) {
                boolean found = false;
                for (FallingBrick tmp : falling) {
                    if (tmp.x == f.x+modX(dir) && tmp.y == f.y + modY(dir) && tmp.z == f.z + modZ(dir)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return true;
                }
            }
        }
        return false;
    }


    public int modX(int dir) {
        return (dir == 3 ? 1 : 0) - (dir == 2 ? 1 : 0);
    }
    public int modY(int dir) {
        return (dir == 1 ? 1 : 0) - (dir == 0 ? 1 : 0);
    }
    public int modZ(int dir) {
        return (dir == 5 ? 1 : 0) - (dir == 4 ? 1 : 0);
    }

    public int startX() {
        if (tdir == 2) {
            return x-1;
        } else if (tdir == 3) {
            return 0;
        } else {
            return (int) (0.5 * x);
        }
    }

    public int startY() {
        if (tdir == 0) {
            return y-1;
        } else if (tdir == 1) {
            return 0;
        } else {
            return (int) (0.5 * y);
        }
    }

    public int startZ() {
        if (tdir == 4) {
            return z-1;
        } else if (tdir == 5) {
            return 0;
        } else {
            return (int) (0.5 * z);
        }
    }

    public boolean collides(int cy, int cx, int cz) {
        if (outOfBounds(cy, cx, cz)) {
            return true;
        }
        return (field[cy][cx][cz] != 0);
    }

    public boolean outOfBounds(int cy, int cx, int cz) {
        return (cy < 0 || cx < 0 || cz < 0 || cx >= x || cy >= y || cz >= z);
    }
    public void pull(int dir) {
        for (FallingBrick f : falling) {
            this.field[f.y][f.x][f.z] = 0;
            this.data[f.y][f.x][f.z] = 0;
            f.pull(dir);
            this.field[f.y][f.x][f.z] = 35;
            this.data[f.y][f.x][f.z] = f.data;
        }
        updateFalling();
    }

    public void updateFalling() {
        for (FallingBrick f : falling) {
            this.field[f.y][f.x][f.z] = 35;
            this.data[f.y][f.x][f.z] = f.data;
        }

    }

    public void next(int dir) {
        if (!running) {
            stopGame(null);
            return;
        }
        if (checkStop(dir)) {
            addBlock();
        } else {
            pull(dir);
        }
        update();
    }

    public void move(int dir) {
        if (!running) {
            return;
        }
        if (!checkStop(dir)) {
            pull(dir);
        }
        update();
    }

    public LinkedList<FallingBrick> getRotated(int dir) {
        LinkedList<FallingBrick> out = new LinkedList<FallingBrick>();
        FallingBrick c = falling.get(0);
        FallingBrick tmp;
        out.push(c);
        for (int i = 0; i < falling.size(); i++) {
            tmp = falling.get(i);
            if (tmp == c) {
                continue;
            }
            out.push(getRelative(dir, tmp, c));
        }
        return out;
    }

    public FallingBrick getRelative(int dir, FallingBrick p1, FallingBrick p2) {
        int x, y, z, nx, ny, nz;
        y = p2.y-p1.y;
        x = p2.x-p1.x;
        z = p2.z-p1.z;
        // (dir == 0 ? -x : 0)
        ny = (dir == 0 ? -x : 0) + (dir == 1 ?  x : 0) +  (dir == 2 ? -z : 0) + (dir == 3 ?  z : 0) + ((dir == 4 || dir == 5) ? p1.y - p2.y : 0);
        nx = (dir == 0 ?  y : 0) + (dir == 1 ? -y : 0) + ((dir == 2 || dir == 3) ? p1.x - p2.x : 0) + (dir == 4 ? -z : 0) + (dir == 5 ?  z : 0);
        nz = ((dir == 0 || dir == 1) ? p1.z - p2.z : 0) + (dir == 2 ?  y : 0) + (dir == 3 ? -y : 0) + (dir == 4 ?  x : 0) + (dir == 5 ? -x : 0);
        FallingBrick out = new FallingBrick(p2.y+ny, p2.x+nx, p2.z+nz, p2.data);
        return out;
    }

    public boolean checkFalling(LinkedList<FallingBrick> bricks) {
        for (FallingBrick f : falling) {
            field[f.y][f.x][f.z] = 0;
        }
        for (FallingBrick brk : bricks) {
            if (collides(brk.y, brk.x, brk.z)) {
                for (FallingBrick f : falling) {
                    field[f.y][f.x][f.z] = 35;
                }
                return false;
            }
        }
        return true;
    }

    public void rotate(int dir) {
        if (falling == null || falling.size() == 0) {
            return;
        }
        LinkedList<FallingBrick> result = getRotated(dir);
        if (checkFalling(result)) {
            falling = result;
            updateFalling();
        }
        update();
    }

    public void handleSignal(Sign sign, Player pl) {
        String input[] = sign.getLines();
        String in[] = input[2].split(":");
        if (in.length != 0) {
            if (input[2].equalsIgnoreCase("start")) {
                startGame(pl);
            } else if (input[2].equalsIgnoreCase("stop")) {
                stopGame(pl);
            } else {
                if (in[0].contains("r")) {
                    String rin[] = in[0].split("r");
                    if (rin.length != 0) {
                        if (plugin.checkInt(rin[0])) {
                            rotate(Integer.parseInt(rin[0]));
                        }
                    }
                } else if (plugin.checkInt(in[0])) {
                    move(Integer.parseInt(in[0]));
                } else if (input[2].equalsIgnoreCase("points")) {
                    points = sign;
                    updatePoints();
                } else {
                    pl.sendMessage(ChatColor.RED + "No such command!");
                }
            }
        }
    }

    public void updatePoints() {
        if (points != null) {
            points.setLine(3, ""+p);
            points.update();
        }
    }

    public void startGame(Player pl) {
        p = 0;
        updatePoints();
        running = true;
        stopGame(pl);
        initEmpty();
        addBlock();
        updateFalling();
        update();
        if (x*y*z < blockmax) {
            if (pl != null) {
                pl.sendMessage("Invalid board size vs. block-size");
            }
            running = false;
            return;
        }
        gr = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GameRunner(), 40L, 40L);
    }

    public void stopGame(Player pl) {
        if (gr != -1) {
            plugin.getServer().getScheduler().cancelTask(gr);
        }
    }

    public void info(Player pl) {
        super.info(pl);
        pl.sendMessage(ChatColor.GREEN + "Type: Tetris");
        pl.sendMessage(ChatColor.GREEN + String.format("Min blocksize: %d, max blocksize: %d", 
                blockmin, blockmax));
    }

    class FallingBrick {
        byte data;
        int x, y, z;
        public FallingBrick(int y, int x, int z, byte data) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.data = data;
        }

        public void pull(int dir) {
            this.x += modX(dir);
            this.y += modY(dir);
            this.z += modZ(dir);
        }
    }
    class GameRunner extends Thread {
        public void run() {
            next(tdir);
        }
    }
}
