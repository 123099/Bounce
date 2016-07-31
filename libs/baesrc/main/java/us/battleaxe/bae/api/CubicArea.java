package us.battleaxe.bae.api;

import org.bukkit.Location;
import org.bukkit.block.Block;

//TODO Rename "PrismaticArea"
public class CubicArea {
	
	private final Block min;
	private final Block max;
	
	private CubicArea(Block min, Block max) {
		this.min = min;
		this.max = max;
	}
	
	public Block getMin() {
		return this.min;
	}
	
	public Block getMax() {
		return this.max;
	}
	
	public boolean inArea(Location loc) {
		return inArea(loc.getBlock());
	}
	
	public boolean inArea(Block block) {
		if(block.getX() < this.min.getX() || block.getY() < this.min.getY() || block.getZ() < this.min.getZ()) {
			return false;
		}
		if(block.getX() > this.max.getX() || block.getY() > this.max.getY() || block.getZ() > this.max.getZ()) {
			return false;
		}
		return true;
	}
	
	public static CubicArea parse(Location pos1, Location pos2) {
		return parse(pos1.getBlock(), pos2.getBlock());
	}
	
	public static CubicArea parse(Block pos1, Block pos2) {
		if(!pos1.getWorld().equals(pos2.getWorld())) throw new IllegalArgumentException("Positions have to be in the same world!");
		
		Block min = new Location(pos1.getWorld(),
				Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ())).getBlock();
		Block max = new Location(pos1.getWorld(),
				Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ())).getBlock();
		return new CubicArea(min, max);
	}
}