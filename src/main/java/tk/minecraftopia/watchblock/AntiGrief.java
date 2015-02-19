package tk.minecraftopia.watchblock;

import org.bukkit.configuration.file.*;
import org.bukkit.block.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import java.util.*;
import org.bukkit.event.player.*;
import org.bukkit.*;
import org.bukkit.event.block.*;

public class AntiGrief implements Listener
{
    private WatchBlock plugin;
    YamlConfiguration config;
    static HashMap<String, HashMap<String, LinkedList<String>>> worlds;
    static LinkedHashMap<Block, String> pistoncache;
    
    static {
        AntiGrief.worlds = new HashMap<String, HashMap<String, LinkedList<String>>>();
        AntiGrief.pistoncache = new LinkedHashMap<Block, String>();
    }
    
    public AntiGrief(final WatchBlock callbackPlugin) {
        System.out.println("[WatchBlock] AntiGrief started and running");
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
            final Block piston = event.getBlock();
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
            final Block above = piston.getRelative(x, y, z);
            final String world = piston.getWorld().getName();
            String cached = AntiGrief.pistoncache.get(piston);
            String pistonowner;
            if (cached == null) {
                pistonowner = WatchBlock.queue.getBlock(world, "", piston.getX(), piston.getY(), piston.getZ(), piston.getChunk().getX(), piston.getChunk().getZ());
                try {
                    pistonowner = pistonowner.split(",")[1];
                }
                catch (ArrayIndexOutOfBoundsException ex) {}
                AntiGrief.pistoncache.put(piston, pistonowner);
            }
            else {
                pistonowner = cached;
            }
            String blockowner = null;
            cached = null;
            cached = AntiGrief.pistoncache.get(above);
            if (cached == null) {
                final String get = WatchBlock.queue.getBlock(world, "", above.getX(), above.getY(), above.getZ(), above.getChunk().getX(), above.getChunk().getZ());
                try {
                    blockowner = get.split(",")[1];
                }
                catch (ArrayIndexOutOfBoundsException ex2) {}
                AntiGrief.pistoncache.put(above, blockowner);
            }
            else {
                blockowner = cached;
            }
            final Player player = this.plugin.getServer().getPlayer(pistonowner);
            if (player != null) {
                if (!player.isOp() && !this.plugin.getVault().has(player, "watchblock.admin") && !player.getName().equalsIgnoreCase(blockowner) && blockowner != null && blockowner != "") {
                    try {
                        final String allowed = WatchBlock.queue.getAllowedPlayers(blockowner);
                        final String[] check = allowed.split(" ");
                        String[] array;
                        for (int length = (array = check).length, i = 0; i < length; ++i) {
                            final String r = array[i];
                            if (r.equalsIgnoreCase(player.getName())) {
                                event.setCancelled(false);
                                return;
                            }
                        }
                    }
                    catch (NullPointerException localNullPointerException1) {
                        event.setCancelled(true);
                        return;
                    }
                    String chatmsg = WatchBlock.pistonmsg;
                    try {
                        chatmsg = WatchBlock.pistonmsg.replace("$player", blockowner);
                    }
                    catch (NullPointerException ex3) {}
                    if (show) {
                        if (chatmsg != "") {
                            player.sendMessage(ChatColor.RED + chatmsg);
                        }
                        show = false;
                    }
                    event.setCancelled(true);
                }
            }
            else {
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
        final Block block = event.getBlock();
        boolean show = true;
        final String world = block.getWorld().getName();
        String cached = AntiGrief.pistoncache.get(block);
        String pistonowner;
        if (cached == null) {
            pistonowner = WatchBlock.queue.getBlock(world, "", block.getX(), block.getY(), block.getZ(), block.getChunk().getX(), block.getChunk().getZ());
            try {
                pistonowner = pistonowner.split(",")[1];
            }
            catch (ArrayIndexOutOfBoundsException ex) {}
            AntiGrief.pistoncache.put(block, pistonowner);
        }
        else {
            pistonowner = cached;
        }
        for (final Block above : event.getBlocks()) {
            cached = null;
            String blockowner = null;
            cached = AntiGrief.pistoncache.get(above);
            if (cached == null) {
                final String get = WatchBlock.queue.getBlock(world, "", above.getX(), above.getY(), above.getZ(), above.getChunk().getX(), above.getChunk().getZ());
                try {
                    blockowner = get.split(",")[1];
                }
                catch (ArrayIndexOutOfBoundsException ex2) {}
                AntiGrief.pistoncache.put(above, blockowner);
            }
            else {
                blockowner = cached;
            }
            final Player player = this.plugin.getServer().getPlayer(pistonowner);
            if (player != null) {
                if (player.isOp() || this.plugin.getVault().has(player, "watchblock.admin") || player.getName().equalsIgnoreCase(blockowner) || blockowner == null || blockowner == "") {
                    continue;
                }
                try {
                    final String allowed = WatchBlock.queue.getAllowedPlayers(blockowner);
                    final String[] check = allowed.split(" ");
                    String[] array;
                    for (int length = (array = check).length, i = 0; i < length; ++i) {
                        final String r = array[i];
                        if (r.equalsIgnoreCase(player.getName())) {
                            event.setCancelled(false);
                            return;
                        }
                    }
                }
                catch (NullPointerException localNullPointerException1) {
                    event.setCancelled(true);
                    return;
                }
                if (show) {
                    String chatmsg = WatchBlock.pistonmsg;
                    try {
                        chatmsg = WatchBlock.pistonmsg.replace("$player", blockowner);
                    }
                    catch (NullPointerException ex3) {}
                    if (chatmsg != "") {
                        player.sendMessage(ChatColor.RED + chatmsg);
                    }
                    show = false;
                }
                event.setCancelled(true);
            }
            else {
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
        final World world = block.getWorld();
        final Chunk chunk = block.getChunk();
        if (!player.isOp() && !this.plugin.getVault().has(player, "watchblock.admin")) {
            final String get = WatchBlock.queue.getBlock(world.getName(), "", block.getX(), block.getY(), block.getZ(), chunk.getX(), chunk.getZ());
            String test = null;
            try {
                test = get.split(",")[1];
            }
            catch (ArrayIndexOutOfBoundsException ex) {}
            if (!player.getName().equalsIgnoreCase(test) && test != null) {
                this.config = YamlConfiguration.loadConfiguration(WatchBlock.allow);
                try {
                    final String allowed = WatchBlock.queue.getAllowedPlayers(test);
                    final String[] check = allowed.split(" ");
                    String[] array;
                    for (int length = (array = check).length, i = 0; i < length; ++i) {
                        final String r = array[i];
                        if (r.equalsIgnoreCase(player.getName())) {
                            event.setCancelled(false);
                            return;
                        }
                    }
                }
                catch (NullPointerException localNullPointerException1) {
                    event.setCancelled(true);
                    return;
                }
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
    public void onBlockBreak(final BlockBreakEvent event) {
        if (!WatchBlock.worlds.contains(event.getBlock().getWorld().getName())) {
            AntiGrief.pistoncache.remove(event.getBlock());
            return;
        }
        final Player player = event.getPlayer();
        final String worldchuck = event.getBlock().getWorld().getName();
        String playername = this.plugin.getSQLQueueInstance().getBlock(worldchuck, "", event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
        String[] roate = null;
        String id = null;
        boolean playerfix = false;
        try {
            roate = playername.split(",");
            for (int i = 1; i < roate.length; ++i, ++i) {
                if (roate[i].contentEquals(player.getName())) {
                    playername = roate[i];
                    id = roate[i - 1];
                    playerfix = true;
                }
                else {
                    playername = roate[i];
                    id = roate[i - 1];
                }
            }
        }
        catch (NullPointerException ex) {}
        catch (ArrayIndexOutOfBoundsException ex2) {}
        if (playername == null || id == null) {
            if ((this.plugin.getVault().has(player, "watchblock.admin") || player.isOp()) && player.getItemInHand().getType().compareTo((Enum)Material.getMaterial(WatchBlock.adminstick)) == 0) {
                String chatmsg = WatchBlock.adminmsg;
                try {
                    chatmsg = WatchBlock.adminmsg.replace("$player", " Nobody");
                }
                catch (NullPointerException ex3) {}
                if (chatmsg != "") {
                    event.getPlayer().sendMessage(ChatColor.RED + chatmsg);
                }
                event.setCancelled(true);
            }
            return;
        }
        if (!playername.equalsIgnoreCase(player.getName()) && !player.isOp() && !this.plugin.getVault().has(player, "watchblock.admin") && !playerfix) {
            try {
                final String allowed = WatchBlock.queue.getAllowedPlayers(playername);
                final String[] check = allowed.split(" ");
                String[] array;
                for (int length = (array = check).length, j = 0; j < length; ++j) {
                    final String r = array[j];
                    if (r.equalsIgnoreCase(player.getName())) {
                        event.setCancelled(false);
                        return;
                    }
                }
            }
            catch (NullPointerException localNullPointerException1) {
                event.setCancelled(true);
                return;
            }
            String chatmsg = WatchBlock.ownedmsg;
            try {
                chatmsg = WatchBlock.ownedmsg.replace("$player", playername);
            }
            catch (NullPointerException ex4) {}
            if (chatmsg != "") {
                event.getPlayer().sendMessage(ChatColor.RED + chatmsg);
            }
            event.setCancelled(true);
            return;
        }
        playerfix = false;
        if ((this.plugin.getVault().has(player, "watchblock.admin") || player.isOp()) && player.getItemInHand().getType().compareTo((Enum)Material.getMaterial(WatchBlock.adminstick)) == 0) {
            String adminmsg = WatchBlock.adminmsg;
            try {
                adminmsg = WatchBlock.adminmsg.replace("$player", playername);
            }
            catch (NullPointerException ex5) {}
            if (adminmsg != "") {
                event.getPlayer().sendMessage(ChatColor.RED + adminmsg);
            }
            event.setCancelled(true);
            return;
        }
        for (int i = 0; i < roate.length; ++i, ++i) {
            this.plugin.getSQLQueueInstance().removeBlock(roate[i], event.getBlock().getWorld().getName());
        }
        AntiGrief.pistoncache.remove(event.getBlock());
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockPlace(final BlockPlaceEvent event) {
        if (WatchBlock.worlds.contains(event.getBlock().getWorld().getName()) && !WatchBlock.toggle.containsKey(event.getPlayer().getName()) && !event.isCancelled() && event.getBlock().getType().compareTo((Enum)Material.SAPLING) != 0) {
            if (!WatchBlock.protectaround && !WatchBlock.excludeblocks.contains(String.valueOf(event.getBlock().getTypeId()))) {
                if (this.plugin.getVault().has(event.getPlayer(), "watchblock.protect")) {
                    this.plugin.getSQLQueueInstance().addBlock(event.getBlock().getWorld().getName(), event.getPlayer().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
                }
            }
            else {
                final Block block1 = event.getBlockAgainst();
                final Block block2 = event.getBlock().getRelative(0, -1, 0);
                final LinkedList<Block> blocksaround = new LinkedList<Block>();
                blocksaround.add(block1);
                blocksaround.add(block2);
                for (final Block above : blocksaround) {
                    final String world = event.getBlock().getWorld().getName();
                    final String get = this.plugin.getSQLQueueInstance().getBlock(world, "", above.getX(), above.getY(), above.getZ(), above.getChunk().getX(), above.getChunk().getZ());
                    String test = null;
                    String playername = null;
                    try {
                        final String[] temp = get.split(",");
                        if (temp.length == 2) {
                            test = temp[1];
                        }
                        if (temp.length > 2) {
                            final LinkedList<String> removeoldowner = new LinkedList<String>();
                            boolean delete_flat = false;
                            for (int j = 0; j < temp.length; ++j, ++j) {
                                if (event.getPlayer().getName().equalsIgnoreCase(temp[j + 1])) {
                                    delete_flat = true;
                                    test = temp[j + 1];
                                }
                                else {
                                    if (test == null) {
                                        test = temp[j + 1];
                                    }
                                    removeoldowner.add(temp[j]);
                                }
                                if (delete_flat) {
                                    final Object localObject = removeoldowner.iterator();
                                    while (((Iterator)localObject).hasNext()) {
                                        final String removeme = ((Iterator)localObject).next();
                                        this.plugin.getSQLQueueInstance().removeBlock(removeme, world);
                                    }
                                }
                            }
                        }
                    }
                    catch (ArrayIndexOutOfBoundsException ex) {}
                    playername = test;
                    if (playername == null || (playername == "" && !WatchBlock.excludeblocks.contains(String.valueOf(event.getBlock().getTypeId())) && this.plugin.getVault().has(event.getPlayer(), "watchblock.protect"))) {
                        this.plugin.getSQLQueueInstance().addBlock(event.getBlock().getWorld().getName(), event.getPlayer().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
                        return;
                    }
                    if ((playername.equalsIgnoreCase(event.getPlayer().getName()) || event.getPlayer().isOp() || this.plugin.getVault().playerHas(event.getPlayer(), "watchblock.admin")) && !WatchBlock.excludeblocks.contains(String.valueOf(event.getBlock().getTypeId())) && this.plugin.getVault().has(event.getPlayer(), "watchblock.protect")) {
                        this.plugin.getSQLQueueInstance().addBlock(event.getBlock().getWorld().getName(), event.getPlayer().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getChunk().getX(), event.getBlock().getChunk().getZ());
                        return;
                    }
                    if (!playername.equalsIgnoreCase(event.getPlayer().getName())) {
                        try {
                            final String allowed = WatchBlock.queue.getAllowedPlayers(playername);
                            final String[] check = allowed.split(" ");
                            String[] array;
                            for (int length = (array = check).length, i = 0; i < length; ++i) {
                                final String r = array[i];
                                if (r.equalsIgnoreCase(event.getPlayer().getName())) {
                                    event.setCancelled(false);
                                    return;
                                }
                            }
                        }
                        catch (NullPointerException localNullPointerException1) {
                            event.setCancelled(true);
                            return;
                        }
                        String chatmsg = WatchBlock.abovemsg;
                        try {
                            chatmsg = WatchBlock.abovemsg.replace("$player", test);
                        }
                        catch (NullPointerException ex2) {}
                        if (chatmsg != "") {
                            event.getPlayer().sendMessage(ChatColor.RED + chatmsg);
                        }
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
