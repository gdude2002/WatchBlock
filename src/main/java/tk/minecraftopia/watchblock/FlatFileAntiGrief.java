package tk.minecraftopia.watchblock;

import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import java.io.*;
import org.bukkit.*;
import org.bukkit.event.block.*;
import org.bukkit.configuration.file.*;
import java.util.*;

public class FlatFileAntiGrief implements Listener
{
    private WatchBlock plugin;
    static File configfile;
    YamlConfiguration config;
    static LinkedHashMap<Block, String> pistoncache;
    static HashMap<String, LinkedList<String>> queue;
    static boolean locked;
    
    static {
        FlatFileAntiGrief.configfile = new File("plugins" + File.separator + "WatchBlock" + File.separator + "config.yml");
        FlatFileAntiGrief.pistoncache = new LinkedHashMap<Block, String>();
        FlatFileAntiGrief.queue = new HashMap<String, LinkedList<String>>();
        FlatFileAntiGrief.locked = false;
    }
    
    public FlatFileAntiGrief(final WatchBlock callbackPlugin) {
        System.out.println("[WatchBlock] FlatFile AntiGrief started and running");
        this.plugin = callbackPlugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonRetract(final BlockPistonRetractEvent event) {
        if (!WatchBlock.worlds.contains(event.getBlock().getWorld().getName())) {
            return;
        }
        if (!WatchBlock.pistons) {
            event.setCancelled(false);
            return;
        }
        if (event.isSticky()) {
            boolean show = true;
            final Block block = event.getBlock();
            String cached = FlatFileAntiGrief.pistoncache.get(block);
            String pistonowner;
            if (cached == null) {
                pistonowner = this.ownedBy(block, new File("plugins" + File.separator + "WatchBlock" + File.separator + block.getWorld().getName()));
                FlatFileAntiGrief.pistoncache.put(block, pistonowner);
            }
            else {
                pistonowner = cached;
            }
            final Player player = this.plugin.getServer().getPlayer(pistonowner);
            int x = event.getDirection().getModX();
            int y = event.getDirection().getModY();
            int z = event.getDirection().getModZ();
            if (x == 1) {
                ++x;
            }
            else if (x == -1) {
                --x;
            }
            if (y == 1) {
                ++y;
            }
            else if (y == -1) {
                --y;
            }
            if (z == 1) {
                ++z;
            }
            else if (z == -1) {
                --z;
            }
            final Block above = block.getRelative(x, y, z);
            cached = null;
            cached = FlatFileAntiGrief.pistoncache.get(above);
            String blockowner;
            if (cached == null) {
                blockowner = this.ownedBy(above, new File("plugins" + File.separator + "WatchBlock" + File.separator + block.getWorld().getName()));
                FlatFileAntiGrief.pistoncache.put(above, pistonowner);
            }
            else {
                blockowner = cached;
            }
            if (player != null && !player.isOp() && !this.plugin.getVault().has(player, "watchblock.admin") && !player.getName().equalsIgnoreCase(blockowner) && blockowner != null && blockowner != "") {
                try {
                    this.config = YamlConfiguration.loadConfiguration(WatchBlock.allow);
                    final Set<String> temp = (Set<String>)this.config.getConfigurationSection("allow." + blockowner).getKeys(true);
                    for (final String r : temp) {
                        if (r.equalsIgnoreCase(player.getName())) {
                            event.setCancelled(false);
                            return;
                        }
                    }
                }
                catch (NullPointerException ex) {}
                if (show) {
                    String chatmsg = WatchBlock.pistonmsg;
                    try {
                        chatmsg = WatchBlock.pistonmsg.replace("$player", blockowner);
                    }
                    catch (NullPointerException ex2) {}
                    if (chatmsg != "") {
                        player.sendMessage(ChatColor.RED + chatmsg);
                    }
                    show = false;
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPistonExtend(final BlockPistonExtendEvent event) {
        if (!WatchBlock.worlds.contains(event.getBlock().getWorld().getName())) {
            return;
        }
        if (!WatchBlock.pistons) {
            event.setCancelled(false);
            return;
        }
        boolean show = true;
        final Block block = event.getBlock();
        String cached = FlatFileAntiGrief.pistoncache.get(block);
        String pistonowner;
        if (cached == null) {
            pistonowner = this.ownedBy(block, new File("plugins" + File.separator + "WatchBlock" + File.separator + block.getWorld().getName()));
            FlatFileAntiGrief.pistoncache.put(block, pistonowner);
        }
        else {
            pistonowner = cached;
        }
        final Player player = this.plugin.getServer().getPlayer(pistonowner);
        final File file = new File("plugins" + File.separator + "WatchBlock" + File.separator + block.getWorld().getName());
        for (final Block above : event.getBlocks()) {
            cached = null;
            cached = FlatFileAntiGrief.pistoncache.get(above);
            String blockowner;
            if (cached == null) {
                blockowner = this.ownedBy(above, file);
                FlatFileAntiGrief.pistoncache.put(above, blockowner);
            }
            else {
                blockowner = cached;
            }
            if (player != null && !player.isOp() && !this.plugin.getVault().has(player, "watchblock.admin") && !player.getName().equalsIgnoreCase(blockowner) && blockowner != null && blockowner != "") {
                try {
                    this.config = YamlConfiguration.loadConfiguration(WatchBlock.allow);
                    final Set<String> temp = (Set<String>)this.config.getConfigurationSection("allow." + blockowner).getKeys(true);
                    for (final String r : temp) {
                        if (r.equalsIgnoreCase(player.getName())) {
                            event.setCancelled(false);
                            return;
                        }
                    }
                }
                catch (NullPointerException ex) {}
                if (show) {
                    String chatmsg = WatchBlock.pistonmsg;
                    try {
                        chatmsg = WatchBlock.pistonmsg.replace("$player", blockowner);
                    }
                    catch (NullPointerException ex2) {}
                    if (chatmsg != "") {
                        player.sendMessage(ChatColor.RED + chatmsg);
                    }
                    show = false;
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWaterBukketPlace(final PlayerBucketEmptyEvent event) {
        if (!WatchBlock.worlds.contains(event.getBlockClicked().getWorld().getName())) {
            return;
        }
        if (!WatchBlock.watergrief) {
            event.setCancelled(false);
            return;
        }
        final Player player = event.getPlayer();
        final Block block = event.getBlockClicked();
        if (!player.isOp() && !this.plugin.getVault().has(player, "watchblock.admin")) {
            final String test = this.ownedBy(block, new File("plugins" + File.separator + "WatchBlock" + File.separator + player.getWorld().getName()));
            if (!player.getName().equalsIgnoreCase(test) && test != null && test != "") {
                try {
                    this.config = YamlConfiguration.loadConfiguration(WatchBlock.allow);
                    final Set<String> temp = (Set<String>)this.config.getConfigurationSection("allow." + test).getKeys(true);
                    for (final String r : temp) {
                        if (r.equalsIgnoreCase(player.getName())) {
                            event.setCancelled(false);
                            return;
                        }
                    }
                }
                catch (NullPointerException ex) {}
                String chatmsg = WatchBlock.bucketmsg;
                try {
                    chatmsg = WatchBlock.bucketmsg.replace("$player", test);
                }
                catch (NullPointerException ex2) {}
                if (chatmsg != "") {
                    event.getPlayer().sendMessage(ChatColor.RED + chatmsg);
                }
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public synchronized void onBlockBreak(final BlockBreakEvent event) {
        if (!WatchBlock.worlds.contains(event.getBlock().getWorld().getName())) {
            return;
        }
        final Player player = event.getPlayer();
        if (!this.plugin.getVault().has(player, "watchblock.admin") && !player.isOp()) {
            final Block block = event.getBlock();
            final World world = block.getWorld();
            final File worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + world.getName());
            final Chunk chunk = block.getChunk();
            final String chunkname = String.valueOf(chunk.getX()) + "." + chunk.getZ() + ".yml";
            final String blocklocation = String.valueOf(block.getLocation().getBlockX()) + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
            final String protectionfix = String.valueOf(block.getLocation().getBlockX()) + "," + (block.getLocation().getBlockY() + 1) + "," + block.getLocation().getBlockZ();
            final YamlConfiguration flats = this.plugin.getConfig(new File(worldpath + File.separator + chunkname));
            final YamlConfiguration allowed = this.plugin.getConfig(WatchBlock.allow);
            if (flats.getString(String.valueOf(blocklocation) + ".player", "").equals(event.getPlayer().getName())) {
                flats.set(blocklocation, (Object)null);
                try {
                    FlatFileAntiGrief.pistoncache.remove(block);
                    flats.save(new File(worldpath + File.separator + chunkname));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (!allowed.getBoolean("allow." + flats.getString(String.valueOf(blocklocation) + ".player", "") + "." + player.getName().toLowerCase(), false) && flats.getString(String.valueOf(blocklocation) + ".player", "").length() > 0) {
                if (!flats.getString(String.valueOf(blocklocation) + ".player", "").equals(player.getName()) && flats.getString(String.valueOf(blocklocation) + ".player", "").length() != 0) {
                    final String whoplaced = this.ownedBy(event.getBlock().getLocation().getBlock(), new File("plugins" + File.separator + "WatchBlock" + File.separator + player.getWorld().getName()));
                    if (whoplaced.length() != 0) {
                        String chatmsg = WatchBlock.ownedmsg;
                        try {
                            chatmsg = WatchBlock.ownedmsg.replace("$player", whoplaced);
                        }
                        catch (NullPointerException ex) {}
                        if (chatmsg != "") {
                            event.getPlayer().sendMessage(ChatColor.RED + chatmsg);
                        }
                    }
                    event.setCancelled(true);
                    return;
                }
                if (block.getRelative(0, 1, 0).getTypeId() == 71 || block.getRelative(0, 1, 0).getTypeId() == 64) {
                    if (!flats.getString(String.valueOf(protectionfix) + ".player", "").equals(player.getName()) && flats.getString(String.valueOf(protectionfix) + ".player", "").length() != 0) {
                        final String whoplaced = this.ownedBy(event.getBlock().getLocation().getBlock(), new File("plugins" + File.separator + "WatchBlock" + File.separator + player.getWorld().getName()));
                        if (whoplaced.length() != 0) {
                            String chatmsg = WatchBlock.ownedmsg;
                            try {
                                chatmsg = WatchBlock.ownedmsg.replace("$player", whoplaced);
                            }
                            catch (NullPointerException ex2) {}
                            if (chatmsg != "") {
                                event.getPlayer().sendMessage(ChatColor.RED + chatmsg);
                            }
                        }
                        event.setCancelled(true);
                    }
                    else {
                        FlatFileAntiGrief.pistoncache.remove(block);
                        flats.set(blocklocation, (Object)null);
                        try {
                            flats.save(new File(worldpath + File.separator + chunkname));
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        else {
            final Block block = event.getBlock();
            final World world = block.getWorld();
            final File worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + world.getName());
            final Chunk chunk = block.getChunk();
            final String chunkname = String.valueOf(chunk.getX()) + "." + chunk.getZ() + ".yml";
            final String blocklocation = String.valueOf(block.getLocation().getBlockX()) + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
            final YamlConfiguration flats2 = this.plugin.getConfig(new File(worldpath + File.separator + chunkname));
            final String whoplaced2 = this.ownedBy(event.getBlock().getLocation().getBlock(), new File("plugins" + File.separator + "WatchBlock" + File.separator + player.getWorld().getName()));
            if ((this.plugin.getVault().has(player, "watchblock.admin") || player.isOp()) && player.getItemInHand().getType().compareTo(Material.getMaterial(WatchBlock.adminstick)) == 0) {
                if (whoplaced2 != null && whoplaced2 != "") {
                    String chatmsg2 = WatchBlock.adminmsg;
                    try {
                        chatmsg2 = WatchBlock.adminmsg.replace("$player", whoplaced2);
                    }
                    catch (NullPointerException ex3) {}
                    if (chatmsg2 != "") {
                        event.getPlayer().sendMessage(ChatColor.RED + chatmsg2);
                    }
                }
                else {
                    String chatmsg2 = WatchBlock.adminmsg;
                    try {
                        chatmsg2 = WatchBlock.adminmsg.replace("$player", whoplaced2);
                    }
                    catch (NullPointerException ex4) {}
                    if (chatmsg2 != "") {
                        event.getPlayer().sendMessage(ChatColor.RED + chatmsg2);
                    }
                }
                event.setCancelled(true);
                return;
            }
            FlatFileAntiGrief.pistoncache.remove(block);
            flats2.set(blocklocation, (Object)null);
            try {
                flats2.save(new File(worldpath + File.separator + chunkname));
            }
            catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (event.getBlock().getType() == Material.SAPLING) {
            return;
        }
        if (!WatchBlock.worlds.contains(event.getBlock().getWorld().getName())) {
            return;
        }
        final Player player = event.getPlayer();
        final YamlConfiguration allowed = this.plugin.getConfig(WatchBlock.allow);
        final Block block = event.getBlock();
        final World world = block.getWorld();
        final File worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + world.getName());
        final Chunk chunk = block.getChunk();
        final String chunkname = String.valueOf(chunk.getX()) + "." + chunk.getZ() + ".yml";
        final String blocklocation = String.valueOf(block.getLocation().getBlockX()) + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
        if (!worldpath.exists()) {
            worldpath.mkdir();
        }
        final File customConfigFile = new File(worldpath + File.separator + chunkname);
        final FileConfiguration flats = (FileConfiguration)YamlConfiguration.loadConfiguration(customConfigFile);
        if (WatchBlock.protectaround && !player.isOp() && !this.plugin.getVault().has(player, "watchblock.admin")) {
            final Block block2 = event.getBlockAgainst();
            final Block block3 = event.getBlock().getRelative(0, -1, 0);
            final LinkedList<Block> blocksaround = new LinkedList<Block>();
            blocksaround.add(block2);
            blocksaround.add(block3);
            for (final Block above : blocksaround) {
                String test = null;
                String owner = null;
                if (event.getBlock().getType().compareTo(Material.AIR) != 0) {
                    test = this.ownedBy(above, new File("plugins" + File.separator + "WatchBlock" + File.separator + player.getWorld().getName()));
                    owner = String.valueOf(above.getLocation().getBlockX()) + "," + above.getLocation().getBlockY() + "," + above.getLocation().getBlockZ();
                }
                if (!player.getName().equalsIgnoreCase(test) && test != null && test != "") {
                    if (allowed.getBoolean("allow." + flats.getString(String.valueOf(owner) + ".player", "") + "." + player.getName().toLowerCase(), false) || flats.getString(String.valueOf(owner) + ".player", "").length() <= 0) {
                        continue;
                    }
                    String chatmsg = WatchBlock.abovemsg;
                    try {
                        chatmsg = WatchBlock.abovemsg.replace("$player", test);
                    }
                    catch (NullPointerException ex) {}
                    if (chatmsg != "") {
                        event.getPlayer().sendMessage(ChatColor.RED + chatmsg);
                    }
                    event.setCancelled(true);
                }
                else {
                    flats.set(String.valueOf(blocklocation) + ".player", (Object)player.getName());
                    if (block.getTypeId() == 71 || block.getTypeId() == 64) {
                        final String protectionfix = String.valueOf(block.getLocation().getBlockX()) + "," + (block.getLocation().getBlockY() + 1) + "," + block.getLocation().getBlockZ();
                        flats.set(String.valueOf(protectionfix) + ".player", (Object)player.getName());
                    }
                    try {
                        if (!this.plugin.getVault().has(player, "watchblock.protect") || WatchBlock.toggle.containsKey(event.getPlayer().getName()) || WatchBlock.excludeblocks.contains(String.valueOf(event.getBlock().getTypeId()))) {
                            continue;
                        }
                        flats.save(customConfigFile);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            flats.set(String.valueOf(blocklocation) + ".player", (Object)player.getName());
            if (block.getTypeId() == 71 || block.getTypeId() == 64) {
                final String protectionfix2 = String.valueOf(block.getLocation().getBlockX()) + "," + (block.getLocation().getBlockY() + 1) + "," + block.getLocation().getBlockZ();
                flats.set(String.valueOf(protectionfix2) + ".player", (Object)player.getName());
            }
            try {
                if (this.plugin.getVault().has(player, "watchblock.protect") && !WatchBlock.toggle.containsKey(event.getPlayer().getName()) && !WatchBlock.excludeblocks.contains(String.valueOf(event.getBlock().getTypeId()))) {
                    flats.save(customConfigFile);
                }
            }
            catch (IOException e2) {
                e2.printStackTrace();
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
    
    public LinkedList<String> PistonownedBy(final List<Block> blocks, final File pluginpath) {
        final LinkedList<String> people = new LinkedList<String>();
        for (final Block block : blocks) {
            final int x = block.getChunk().getX();
            final int z = block.getChunk().getZ();
            FileConfiguration pC = null;
            String blocklocation = null;
            final String chunkname = String.valueOf(x) + "." + z + ".yml";
            blocklocation = String.valueOf(block.getLocation().getBlockX()) + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ();
            final File customConfigFile = new File(pluginpath + File.separator + chunkname);
            pC = (FileConfiguration)YamlConfiguration.loadConfiguration(customConfigFile);
            try {
                final String string = pC.getString(String.valueOf(blocklocation) + ".player", "");
                people.add(string);
            }
            catch (NullPointerException ex) {}
        }
        return people;
    }
}
