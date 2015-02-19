package tk.minecraftopia.util;

import tk.minecraftopia.watchblock.*;
import java.util.*;
import org.bukkit.plugin.*;
import java.sql.*;

public class SQLQueue extends Thread
{
    private WatchBlock watchblock;
    static LinkedList<String> removequeue;
    static LinkedList<String> addqueue;
    static boolean removelocked;
    static boolean addlocked;
    
    static {
        SQLQueue.removequeue = new LinkedList<String>();
        SQLQueue.addqueue = new LinkedList<String>();
        SQLQueue.removelocked = false;
        SQLQueue.addlocked = false;
    }
    
    public SQLQueue(final WatchBlock watchblock) {
        this.watchblock = watchblock;
        this.watchblock.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.watchblock, (Runnable)new Runnable() {
            @Override
            public void run() {
                if (SQLQueue.addqueue.size() > 0 && !SQLQueue.addlocked) {
                    SQLQueue.this.addBlockQueueToDB();
                }
            }
        }, 10L, 25L);
        this.watchblock.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this.watchblock, (Runnable)new Runnable() {
            @Override
            public void run() {
                if (SQLQueue.removequeue.size() > 0 && !SQLQueue.removelocked) {
                    SQLQueue.this.removeBlockQueueToDB();
                }
            }
        }, 15L, 25L);
    }
    
    public synchronized ResultSet ShowTables() {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.executeQuery("show tables");
            rs = state.getResultSet();
            state.close();
            conn.close();
            return rs;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized void removePlayer(final String playername) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.executeQuery("select pid from players where playername='" + playername + "';");
            rs = state.getResultSet();
            while (rs.next()) {
                state.execute("delete from players where pid='" + rs.getInt("pid") + "';");
            }
            rs.close();
            state.close();
            conn.close();
        }
        catch (SQLException e2) {
            try {
                conn.close();
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
            e2.printStackTrace();
        }
    }
    
    public synchronized ResultSet newSQLShowWorlds() {
        final Connection conn = this.watchblock.getConnection();
        try {
            final Statement state = conn.createStatement();
            state.executeQuery("select id,worldname from worlds");
            return state.getResultSet();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized void updateSQLtoIndexes() {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.execute("create index player_idx on players (pid,playername);");
            state.execute("create index worldname_idx on worlds (worldname)");
            state.execute("create index playername_idx on  players (playername);");
            state.execute("select worldname from worlds;");
            rs = state.getResultSet();
            while (rs.next()) {
                final String worldname = rs.getString("worldname");
                final Statement update = conn.createStatement();
                update.execute("create index chunk_idx on " + worldname + "_chunks (cx,cz);");
                update.execute("create index cid_idx on " + worldname + "_chunks (cid,cx,cz);");
                update.execute("create index coor_idx on " + worldname + "_blocks (bid,cid,x,y,z);");
                update.execute("create index blockid_idx on " + worldname + "_blocks (cid,bid);");
                update.execute("create index b_idx on " + worldname + "_blocks (cid,x,y,z);");
                update.execute("create index coors_idx on " + worldname + "_blocks (x,y,z);");
                update.close();
            }
            state.close();
            rs.close();
            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized ResultSet newReceiveEntries(final String world) {
        final Connection conn = this.watchblock.getConnection();
        try {
            final Statement state = conn.createStatement();
            final String select = "SELECT id,worldname," + world + "_chunks.cid,mainid,cx,cz,bid," + world + "_blocks.cid,x,y,z,player,pid,playername FROM worlds JOIN " + world + "_chunks ON worlds.id = " + world + "_chunks.mainid " + "JOIN " + world + "_blocks ON " + world + "_chunks.cid = " + world + "_blocks.cid " + "JOIN players ON " + world + "_blocks.player = players.pid ";
            state.executeQuery(select);
            return state.getResultSet();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized ResultSet dropOldTable(final String table) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.executeUpdate("drop table `" + table + "`");
            rs = state.getResultSet();
            state.close();
            conn.close();
            return rs;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized ResultSet receiveEntries(final String table) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.executeQuery("select world,x,y,z,owner from `" + table + "`");
            rs = state.getResultSet();
            conn.close();
            return rs;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized ResultSet receiveCount(final String table) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.executeQuery("select count(id) from `" + table + "`;");
            rs = state.getResultSet();
            conn.close();
            return rs;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized ResultSet receiveEntries(final String table, final int limit_min, final int limit_max) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.executeQuery("select world,x,y,z,owner from `" + table + "` Limit " + limit_min + " , " + limit_max + ";");
            rs = state.getResultSet();
            conn.close();
            return rs;
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public synchronized void createWorldTables(final String world) {
        final Connection conn = this.watchblock.getConnection();
        try {
            conn.setAutoCommit(false);
            final Statement state = conn.createStatement();
            state.execute("create table IF NOT EXISTS `worlds` (id INT NOT NULL AUTO_INCREMENT , worldname varchar(50) NOT NULL,PRIMARY KEY (id), KEY worldname_idx (worldname)) ENGINE=MyISAM");
            state.execute("create table IF NOT EXISTS `" + world + "_chunks" + "` (cid INT NOT NULL AUTO_INCREMENT , mainid int NOT NULL, cx int NOT NULL,  cz int NOT NULL," + "PRIMARY KEY (cid), KEY cid_idx (cid,cx,cz),  KEY chunk_idx (cx,cz)) ENGINE=MyISAM ");
            state.execute("create table IF NOT EXISTS `players` (pid INT NOT NULL AUTO_INCREMENT ,playername varchar(50),PRIMARY KEY (pid), KEY player_idx (pid,playername),  KEY playername_idx (playername)) ENGINE=MyISAM ");
            state.execute("create table IF NOT EXISTS`" + world + "_blocks" + "` (bid INT NOT NULL AUTO_INCREMENT, cid int NOT NULL ,x int NOT NULL, y int NOT NULL, z int NOT NULL, player int NOT NULL, " + "PRIMARY KEY (bid),KEY coors_idx (x,y,z),  KEY coor_idx (bid,cid,x,y,z),  KEY blockid_idx (cid,bid),  KEY b_idx (cid,x,y,z)) ENGINE=MyISAM");
            state.execute("CREATE TABLE IF NOT EXISTS allowlist (pid int NOT NULL,allowedplayers varchar(1500) DEFAULT NULL,KEY pid (pid))  ENGINE=MyISAM");
            conn.commit();
            state.close();
            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void addBlockQueueToDB() {
        if (SQLQueue.addqueue.size() > 0) {
            SQLQueue.addlocked = true;
            final Connection conn = this.watchblock.getConnection();
            if (SQLQueue.addqueue.size() > 2000) {
                System.out.println("[WatchBlock] SQL Add Block Queuesize: " + SQLQueue.addqueue.size());
            }
            final int size = SQLQueue.addqueue.size();
            try {
                final Statement state = conn.createStatement();
                conn.setAutoCommit(false);
                for (int i = 0; i < size; ++i) {
                    try {
                        final String add = SQLQueue.addqueue.pollFirst();
                        final String[] temp = add.split(",");
                        ResultSet rs = null;
                        state.execute("select id from worlds where worldname = '" + temp[0] + "'");
                        rs = state.getResultSet();
                        int worldid = -1;
                        int chunkid = -1;
                        int playerid = -1;
                        if (rs.next()) {
                            worldid = rs.getInt("id");
                        }
                        else {
                            state.execute("insert into worlds (worldname) VALUES ('" + temp[0] + "')");
                            state.execute("select id,worldname from worlds where worldname = '" + temp[0] + "'");
                            conn.commit();
                            final ResultSet retry = state.getResultSet();
                            retry.next();
                            worldid = retry.getInt("id");
                        }
                        rs = null;
                        state.execute("select pid from players where playername = '" + temp[1] + "'");
                        rs = state.getResultSet();
                        if (rs.next()) {
                            playerid = rs.getInt("pid");
                        }
                        else {
                            state.execute("insert into players (playername) VALUES ('" + temp[1] + "')");
                            state.execute("select pid from players where playername = '" + temp[1] + "'");
                            conn.commit();
                            final ResultSet retry = state.getResultSet();
                            retry.next();
                            playerid = retry.getInt("pid");
                        }
                        rs = null;
                        state.execute("select cid from " + temp[0] + "_chunks where cx = '" + temp[5] + "' AND cz='" + temp[6] + "'");
                        rs = state.getResultSet();
                        if (rs.next()) {
                            chunkid = rs.getInt("cid");
                        }
                        else {
                            state.execute("insert into " + temp[0] + "_chunks (mainid, cx, cz) VALUES ('" + worldid + "','" + temp[5] + "','" + temp[6] + "')");
                            state.execute("select cid from " + temp[0] + "_chunks where cx = '" + temp[5] + "' AND cz='" + temp[6] + "'");
                            conn.commit();
                            final ResultSet retry = state.getResultSet();
                            retry.next();
                            chunkid = retry.getInt("cid");
                        }
                        state.execute("insert into `" + temp[0] + "_blocks` (cid,x,y,z,player) VALUES ('" + chunkid + "','" + temp[2] + "','" + temp[3] + "','" + temp[4] + "','" + playerid + "');");
                        conn.commit();
                        rs.close();
                    }
                    catch (NullPointerException ex) {}
                }
                state.close();
                conn.close();
                SQLQueue.addlocked = false;
                if (size > 2000) {
                    System.out.println("The Addqueue is back to normal: 0, If you ran an import it should be finished now.");
                }
            }
            catch (SQLException e2) {
                try {
                    conn.close();
                }
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
                e2.printStackTrace();
            }
        }
        SQLQueue.addlocked = false;
    }
    
    public synchronized String getAllowedPlayers(final String player) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        Statement state = null;
        String who = "";
        int pid = 0;
        try {
            state = conn.createStatement();
            rs = state.getResultSet();
            rs = null;
            state.execute("select pid from players where playername = '" + player + "';");
            rs = state.getResultSet();
            if (rs.next()) {
                pid = rs.getInt("pid");
            }
            rs = null;
            if (player != "") {
                final String select = "SELECT allowedplayers from allowlist where pid='" + pid + "';";
                state = conn.createStatement();
                state.executeQuery(select);
                rs = state.getResultSet();
            }
            if (rs.next()) {
                who = rs.getString("allowedplayers");
            }
            rs.close();
            state.close();
            conn.close();
        }
        catch (SQLException e2) {
            try {
                conn.close();
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
            e2.printStackTrace();
        }
        return who;
    }
    
    public synchronized String addAllowedPlayers(final String player, final String add) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        Statement state = null;
        String who = "";
        int pid = 0;
        try {
            state = conn.createStatement();
            rs = state.getResultSet();
            rs = null;
            state.execute("select pid from players where playername = '" + player + "';");
            rs = state.getResultSet();
            if (rs.next()) {
                pid = rs.getInt("pid");
            }
            rs = null;
            if (player != "") {
                final String select = "SELECT allowedplayers from allowlist where pid='" + pid + "';";
                state = conn.createStatement();
                state.executeQuery(select);
                rs = state.getResultSet();
            }
            if (rs.next()) {
                who = rs.getString("allowedplayers");
            }
            if (who == "" || who == null) {
                who = String.valueOf(who) + add + " ";
                final String update = "insert into allowlist(pid,allowedplayers) VALUES('" + pid + "','" + who + "');";
                state = conn.createStatement();
                state.execute(update);
            }
            else {
                who = String.valueOf(who) + add + " ";
                final String update = "update allowlist set allowedplayers='" + who + "' where pid='" + pid + "';";
                state = conn.createStatement();
                state.execute(update);
            }
            rs.close();
            state.close();
            conn.close();
        }
        catch (SQLException e2) {
            try {
                conn.close();
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
            e2.printStackTrace();
        }
        return who;
    }
    
    public synchronized String removeAllowedPlayers(final String player, final String remove) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        Statement state = null;
        String who = "";
        int pid = 0;
        try {
            state = conn.createStatement();
            rs = state.getResultSet();
            rs = null;
            state.execute("select pid from players where playername = '" + player + "';");
            rs = state.getResultSet();
            if (rs.next()) {
                pid = rs.getInt("pid");
            }
            rs = null;
            if (player != "") {
                final String select = "SELECT allowedplayers from allowlist where pid='" + pid + "';";
                state = conn.createStatement();
                state.executeQuery(select);
                rs = state.getResultSet();
            }
            if (rs.next()) {
                who = rs.getString("allowedplayers");
            }
            who = who.replace(String.valueOf(remove) + " ", "");
            final String update = "update allowlist set allowedplayers='" + who + "' where pid='" + pid + "';";
            state = conn.createStatement();
            state.execute(update);
            rs.close();
            state.close();
            conn.close();
        }
        catch (SQLException e2) {
            try {
                conn.close();
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
            e2.printStackTrace();
        }
        return who;
    }
    
    public synchronized String getBlock(final String world, final String player, final int x, final int y, final int z, final int cx, final int cz) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        Statement state = null;
        String who = "";
        try {
            if (player != "") {
                final String select = "SELECT bid,playername FROM worlds JOIN " + world + "_chunks ON worlds.id = " + world + "_chunks.mainid " + "JOIN " + world + "_blocks ON " + world + "_chunks.cid = " + world + "_blocks.cid " + "JOIN players ON " + world + "_blocks.player = players.pid " + "WHERE worldname='" + world + "' AND playername='" + player + "' AND x='" + x + "' AND y='" + y + "' AND z='" + z + "' AND cx ='" + cx + "' AND cz='" + cz + "'";
                state = conn.createStatement();
                state.executeQuery(select);
                rs = state.getResultSet();
            }
            else {
                final String select = "SELECT bid,playername FROM worlds JOIN " + world + "_chunks ON worlds.id = " + world + "_chunks.mainid " + "INNER JOIN " + world + "_blocks ON " + world + "_chunks.cid = " + world + "_blocks.cid " + "INNER JOIN players ON " + world + "_blocks.player = players.pid " + "WHERE worldname='" + world + "' AND x='" + x + "' AND y='" + y + "' AND z='" + z + "' AND cx ='" + cx + "' AND cz='" + cz + "'";
                state = conn.createStatement();
                state.executeQuery(select);
                rs = state.getResultSet();
            }
            while (rs.next()) {
                who = String.valueOf(who) + rs.getInt("bid") + "," + rs.getString("playername") + ",";
            }
            try {
                who = who.substring(0, who.length() - 1);
            }
            catch (StringIndexOutOfBoundsException ex) {}
            catch (NullPointerException ex2) {}
            state.close();
            rs.close();
            conn.close();
        }
        catch (SQLException e2) {
            try {
                conn.close();
            }
            catch (SQLException e1) {
                e1.printStackTrace();
            }
            e2.printStackTrace();
        }
        return who;
    }
    
    public synchronized ResultSet getChunkBlocks(final String world) {
        final Connection conn = this.watchblock.getConnection();
        ResultSet rs = null;
        try {
            final Statement state = conn.createStatement();
            state.executeQuery("select id,worldname from `" + world + "`");
            rs = state.getResultSet();
            state.close();
            conn.close();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }
    
    public synchronized void removeBlockQueueToDB() {
        if (SQLQueue.removequeue.size() > 0) {
            final Connection conn = this.watchblock.getConnection();
            final int size = SQLQueue.removequeue.size();
            try {
                SQLQueue.removelocked = true;
                if (SQLQueue.removequeue.size() > 2000) {
                    System.out.println("[WatchBlock] SQL Remove Block Queuesize: " + SQLQueue.removequeue.size());
                }
                for (int i = 0; i < size; ++i) {
                    try {
                        final String delete = SQLQueue.removequeue.pollFirst();
                        final String[] temp = delete.split(",");
                        conn.setAutoCommit(false);
                        final Statement state = conn.createStatement();
                        state.execute("delete from `" + temp[1] + "_blocks` WHERE bid='" + temp[0] + "'");
                        conn.commit();
                        state.close();
                    }
                    catch (NullPointerException ex) {}
                }
                if (size > 2000) {
                    System.out.println("The Removequeue is back to normal: 0.");
                }
                SQLQueue.removelocked = false;
                conn.close();
            }
            catch (SQLException e2) {
                try {
                    conn.close();
                }
                catch (SQLException e1) {
                    e1.printStackTrace();
                }
                e2.printStackTrace();
            }
        }
        SQLQueue.removelocked = false;
    }
    
    public synchronized void removeBlock(final String bid, final String world) {
        SQLQueue.removequeue.add(String.valueOf(bid) + "," + world);
    }
    
    public synchronized void addBlock(final String world, final String player, final int x, final int y, final int z, final int cx, final int cz) {
        if (world != null && player != null) {
            SQLQueue.addqueue.add(String.valueOf(world) + "," + player + "," + x + "," + y + "," + z + "," + cx + "," + cz);
        }
    }
}
