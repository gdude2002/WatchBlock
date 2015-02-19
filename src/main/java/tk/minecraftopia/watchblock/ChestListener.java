package tk.minecraftopia.watchblock;

import org.bukkit.event.player.*;
import java.io.*;
import java.util.*;
import org.bukkit.event.*;
import org.bukkit.block.*;
import org.bukkit.*;
import org.bukkit.configuration.file.*;

public class ChestListener implements Listener
{
    YamlConfiguration config;
    WatchBlock plugin;
    
    public ChestListener(final WatchBlock watchblock) {
        this.plugin = watchblock;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onChestUse(final PlayerInteractEvent event) {
        final String player = event.getPlayer().getName();
        if (WatchBlock.flatfile) {
            if (event.getClickedBlock() != null && event.getClickedBlock().getType().compareTo(Material.CHEST) == 0) {
                final String blockowner = this.ownedBy(event.getClickedBlock(), new File("plugins" + File.separator + "WatchBlock" + File.separator + event.getClickedBlock().getWorld().getName()));
                if (!event.getPlayer().isOp() && !this.plugin.getVault().has(event.getPlayer(), "watchblock.admin") && player != null && !player.equalsIgnoreCase(blockowner) && blockowner != null && blockowner != "") {
                    try {
                        this.config = YamlConfiguration.loadConfiguration(WatchBlock.allow);
                        final Set<String> temp = (Set<String>)this.config.getConfigurationSection("allow." + blockowner).getKeys(true);
                        for (final String r : temp) {
                            if (r.equalsIgnoreCase(player)) {
                                event.setCancelled(false);
                                return;
                            }
                        }
                    }
                    catch (NullPointerException ex) {}
                    event.getPlayer().sendMessage(ChatColor.RED + "This Chest is Protected by : " + blockowner + "!");
                    event.setCancelled(true);
                }
            }
        }
        else if (event.getClickedBlock() != null && event.getClickedBlock().getType().compareTo(Material.CHEST) == 0) {
            final String ownedby = WatchBlock.queue.getBlock(event.getClickedBlock().getWorld().getName(), "", event.getClickedBlock().getX(), event.getClickedBlock().getY(), event.getClickedBlock().getZ(), event.getClickedBlock().getChunk().getX(), event.getClickedBlock().getChunk().getZ());
            String blockowner2 = null;
            if (ownedby.length() > 2) {
                blockowner2 = ownedby.split(",")[1];
            }
            if (!event.getPlayer().isOp() && !this.plugin.getVault().has(event.getPlayer(), "watchblock.admin") && player != null && !player.equalsIgnoreCase(blockowner2) && blockowner2 != null && blockowner2 != "") {
                try {
                    this.config = YamlConfiguration.loadConfiguration(WatchBlock.allow);
                    final Set<String> temp2 = (Set<String>)this.config.getConfigurationSection("allow." + blockowner2).getKeys(true);
                    for (final String r2 : temp2) {
                        if (r2.equalsIgnoreCase(player)) {
                            event.setCancelled(false);
                            return;
                        }
                    }
                }
                catch (NullPointerException ex2) {}
                event.getPlayer().sendMessage(ChatColor.RED + "This Chest is Protected by : " + blockowner2 + "!");
                event.setCancelled(true);
            }
        }
    }
    
    public String ownedBy(final Block block, final File pluginpath) {
        final Chunk chunk = block.getChunk();
        final String chunkname = String.valueOf(chunk.getX()) + "." + chunk.getZ() + ".yml";
        final String blocklocation = String.valueOf(block.getLocation().getBlockX()) + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
        final File customConfigFile = new File(pluginpath + File.separator + chunkname);
        final FileConfiguration pC = (FileConfiguration)YamlConfiguration.loadConfiguration(customConfigFile);
        final String string = pC.getString(String.valueOf(blocklocation) + ".player", "");
        return string;
    }
}
