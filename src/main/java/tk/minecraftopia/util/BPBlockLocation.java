package tk.minecraftopia.util;

import java.io.*;
import org.bukkit.block.*;

public class BPBlockLocation implements Serializable
{
    private static final long serialVersionUID = 1L;
    private int x;
    private int y;
    private int z;
    
    public BPBlockLocation(final Block b) {
        this.x = b.getX();
        this.y = b.getY();
        this.z = b.getZ();
    }
    
    public BPBlockLocation(final int x, final int y, final int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public boolean equals(final Object obj) {
        final BPBlockLocation o = (BPBlockLocation)obj;
        return o.x == this.x && o.y == this.y && o.z == this.z;
    }
    
    @Override
    public int hashCode() {
        return this.x + this.y + this.z;
    }
    
    @Override
    public String toString() {
        return "(" + this.x + "," + this.y + "," + this.z + ")";
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getZ() {
        return this.z;
    }
}
