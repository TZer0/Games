/*
 * 
 */
package tzer0.Games;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;


public class Board {
    int x, z, y;
    Games plugin;
    Block startblock;

    Configuration conf;
    String pre;
    String name;
    String type;
    byte data[][][];
    int field[][][];
    
    public Board(String name, World world, Location pos1, Location pos2, boolean imported, Games plugin, Configuration conf) {
        this.conf = conf;
        this.plugin = plugin;
        pre = String.format("boards.%s.%s.", world.getName(), name);
        if (imported) {
            this.x = conf.getInt(pre+"x", 0);
            this.z = conf.getInt(pre+"z", 0);
            this.y = conf.getInt(pre+"y", -1);
            startblock = world.getBlockAt(conf.getInt(pre+"sx",-1), conf.getInt(pre+"sy",-1), conf.getInt(pre+"sz",-1));
        } else {
            move(pos1, pos2);
        }
        this.name = name;
        this.data = null;
        this.field = null;
    }

    public void save() {
        conf.setProperty(pre+"x", x);
        conf.setProperty(pre+"z", z);
        conf.setProperty(pre+"y", y);
        conf.setProperty(pre+"sx", startblock.getX());
        conf.setProperty(pre+"sy", startblock.getY());
        conf.setProperty(pre+"sz", startblock.getZ());
        conf.save();
    }

    public void move(Location pos1, Location pos2) {
        int xmin, zmin, xmax, zmax, ymin, ymax;
        xmin = Math.min(pos1.getBlockX(), pos2.getBlockX());
        xmax = Math.max(pos1.getBlockX(), pos2.getBlockX()) + 1;
        zmin = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        zmax = Math.max(pos1.getBlockZ(), pos2.getBlockZ()) + 1;
        ymin = Math.min(pos1.getBlockY(), pos2.getBlockY())-1;
        ymax = Math.max(pos1.getBlockY(), pos2.getBlockY())+1;
        x = Math.abs(xmax-xmin);
        z = Math.abs(zmax-zmin);
        y = Math.abs(ymax-ymin);
        startblock = pos1.getWorld().getBlockAt(xmin, ymin, zmin);
    }

    public void shortInfo(Player pl) {
        pl.sendMessage(ChatColor.YELLOW + String.format("%s - %s - %s", name, type, startblock.getWorld().getName()));
    }
    
    public void info(Player pl) {
        pl.sendMessage(ChatColor.GREEN + String.format("Name: %s", name));
        pl.sendMessage(ChatColor.GREEN + String.format("Location: %d-%d,%d-%d,%d-%d", startblock.getX(), 
                startblock.getX()+x, startblock.getY(), startblock.getY()+y, startblock.getZ(), startblock.getZ()+z));
    }
    
    public void initEmpty() {
        for (int k = 0; k < y; k++) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < z; j++) {
                    if (field != null) {
                        field[k][i][j] = 0;
                    }
                    if (data != null) {
                        field[k][i][j] = 0;
                    }
                }
            }
        }
    }
    
    
    public void update() {
        for (int k = 0; k < y; k++) {
            for (int i = 0; i < x; i++) {
                for (int j = 0; j < z; j++) {
                    startblock.getRelative(i, k, j).setTypeId(field[k][i][j]);
                    if (data != null) {
                        startblock.getRelative(i, k, j).setData((byte) data[k][i][j]);
                    }
                }
            }
        }
    }
}
