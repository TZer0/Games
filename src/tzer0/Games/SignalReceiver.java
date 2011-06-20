package tzer0.Games;

import org.bukkit.entity.Player;

public interface SignalReceiver {
    public void handleSignal(String signal[], Player pl);
}
