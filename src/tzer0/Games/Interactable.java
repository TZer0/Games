package tzer0.Games;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Interactable {
    public boolean isInBoard(Block pos);
    public void interact(Block pos, Player pl);
}
