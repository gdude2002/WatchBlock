package tk.minecraftopia.watchblock;

import org.bukkit.plugin.*;
import org.bukkit.event.player.*;
import org.bukkit.event.*;

public class PlayerLoginListener implements Listener
{
    Plugin plugin;
    
    public PlayerLoginListener(final WatchBlock callbackPlugin) {
        System.out.println("[WatchBlock] PlayerLoginListener started and running");
        this.plugin = (Plugin)callbackPlugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLogin(final PlayerLoginEvent event) {
        WatchBlock.toggle.remove(event.getPlayer().getName());
    }
}
