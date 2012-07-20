package tzer0.Games;

import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

public interface SignalReceiver {
    public void handleSignal(Sign sign, Player pl);
}
