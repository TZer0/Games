package tzer0.Games;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.config.Configuration;

public class Tetris extends Board {

    public Tetris(String name, World world, Location pos1, Location pos2,
            boolean imported, Games plugin, Configuration conf) {
        super(name, world, pos1, pos2, imported, plugin, conf);
        this.type = "Tetris";
        save();
    }

    
    
    public void save() {
        super.save();
        conf.setProperty(pre + "type", type);
        conf.save();
    }
}
