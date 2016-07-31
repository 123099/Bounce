package us.battleaxe.bae.permissions;

public class PlayerGroupDescription {
	
	private final int id;
	private final String name;
	private final String prefix;
	private final String suffix;
	private final int parent;
	
	public PlayerGroupDescription(int id, String name, int parent, String prefix, String suffix) {
		this.id = id;
		this.name = name;
		this.parent = parent;
		this.prefix = prefix;
		this.suffix = suffix;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getParent() {
		return this.parent;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public String getSuffix() {
		return this.suffix;
	}
}