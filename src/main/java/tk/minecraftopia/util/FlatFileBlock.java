package tk.minecraftopia.util;

public class FlatFileBlock
{
    String chunk;
    String blocklocation;
    String playername;
    
    public FlatFileBlock(final String chunk, final String blocklocation, final String playername) {
        this.blocklocation = blocklocation;
        this.chunk = chunk;
        this.playername = playername;
    }
    
    public String getChunk() {
        return this.chunk;
    }
    
    public String getPlayer() {
        return this.playername;
    }
    
    public String getLocation() {
        return this.blocklocation;
    }
}
