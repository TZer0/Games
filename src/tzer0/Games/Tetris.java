package tzer0.Games;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class Tetris extends Board {
    char color1, color2;
    int blockmin, blockmax;
    char dir;
    boolean running;
    int gr;
    LinkedList<FallingBrick> falling;
    public Tetris(String name, World world, Location pos1, Location pos2,
            boolean imported, Games plugin, Configuration conf) {
        super(name, world, pos1, pos2, imported, plugin, conf);
        gr = -1;
        running = true;
        blockmin = 4;
        blockmax = 4;
        this.type = "Tetris";
        save();
        color1 = 0;
        dir = 1;
        color2 = 15;
        this.field = new int[y][x][z];
        this.data = new byte[y][x][z];
        falling = new LinkedList<FallingBrick>();
        initEmpty();
    }

    public void addBlock() {
        int target = blockmin + (int) (Math.random()*(blockmax-blockmin))-1;
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
        conf.save();
    }

    public boolean checkStop() {
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
        if (dir == 2) {
            return x-1;
        } else if (dir == 3) {
            return 0;
        } else {
            return (int) (Math.random() * x);
        }
    }

    public int startY() {
        if (dir == 0) {
            return y-1;
        } else if (dir == 1) {
            return 0;
        } else {
            return (int) (Math.random() * y);
        }
    }

    public int startZ() {
        if (dir == 4) {
            return z-1;
        } else if (dir == 5) {
            return 0;
        } else {
            return (int) (Math.random() * z);
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
    public void pull() {
        for (FallingBrick f : falling) {
            this.field[f.y][f.x][f.z] = 0;
            this.data[f.y][f.x][f.z] = 0;
            f.pull();
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

    public void next() {
        if (!running) {
            stopGame(null);
            return;
        }
        if (checkStop()) {
            addBlock();
        } else {
            pull();
        }
        update();
    }
    
    public void startGame(Player pl) {
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
        gr = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new GameRunner(), 20L, 20L);
    }
    
    public void stopGame(Player pl) {
        if (gr != -1) {
            plugin.getServer().getScheduler().cancelTask(gr);
        }
    }
    public void info(Player pl) {
        super.info(pl);
        pl.sendMessage(ChatColor.GREEN + "Type: Tetris");
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

        public void pull() {
            this.x += modX(dir);
            this.y += modY(dir);
            this.z += modZ(dir);
        }
    }
    class GameRunner extends Thread {
        public void run() {
            next();
        }
    }
}
