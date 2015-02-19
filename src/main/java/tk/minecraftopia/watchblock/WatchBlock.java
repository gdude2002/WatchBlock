package tk.minecraftopia.watchblock;

import org.bukkit.plugin.java.*;
import tk.minecraftopia.util.*;
import com.sk89q.worldedit.bukkit.*;
import net.milkbowl.vault.permission.*;
import java.util.logging.*;
import java.util.*;
import org.bukkit.event.*;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import com.sk89q.worldedit.bukkit.selections.*;
import org.bukkit.*;
import org.bukkit.configuration.file.*;
import java.sql.*;
import java.io.*;

public class WatchBlock extends JavaPlugin
{
    boolean connected;
    public static SQLQueue queue;
    MySQLConnectionPool pool;
    static boolean flatfile;
    static boolean watergrief;
    static boolean pistons;
    static boolean chestprotection;
    AntiGrief playerListener;
    static File configfile;
    static File mysql;
    static File allow;
    static File excludedBlocks;
    static LinkedList<String> worlds;
    static HashMap<String, Boolean> toggle;
    static Set<String> excludeblocks;
    static HashMap<BPBlockLocation, String> database;
    static boolean protectaround;
    static String bucketmsg;
    static String ownedmsg;
    static String pistonmsg;
    static String abovemsg;
    static String adminmsg;
    static int adminstick;
    private static WorldEditPlugin worldEdit;
    private static Permission vault;
    
    static {
        WatchBlock.queue = null;
        WatchBlock.flatfile = true;
        WatchBlock.watergrief = false;
        WatchBlock.pistons = false;
        WatchBlock.chestprotection = false;
        WatchBlock.configfile = new File("plugins" + File.separator + "WatchBlock" + File.separator + "config.yml");
        WatchBlock.mysql = new File("plugins" + File.separator + "WatchBlock" + File.separator + "mysql.yml");
        WatchBlock.allow = new File("plugins" + File.separator + "WatchBlock" + File.separator + "allow.yml");
        WatchBlock.excludedBlocks = new File("plugins" + File.separator + "WatchBlock" + File.separator + "exclude.yml");
        WatchBlock.worlds = new LinkedList<String>();
        WatchBlock.toggle = new HashMap<String, Boolean>();
        WatchBlock.database = new HashMap<BPBlockLocation, String>();
        WatchBlock.protectaround = false;
        WatchBlock.bucketmsg = "You can't empty your bucket on $player Blocks!";
        WatchBlock.ownedmsg = "This Block is owned by $player!";
        WatchBlock.pistonmsg = "You can't use Pistons on $player Blocks!";
        WatchBlock.abovemsg = "You can't build above $player Blocks!";
        WatchBlock.adminmsg = "This Block is owned by $player !";
    }
    
    public WatchBlock() {
        this.playerListener = new AntiGrief(this);
    }
    
    public void onDisable() {
        final PluginDescriptionFile pdfFile = this.getDescription();
        System.out.println("[" + pdfFile.getName() + "]" + " version " + pdfFile.getVersion() + " is disabled!");
        if (!WatchBlock.flatfile) {
            try {
                this.pool.close();
            }
            catch (NullPointerException ex) {}
        }
    }
    
    public SQLQueue getSQLQueueInstance() {
        return WatchBlock.queue;
    }
    
    protected YamlConfiguration getConfig(final File filepath) {
        if (!filepath.exists()) {
            try {
                filepath.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        final YamlConfiguration c = YamlConfiguration.loadConfiguration(filepath);
        return c;
    }
    
    public Connection getConnection() {
        try {
            final Connection conn = this.pool.getConnection();
            if (!this.connected) {
                this.getLogger().info("MySQL connection established");
                this.connected = true;
            }
            return conn;
        }
        catch (Exception ex) {
            if (this.connected) {
                this.getLogger().log(Level.SEVERE, "Error while fetching connection: ", ex);
                this.connected = false;
            }
            else {
                this.getLogger().severe("MySQL connection lost");
                ex.printStackTrace();
            }
            return null;
        }
    }
    
    public void loadConf() {
        final YamlConfiguration c = this.getConfig(WatchBlock.configfile);
        final Map<String, Object> worldmap = c.getConfigurationSection("worlds").getValues(true);
        for (final String key : worldmap.keySet()) {
            Object value = worldmap.get(key);
            if ((Boolean) value) {
                WatchBlock.worlds.add(key);
                System.out.println("[WatchBlock] Activated worlds : " + key);
            }
        }
        try {
            WatchBlock.protectaround = c.getConfigurationSection("general").getBoolean("ProtectAreaAroundPlacedBlocks");
        }
        catch (NullPointerException e3) {
            c.set("general.ProtectAreaAroundPlacedBlocks", (Object)false);
        }
        try {
            WatchBlock.flatfile = c.getConfigurationSection("flatfiles").getBoolean("FlatFiles");
        }
        catch (NullPointerException e3) {
            c.set("flatfiles.FlatFiles", (Object)false);
        }
        try {
            WatchBlock.pistons = c.getConfigurationSection("pistons").getBoolean("ProtectPistonMovement");
        }
        catch (NullPointerException e3) {
            c.set("pistons.ProtectPistonMovement", (Object)false);
        }
        try {
            WatchBlock.watergrief = c.getConfigurationSection("bucket").getBoolean("BucketGrief");
        }
        catch (NullPointerException e3) {
            c.set("bucket.BucketGrief", (Object)false);
        }
        try {
            WatchBlock.chestprotection = c.getConfigurationSection("chests").getBoolean("ChestProtection");
        }
        catch (NullPointerException e3) {
            c.set("chests.ChestProtection", (Object)false);
        }
        try {
            WatchBlock.adminstick = c.getConfigurationSection("adminstick").getInt("AdminStickID");
        }
        catch (NullPointerException e3) {
            c.set("adminstick.AdminStickID", (Object)278);
        }
        try {
            WatchBlock.abovemsg = c.getConfigurationSection("locale").getString("abovemsg");
            WatchBlock.ownedmsg = c.getConfigurationSection("locale").getString("ownedmsg");
            WatchBlock.bucketmsg = c.getConfigurationSection("locale").getString("bucketmsg");
            WatchBlock.pistonmsg = c.getConfigurationSection("locale").getString("pistonmsg");
            WatchBlock.adminmsg = c.getConfigurationSection("locale").getString("admintoolmsg");
        }
        catch (NullPointerException e3) {
            c.set("locale.admintoolmsg", (Object)"This Block is owned by $player !");
            c.set("locale.ownedmsg", (Object)"This Block is owned by $player!");
            c.set("locale.abovemsg", (Object)"You can't build above $player Blocks!");
            c.set("locale.bucketmsg", (Object)"You can't empty your bucket on $player Blocks!");
            c.set("locale.pistonmsg", (Object)"You can't use Pistons on $player Blocks!");
        }
        try {
            c.save(WatchBlock.configfile);
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
    }
    
    public void loadMySQLConf(final File mysql2) {
    }
    
    public void onEnable() {
        if (!WatchBlock.configfile.exists()) {
            final YamlConfiguration c = this.getConfig(WatchBlock.configfile);
            for (int i = 0; i < this.getServer().getWorlds().size(); ++i) {
                c.set("worlds." + this.getServer().getWorlds().get(i).getName(), (Object)c.getBoolean("worlds." + this.getServer().getWorlds().get(i).getName(), true));
                WatchBlock.worlds.add(this.getServer().getWorlds().get(i).getName());
                c.set("general.ProtectAreaAroundPlacedBlocks", (Object)false);
                c.set("flatfiles.FlatFiles", (Object)true);
                c.set("pistons.ProtectPistonMovement", (Object)false);
                c.set("bucket.BucketGrief", (Object)false);
                c.set("chests.ChestProtection", (Object)false);
                c.set("locale.ownedmsg", (Object)"This Block is owned by $player!");
                c.set("locale.abovemsg", (Object)"You can't build above $player Blocks!");
                c.set("locale.bucketmsg", (Object)"You can't empty your bucket on $player Blocks!");
                c.set("locale.pistonmsg", (Object)"You can't use Pistons on $player Blocks!");
                c.set("locale.admintoolmsg", (Object)"This Block is owned by $player !");
                c.set("adminstick.AdminStickID", (Object)278);
            }
            try {
                c.save(WatchBlock.configfile);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            this.loadConf();
        }
        String database = "";
        int port = 3306;
        String username = "";
        String password = "";
        String hostname = "";
        int poolsize = 10;
        if (!WatchBlock.mysql.exists()) {
            final YamlConfiguration mysqlconfig = new YamlConfiguration();
            final Map<String, String> map = new HashMap<String, String>();
            map.put("database", "WatchBlock");
            map.put("hostname", "localhost");
            map.put("port", "3306");
            map.put("username", "WatchBlock");
            map.put("password", "yourpassword");
            map.put("poolsize", "10");
            for (final Map.Entry e2 : map.entrySet()) {
                mysqlconfig.set("MySQL." + e2.getKey(), e2.getValue());
            }
            try {
                mysqlconfig.save(WatchBlock.mysql);
            }
            catch (IOException e3) {
                e3.printStackTrace();
            }
        }
        else {
            final YamlConfiguration mysqlconfig = YamlConfiguration.loadConfiguration(WatchBlock.mysql);
            try {
                database = mysqlconfig.getConfigurationSection("MySQL").getString("database");
                hostname = mysqlconfig.getConfigurationSection("MySQL").getString("hostname");
                port = Integer.valueOf(mysqlconfig.getConfigurationSection("MySQL").getString("port"));
                username = mysqlconfig.getConfigurationSection("MySQL").getString("username");
                password = mysqlconfig.getConfigurationSection("MySQL").getString("password");
                poolsize = Integer.valueOf(mysqlconfig.getConfigurationSection("MySQL").getString("poolsize"));
            }
            catch (NullPointerException e7) {
                System.out.println("[WatchBlock] Section not found in mysql.yml");
            }
        }
        if (!WatchBlock.excludedBlocks.exists()) {
            final YamlConfiguration excludeConfig = new YamlConfiguration();
            excludeConfig.set("Exclude.00", (Object)"");
            try {
                excludeConfig.save(WatchBlock.excludedBlocks);
            }
            catch (IOException e4) {
                e4.printStackTrace();
            }
        }
        else {
            final YamlConfiguration excludeConfig = YamlConfiguration.loadConfiguration(WatchBlock.excludedBlocks);
            try {
                WatchBlock.excludeblocks = (Set<String>)excludeConfig.getConfigurationSection("Exclude.").getKeys(true);
            }
            catch (NullPointerException e7) {
                System.out.println("[WatchBlock] Section not found in Exclude.yml");
            }
        }
        final PluginDescriptionFile pdfFile = this.getDescription();
        if (!WatchBlock.flatfile) {
            try {
                this.pool = new MySQLConnectionPool("jdbc:mysql://" + hostname + ":" + port + "/" + database, username, password, poolsize);
                WatchBlock.queue = new SQLQueue(this);
                for (final String world : WatchBlock.worlds) {
                    WatchBlock.queue.createWorldTables(world);
                }
            }
            catch (ClassNotFoundException e5) {
                e5.printStackTrace();
            }
        }
        if (!WatchBlock.flatfile) {
            this.getServer().getPluginManager().registerEvents((Listener)new AntiGrief(this), (Plugin)this);
        }
        else {
            this.getServer().getPluginManager().registerEvents((Listener)new FlatFileAntiGrief(this), (Plugin)this);
        }
        final WorldEditPlugin wePlugin = (WorldEditPlugin)this.getServer().getPluginManager().getPlugin("WorldEdit");
        if (wePlugin != null) {
            WatchBlock.worldEdit = wePlugin;
            System.out.println("[WatchBlock] WorldEdit " + this.getWorldEdit().getDescription().getVersion() + " has been found, Ownership Transfer enabled");
        }
        final RegisteredServiceProvider<Permission> permissionProvider = (RegisteredServiceProvider<Permission>)this.getServer().getServicesManager().getRegistration((Class)Permission.class);
        if (permissionProvider != null) {
            WatchBlock.vault = (Permission)permissionProvider.getProvider();
            System.out.println("[WatchBlock] Vault has been found, Loading Permissions successful");
        }
        else {
            System.out.println("[WatchBlock] Vault has not been found, WatchBlock will be disabled!! Please install Vault plugin");
            this.onDisable();
        }
        this.getServer().getPluginManager().registerEvents((Listener)new PlayerLoginListener(this), (Plugin)this);
        if (WatchBlock.chestprotection) {
            this.getServer().getPluginManager().registerEvents((Listener)new ChestListener(this), (Plugin)this);
        }
        final File sqluptodate = new File("plugins" + File.separator + "WatchBlock" + File.separator + "sqlupdate-1");
        if (!WatchBlock.flatfile && !sqluptodate.exists()) {
            WatchBlock.queue.updateSQLtoIndexes();
            try {
                sqluptodate.createNewFile();
            }
            catch (IOException e6) {
                e6.printStackTrace();
            }
        }
    }
    
    public WorldEditPlugin getWorldEdit() {
        return WatchBlock.worldEdit;
    }
    
    public Permission getVault() {
        return WatchBlock.vault;
    }
    
    public boolean onCommand(final CommandSender sender, final Command cmd, final String commandLabel, final String[] args) {
        if (cmd.getName().equalsIgnoreCase("wallow") && sender instanceof Player && args.length == 1) {
            final Player player = (Player)sender;
            if (WatchBlock.flatfile) {
                final YamlConfiguration pC = this.getConfig(WatchBlock.allow);
                pC.set("allow." + player.getName() + "." + args[0].toLowerCase(), (Object)true);
                try {
                    pC.save(WatchBlock.allow);
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else {
                WatchBlock.queue.addAllowedPlayers(player.getName(), args[0]);
            }
            sender.sendMessage(ChatColor.GREEN + "[Notice] " + ChatColor.LIGHT_PURPLE + args[0] + " added to allowed players.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("wremove") && sender instanceof Player && args.length == 1) {
            final Player player = (Player)sender;
            if (WatchBlock.flatfile) {
                final YamlConfiguration pC = this.getConfig(WatchBlock.allow);
                pC.set("allow." + player.getName() + "." + args[0].toLowerCase(), (Object)null);
                try {
                    pC.save(WatchBlock.allow);
                }
                catch (Exception e2) {
                    System.err.print(String.valueOf(player.getName()) + " cannot type properly!");
                    System.err.print(e2);
                }
            }
            else {
                WatchBlock.queue.removeAllowedPlayers(player.getName(), args[0]);
            }
            sender.sendMessage(ChatColor.GREEN + "[Notice] " + ChatColor.LIGHT_PURPLE + args[0] + " removed from allowed players.");
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("wlist") && sender instanceof Player) {
            String allplayersallowed = "";
            if (WatchBlock.flatfile) {
                final YamlConfiguration config = this.getConfig(WatchBlock.allow);
                try {
                    final Set<String> temp = (Set<String>)config.getConfigurationSection("allow." + sender.getName()).getKeys(true);
                    for (final String r : temp) {
                        allplayersallowed = String.valueOf(allplayersallowed) + r + ",";
                    }
                }
                catch (NullPointerException ex3) {}
            }
            else {
                allplayersallowed = WatchBlock.queue.getAllowedPlayers(sender.getName());
            }
            sender.sendMessage(ChatColor.GREEN + "[Notice] " + ChatColor.LIGHT_PURPLE + "Allowed players: " + allplayersallowed);
            return true;
        }
        if (cmd.getName().equalsIgnoreCase("wtransfer")) {
            if (WatchBlock.worldEdit != null) {
                if ((sender.isOp() || WatchBlock.vault.has(sender, "watchblock.admin")) && args.length >= 0) {
                    final Player player = this.getServer().getPlayer(sender.getName());
                    final Selection select = WatchBlock.worldEdit.getSelection(player);
                    String temp2;
                    if (args.length > 0) {
                        temp2 = args[0];
                    }
                    else {
                        temp2 = null;
                    }
                    final String newowner = temp2;
                    final World world = player.getWorld();
                    if (WatchBlock.flatfile) {
                        final Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                player.sendMessage(ChatColor.RED + " [WatchBlock Notice] Starting Transfer of Ownership. This may take a bit.");
                                final File worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + world.getName());
                                final Location max = select.getMaximumPoint();
                                final Location min = select.getMinimumPoint();
                                final HashMap<String, LinkedList<Location>> tempmap = new HashMap<String, LinkedList<Location>>();
                                for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
                                    for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                                            final Location loc = new Location(world, (double)x, (double)y, (double)z);
                                            if (loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.LAVA && loc.getBlock().getType() != Material.WATER && !WatchBlock.excludeblocks.contains(String.valueOf(loc.getBlock().getTypeId()))) {
                                                final String chunkname = String.valueOf(loc.getChunk().getX()) + "." + loc.getChunk().getZ() + ".yml";
                                                if (tempmap.get(chunkname) == null) {
                                                    tempmap.put(chunkname, new LinkedList<Location>());
                                                }
                                                tempmap.get(chunkname).add(loc);
                                            }
                                        }
                                    }
                                    for (final String key : tempmap.keySet()) {
                                        LinkedList<Location> value = tempmap.get(key);
                                        final File customConfigFile = new File(worldpath + File.separator + key);
                                        final FileConfiguration flats = YamlConfiguration.loadConfiguration(customConfigFile);
                                        for (final Location location : value) {
                                            flats.set(String.valueOf(location.getBlockX()) + "," + location.getBlockY() + "," + location.getBlockZ() + ".player", (Object)newowner);
                                        }
                                        try {
                                            flats.save(customConfigFile);
                                        }
                                        catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                                player.sendMessage(ChatColor.GREEN + " [WatchBlock Notice] Transfer of Ownership done!");
                            }
                        });
                        thread.start();
                    }
                    else {
                        final Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                player.sendMessage(ChatColor.RED + " [WatchBlock Notice] Starting Transfer of Ownership. This may take a bit.");
                                final Location max = select.getMaximumPoint();
                                final Location min = select.getMinimumPoint();
                                for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
                                    for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                                            final Location loc = new Location(world, (double)x, (double)y, (double)z);
                                            if (loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.LAVA && loc.getBlock().getType() != Material.WATER && !WatchBlock.excludeblocks.contains(String.valueOf(loc.getBlock().getTypeId()))) {
                                                String check = null;
                                                check = WatchBlock.queue.getBlock(world.getName(), "", x, y, z, loc.getChunk().getX(), loc.getChunk().getZ());
                                                if (check == null) {}
                                                final String[] bid = check.split(",");
                                                for (int i = 0; i < bid.length; ++i, ++i) {
                                                    WatchBlock.queue.removeBlock(bid[i], world.getName());
                                                }
                                                if (newowner != null && newowner != "") {
                                                    WatchBlock.queue.addBlock(world.getName(), newowner, x, y, z, loc.getChunk().getX(), loc.getChunk().getZ());
                                                }
                                            }
                                        }
                                    }
                                }
                                player.sendMessage(ChatColor.GREEN + " [WatchBlock Notice] Transfer of Ownership done!");
                            }
                        });
                        thread.start();
                    }
                }
                if (WatchBlock.vault.has(sender, "watchblock.transfer") && !sender.isOp() && !WatchBlock.vault.has(sender, "watchblock.admin") && args.length >= 0) {
                    final Player player = this.getServer().getPlayer(sender.getName());
                    final Selection select = WatchBlock.worldEdit.getSelection(player);
                    String temp2;
                    if (args.length > 0) {
                        temp2 = args[0];
                    }
                    else {
                        temp2 = null;
                    }
                    final String newowner = temp2;
                    final World world = player.getWorld();
                    final String cmdsender = sender.getName();
                    if (WatchBlock.flatfile) {
                        final Thread thread2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                player.sendMessage(ChatColor.RED + " [WatchBlock Notice] Starting Transfer of Ownership. This may take a bit.");
                                final File worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + world.getName());
                                final Location max = select.getMaximumPoint();
                                final Location min = select.getMinimumPoint();
                                final HashMap<String, LinkedList<Location>> tempmap = new HashMap<String, LinkedList<Location>>();
                                for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
                                    for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                                            final Location loc = new Location(world, (double)x, (double)y, (double)z);
                                            if (loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.LAVA && loc.getBlock().getType() != Material.WATER && !WatchBlock.excludeblocks.contains(String.valueOf(loc.getBlock().getTypeId()))) {
                                                final String chunkname = String.valueOf(loc.getChunk().getX()) + "." + loc.getChunk().getZ() + ".yml";
                                                if (tempmap.get(chunkname) == null) {
                                                    tempmap.put(chunkname, new LinkedList<Location>());
                                                }
                                                tempmap.get(chunkname).add(loc);
                                            }
                                        }
                                    }
                                    for (final String key : tempmap.keySet()) {
                                        LinkedList<Location> value = tempmap.get(key);
                                        final File customConfigFile = new File(worldpath + File.separator + key);
                                        final FileConfiguration flats = YamlConfiguration.loadConfiguration(customConfigFile);
                                        for (final Location location : value) {
                                            if (flats.getString(String.valueOf(location.getBlockX()) + "," + location.getBlockY() + "," + location.getBlockZ() + ".player", "").equalsIgnoreCase(cmdsender)) {
                                                flats.set(String.valueOf(location.getBlockX()) + "," + location.getBlockY() + "," + location.getBlockZ() + ".player", newowner);
                                            }
                                        }
                                        try {
                                            flats.save(customConfigFile);
                                        }
                                        catch (IOException ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                                player.sendMessage(ChatColor.GREEN + " [WatchBlock Notice] Transfer of Ownership done!");
                            }
                        });
                        thread2.start();
                    }
                    else {
                        final Thread thread2 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                player.sendMessage(ChatColor.RED + " [WatchBlock Notice] Starting Transfer of Ownership. This may take a bit.");
                                final Location max = select.getMaximumPoint();
                                final Location min = select.getMinimumPoint();
                                for (int x = min.getBlockX(); x <= max.getBlockX(); ++x) {
                                    for (int y = min.getBlockY(); y <= max.getBlockY(); ++y) {
                                        for (int z = min.getBlockZ(); z <= max.getBlockZ(); ++z) {
                                            final Location loc = new Location(world, (double)x, (double)y, (double)z);
                                            if (loc.getBlock().getType() != Material.AIR && loc.getBlock().getType() != Material.LAVA && loc.getBlock().getType() != Material.WATER && !WatchBlock.excludeblocks.contains(String.valueOf(loc.getBlock().getTypeId()))) {
                                                String check = null;
                                                check = WatchBlock.queue.getBlock(world.getName(), cmdsender, x, y, z, loc.getChunk().getX(), loc.getChunk().getZ());
                                                if (check == null) {}
                                                final String[] bid = check.split(",");
                                                for (int i = 0; i < bid.length; ++i, ++i) {
                                                    String blockowner = null;
                                                    try {
                                                        blockowner = bid[i + 1];
                                                    }
                                                    catch (ArrayIndexOutOfBoundsException ex) {}
                                                    if (blockowner != null && blockowner.equalsIgnoreCase(cmdsender)) {
                                                        WatchBlock.queue.removeBlock(bid[i], world.getName());
                                                        WatchBlock.queue.addBlock(world.getName(), newowner, x, y, z, loc.getChunk().getX(), loc.getChunk().getZ());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                player.sendMessage(ChatColor.GREEN + " [WatchBlock Notice] Transfer of Ownership done!");
                            }
                        });
                        thread2.start();
                    }
                }
            }
            else {
                sender.sendMessage(ChatColor.RED + "WorldEdit not found");
            }
        }
        if (cmd.getName().equalsIgnoreCase("wdeleteplayer") && args.length > 0 && (sender.isOp() || WatchBlock.vault.has(sender, "watchblock.admin"))) {
            WatchBlock.queue.removePlayer(args[0]);
        }
        if (cmd.getName().equalsIgnoreCase("wtoggle")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("on")) {
                    WatchBlock.toggle.remove(sender.getName());
                    sender.sendMessage(ChatColor.GREEN + "[Notice] " + ChatColor.LIGHT_PURPLE + "Protection is turned on!");
                }
                else if (args[0].equalsIgnoreCase("off")) {
                    WatchBlock.toggle.put(sender.getName(), true);
                    sender.sendMessage(ChatColor.GREEN + "[Notice] " + ChatColor.LIGHT_PURPLE + "Protection is turned off!");
                }
            }
            else if (WatchBlock.toggle.containsKey(sender.getName())) {
                sender.sendMessage(ChatColor.GREEN + "[Notice] " + ChatColor.LIGHT_PURPLE + "Protection is turned off!");
            }
            else {
                sender.sendMessage(ChatColor.GREEN + "[Notice] " + ChatColor.LIGHT_PURPLE + "Protection is turned on!");
            }
        }
        if (cmd.getName().equalsIgnoreCase("wb-import") && sender.isOp() && args.length != 0) {
            if (args[0].equalsIgnoreCase("update-sql")) {
                WatchBlock.queue.updateSQLtoIndexes();
            }
            if (args[0].equalsIgnoreCase("oldsql-to-flat")) {
                String world_tables = "";
                ResultSet rs = WatchBlock.queue.ShowTables();
                try {
                    while (rs.next()) {
                        world_tables = String.valueOf(world_tables) + rs.getString(1) + "\n";
                    }
                    final String[] worldtablenames = world_tables.split("\n");
                    for (int i = 0; i < worldtablenames.length; ++i) {
                        rs = WatchBlock.queue.receiveEntries(worldtablenames[i]);
                        final String worldname = worldtablenames[i].split("\\[")[0];
                        final String chunkpath = worldtablenames[i].split("\\[")[1];
                        final String chunkx = chunkpath.split("/")[0];
                        final String chunky = chunkpath.split("/")[1].substring(0, chunkpath.split("/")[1].length() - 1);
                        final File worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + worldname);
                        if (!worldpath.exists()) {
                            worldpath.mkdir();
                        }
                        System.out.println("Importing Chunk" + worldname + "[" + chunkpath);
                        final File customConfigFile = new File(worldpath + File.separator + chunkx + "." + chunky + ".yml");
                        final FileConfiguration flats = (FileConfiguration)YamlConfiguration.loadConfiguration(customConfigFile);
                        while (rs.next()) {
                            final int x = rs.getInt(2);
                            final int y = rs.getInt(3);
                            final int z = rs.getInt(4);
                            final String player2 = rs.getString(5);
                            flats.set(String.valueOf(x) + "," + y + "," + z + ".player", (Object)player2);
                            try {
                                flats.save(customConfigFile);
                            }
                            catch (IOException e3) {
                                e3.printStackTrace();
                            }
                        }
                    }
                }
                catch (SQLException e4) {
                    e4.printStackTrace();
                }
            }
            if (args.length != 0 && args[0].equalsIgnoreCase("oldsql-to-newsql")) {
                String world_tables = "";
                ResultSet rs = WatchBlock.queue.ShowTables();
                try {
                    while (rs.next()) {
                        world_tables = String.valueOf(world_tables) + rs.getString(1) + "\n";
                    }
                    final String[] worldtablenames = world_tables.split("\n");
                    for (int i = 0; i < worldtablenames.length; ++i) {
                        if (worldtablenames[i].contains("[")) {
                            rs = WatchBlock.queue.receiveEntries(worldtablenames[i]);
                            final String worldname = worldtablenames[i].split("\\[")[0];
                            final String chunkpath = worldtablenames[i].split("\\[")[1];
                            final String chunkx = chunkpath.split("/")[0];
                            final String chunky = chunkpath.split("/")[1].substring(0, chunkpath.split("/")[1].length() - 1);
                            System.out.println("Importing Chunk " + worldname + " [" + chunkpath);
                            while (rs.next()) {
                                final int x2 = rs.getInt(2);
                                final int y2 = rs.getInt(3);
                                final int z2 = rs.getInt(4);
                                final String player3 = rs.getString(5);
                                WatchBlock.queue.addBlock(worldname, player3, x2, y2, z2, Integer.valueOf(chunkx), Integer.valueOf(chunky));
                            }
                            WatchBlock.queue.dropOldTable(worldtablenames[i]);
                        }
                    }
                }
                catch (SQLException e4) {
                    e4.printStackTrace();
                }
            }
            if (args[0].equalsIgnoreCase("sql-to-flat")) {
                final Thread thread3 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String world_tables = "";
                        ResultSet rs = WatchBlock.queue.newSQLShowWorlds();
                        try {
                            while (rs.next()) {
                                world_tables = String.valueOf(world_tables) + rs.getString("worldname") + "\n";
                            }
                            File customConfigFile = null;
                            FileConfiguration flats = null;
                            String chunkx = null;
                            String chunky = null;
                            File worldpath = null;
                            final String[] worldtablenames = world_tables.split("\n");
                            for (int i = 0; i < worldtablenames.length; ++i) {
                                rs = WatchBlock.queue.newReceiveEntries(worldtablenames[i]);
                                final String worldname = worldtablenames[i];
                                System.out.println("Importing World: " + worldname + " ! This may take a while!");
                                while (rs.next()) {
                                    chunkx = String.valueOf(rs.getInt("cx"));
                                    chunky = String.valueOf(rs.getInt("cz"));
                                    worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + worldname);
                                    if (!worldpath.exists()) {
                                        worldpath.mkdir();
                                    }
                                    customConfigFile = new File(worldpath + File.separator + chunkx + "." + chunky + ".yml");
                                    flats = (FileConfiguration)YamlConfiguration.loadConfiguration(customConfigFile);
                                    final int x = rs.getInt("x");
                                    final int y = rs.getInt("y");
                                    final int z = rs.getInt("z");
                                    final String player = rs.getString("playername");
                                    flats.set(String.valueOf(x) + "," + y + "," + z + ".player", (Object)player);
                                    try {
                                        flats.save(customConfigFile);
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                        catch (SQLException e2) {
                            e2.printStackTrace();
                        }
                    }
                });
                thread3.start();
            }
            if (args[0].equalsIgnoreCase("ownblocksx-to-flat")) {
                final String world_tables = "OwnBlocksX";
                ResultSet rs = null;
                try {
                    rs = WatchBlock.queue.receiveEntries(world_tables);
                    File customConfigFile2 = null;
                    FileConfiguration flats2 = null;
                    File worldpath2 = null;
                    while (rs.next()) {
                        final String worldname2 = rs.getString(2);
                        final int x3 = rs.getInt(3);
                        final int y3 = rs.getInt(4);
                        final int z3 = rs.getInt(5);
                        final String player4 = rs.getString(6);
                        final Location loc = new Location(this.getServer().getWorld(worldname2), (double)x3, (double)y3, (double)z3);
                        final String chunkx2 = String.valueOf(loc.getChunk().getX());
                        final String chunky2 = String.valueOf(loc.getChunk().getZ());
                        worldpath2 = new File("plugins" + File.separator + "WatchBlock" + File.separator + worldname2);
                        if (!worldpath2.exists()) {
                            worldpath2.mkdir();
                        }
                        System.out.println("Importing Chunk " + worldname2 + "[" + chunkx2 + "/" + chunky2 + "]");
                        customConfigFile2 = new File(worldpath2 + File.separator + chunkx2 + "." + chunky2 + ".yml");
                        flats2 = (FileConfiguration)YamlConfiguration.loadConfiguration(customConfigFile2);
                        flats2.set(String.valueOf(x3) + "," + y3 + "," + z3 + ".player", (Object)player4);
                        try {
                            flats2.save(customConfigFile2);
                        }
                        catch (IOException e5) {
                            e5.printStackTrace();
                        }
                    }
                }
                catch (SQLException e4) {
                    e4.printStackTrace();
                }
            }
            if (args[0].equalsIgnoreCase("ownblocksplus-to-sql")) {
                final String world_tables = "OwnBlocksX";
                ResultSet rs = null;
                try {
                    rs = WatchBlock.queue.receiveCount(world_tables);
                    int size = 0;
                    while (rs.next()) {
                        size = rs.getInt(1);
                    }
                    System.out.println(size);
                    rs = null;
                    System.out.println("Starting import this will take some time.");
                    for (int i = 0; i <= size; i += 100) {
                        System.out.println("Importing Blocks from: " + i);
                        rs = null;
                        rs = WatchBlock.queue.receiveEntries(world_tables, i, 100);
                        while (rs.next()) {
                            final String worldname = rs.getString("world");
                            final int x4 = rs.getInt("x");
                            final int y4 = rs.getInt("y");
                            final int z4 = rs.getInt("z");
                            final String player5 = rs.getString("owner");
                            final Location loc2 = new Location(this.getServer().getWorld(worldname), (double)x4, (double)y4, (double)z4);
                            try {
                                WatchBlock.queue.addBlock(worldname, player5, x4, y4, z4, loc2.getChunk().getX(), loc2.getChunk().getZ());
                            }
                            catch (NullPointerException e6) {
                                System.out.println("This Chunk was not found, maybe you deleted the world: " + worldname);
                            }
                        }
                    }
                    rs.close();
                    System.out.println("Import from db done, Please wait for import for of WatchBlock DB (you can watch in phpmyAdmin \nor wait until Server CPU is back to normal. Then restart your Server once you are sure everything is imported.");
                }
                catch (SQLException e4) {
                    e4.printStackTrace();
                }
            }
            if (args[0].equalsIgnoreCase("flat-to-sql") && args[1] != null) {
                final String world2 = args[1];
                final Thread thread4 = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Running new Import!!!");
                        final File dir = new File("plugins" + File.separator + "WatchBlock" + File.separator + world2 + File.separator);
                        final String[] files = dir.list();
                        for (int i = 0; i < files.length; ++i) {
                            final String file = String.valueOf(dir.getAbsolutePath()) + File.separator + files[i];
                            final YamlConfiguration pC = YamlConfiguration.loadConfiguration(new File(file));
                            String worldaddition = files[i].replace(".yml", "");
                            worldaddition = worldaddition.replace(".", ",");
                            System.out.println(file);
                            final Set<String> temp = (Set<String>)pC.getKeys(true);
                            for (final String r : temp) {
                                String fixformatting = r.replace(".player", "");
                                fixformatting = fixformatting.replace(".type", "");
                                final String[] location = fixformatting.split(",");
                                final String player = pC.getString(String.valueOf(r) + ".player", "");
                                if (player != null && !player.contentEquals("null") && !player.contentEquals("") && !location[0].contentEquals("") && !location[1].contentEquals("") && !location[2].contentEquals("") && location[0] != null && location[1] != null && location[2] != null && worldaddition != null) {
                                    WatchBlock.queue.addBlock(world2, player, Integer.valueOf(location[0]), Integer.valueOf(location[1]), Integer.valueOf(location[2]), Integer.valueOf(worldaddition.split(",")[0]), Integer.valueOf(worldaddition.split(",")[1]));
                                }
                            }
                        }
                        System.out.println("Import finished");
                    }
                });
                thread4.start();
            }
            if (args[0].equalsIgnoreCase("bp-convert")) {
                sender.sendMessage("Developed by Tjnome, improved by slade67");
                if (args[1] != null && args[2] != null) {
                    final File dbFile = new File("plugins" + File.separatorChar + "WatchBlock" + File.separatorChar + args[2]);
                    try {
                        final RandomAccessFile rf = new RandomAccessFile(dbFile, "r");
                        while (true) {
                            final int x5 = rf.readInt();
                            final int y5 = rf.readInt();
                            final int z5 = rf.readInt();
                            String player6 = "";
                            while (true) {
                                final char curr = rf.readChar();
                                if (curr == '\0') {
                                    break;
                                }
                                player6 = String.valueOf(player6) + curr;
                            }
                            rf.close();
                            WatchBlock.database.put(new BPBlockLocation(x5, y5, z5), player6);
                        }
                    }
                    catch (EOFException ex2) {
                        System.out.println("Finished loading database.");
                    }
                    catch (IOException ex) {
                        System.out.println("Error while reading the database.");
                        ex.printStackTrace();
                    }
                    if (args[1].equalsIgnoreCase("sql")) {
                        final Thread thread4 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                Connection conn = null;
                                try {
                                    conn = WatchBlock.this.getConnection();
                                    for (final Map.Entry<?, ?> e : WatchBlock.database.entrySet()) {
                                        final BPBlockLocation currBlock = (BPBlockLocation)e.getKey();
                                        final Location loc = new Location(WatchBlock.this.getServer().getWorld("World"), (double)currBlock.getX(), (double)currBlock.getY(), (double)currBlock.getZ());
                                        WatchBlock.queue.addBlock("world", (String)e.getValue(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getChunk().getX(), loc.getChunk().getX());
                                    }
                                    try {
                                        conn.commit();
                                        conn.close();
                                    }
                                    catch (NullPointerException e2) {
                                        e2.printStackTrace();
                                    }
                                }
                                catch (SQLException localSQLException) {
                                    localSQLException.printStackTrace();
                                }
                                System.out.println("Block Protection Database successfully integrated into WatchBlock");
                            }
                        });
                        thread4.start();
                    }
                    if (args[1].equalsIgnoreCase("flat")) {
                        final Thread thread4 = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final String worldname = "world";
                                final File worldpath = new File("plugins" + File.separator + "WatchBlock" + File.separator + worldname);
                                if (!worldpath.exists()) {
                                    worldpath.mkdir();
                                }
                                for (final Map.Entry<?, ?> e : WatchBlock.database.entrySet()) {
                                    final BPBlockLocation currBlock = (BPBlockLocation)e.getKey();
                                    final Location loc = new Location(WatchBlock.this.getServer().getWorld("world"), (double)currBlock.getX(), (double)currBlock.getY(), (double)currBlock.getZ());
                                    final File customConfigFile = new File(worldpath + File.separator + loc.getChunk().getX() + "." + loc.getChunk().getZ() + ".yml");
                                    final FileConfiguration flats = (FileConfiguration)YamlConfiguration.loadConfiguration(customConfigFile);
                                    flats.set(String.valueOf(loc.getBlockX()) + "," + loc.getBlockY() + "," + loc.getBlockZ() + ".player", (Object)e.getValue());
                                    try {
                                        flats.save(customConfigFile);
                                    }
                                    catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                System.out.println("Block Protection Database successfully integrated into WatchBlock");
                            }
                        });
                        thread4.start();
                    }
                }
            }
        }
        if (cmd.getName().equalsIgnoreCase("wreload") && sender.isOp()) {
            this.loadConf();
        }
        if (cmd.getName().equalsIgnoreCase("bananaprotect-import") && sender.isOp()) {
            final String world2 = args[0];
            final Thread thread4 = new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Running new Import!!!");
                    final File dir = new File("plugins" + File.separator + "BananaProtect" + File.separator + world2 + File.separator);
                    final String[] files = dir.list();
                    for (int i = 0; i < files.length; ++i) {
                        final String file = String.valueOf(dir.getAbsolutePath()) + File.separator + files[i];
                        final YamlConfiguration pC = YamlConfiguration.loadConfiguration(new File(file));
                        String worldaddition = files[i].replace(".yml", "");
                        worldaddition = worldaddition.replace(".", ",");
                        System.out.println(file);
                        final Set<String> temp = (Set<String>)pC.getKeys(true);
                        for (final String r : temp) {
                            final String[] location = r.split(",");
                            final String player = pC.getString(String.valueOf(r) + ".player.");
                            if (player != null && !player.contentEquals("null") && !location[0].contentEquals("") && !location[1].contentEquals("") && !location[2].contentEquals("")) {
                                WatchBlock.queue.addBlock(world2, player, Integer.valueOf(location[0]), Integer.valueOf(location[1]), Integer.valueOf(location[2]), Integer.valueOf(worldaddition.split(",")[0]), Integer.valueOf(worldaddition.split(",")[1]));
                            }
                        }
                    }
                    System.out.println("Import finished");
                }
            });
            thread4.start();
        }
        return false;
    }
}
