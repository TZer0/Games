package tzer0.Games;

import java.util.LinkedList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

public class Tetris extends Board implements SignalReceiver {
    char color1, color2;
    int blockmin, blockmax;
    char tdir;
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
        tdir = 0;
        color2 = 15;
        this.field = new int[y][x][z];
        this.data = new byte[y][x][z];
        falling = new LinkedList<FallingBrick>();
        initEmpty();
    }

    public void addBlock() {
        int target = blockmin + (int) (Math.random()*(blockmax-blockmin))-1;
        falling.clear();
        checkFilled();
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

    public void checkFilled() {
        
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

    public void handleSignal(String input[], Player pl) {
        String in[] = input[2].split("-");
        if (in.length != 0) {
            if (in[0].contains("r")) {
                String rin[] = in[0].split("r");
                if (rin.length != 0) {
                    if (plugin.checkInt(rin[0])) {
                        rotate(Integer.parseInt(rin[0]));
                    }
                }
            } else if (plugin.checkInt(in[0])) {
                move(Integer.parseInt(in[0]));
            } else {
                
            }
        }
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
