package us.battleaxe.bae.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import us.battleaxe.bae.Util;

public class Warp implements Comparable<Warp>{

	private final API api;
	private final String name;
	
	private String world;
	private double x;
	private double y;
	private double z;
	private double yaw;
	private double pitch;
	
	public Warp(API api, String name, String world, double x, double y, double z, double yaw, double pitch) {
		this.api = api;
		this.name = name;
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
		this.yaw = yaw;
		this.pitch = pitch;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getWorld() {
		return this.world;
	}

	public double getX() {
		return this.x;
	}
	
	public double getY() {
		return this.y;
	}
	
	public double getZ() {
		return this.z;
	}
	
	public double getYaw() {
		return this.yaw;
	}
	
	public double getPitch() {
		return this.pitch;
	}
	
	public String getPermission() {
		return "cmd.warp." + this.name;
	}
	
	public boolean hasPermission(Player player) {
		return player.hasPermission(getPermission());
	}
	
	public boolean isWorldValid() {
		return (Bukkit.getWorld(this.world) != null);
	}
	
	public Location getLocation() {
		World world = Bukkit.getWorld(this.world);
		if(world != null) {
			return new Location(world, this.x, this.y, this.z, (float) this.yaw, (float) this.pitch);
		}
		return null;
	}

	public boolean teleport(Player player) {
		return teleport(player, true);
	}
	
	public boolean teleport(Player player, boolean checkPermission) {
		if(checkPermission && !hasPermission(player)) {
			return false;
		}
		
		this.api.getOnlineAccount(player).updateLastLocation();
		player.teleport(getLocation());
		return true;
	}
	
	public boolean writeToFile() {
		return this.api.getWarpManager().writeToFile(this);
	}

	public void setLocation(Location loc) {
		this.world = loc.getWorld().getName();
		this.x = Util.removeDigitsAfterPoint(loc.getX(), 1);
		this.y = Util.removeDigitsAfterPoint(loc.getY(), 1);
		this.z = Util.removeDigitsAfterPoint(loc.getZ(), 1);
		this.yaw = Util.removeDigitsAfterPoint(loc.getYaw(), 1);
		this.pitch = Util.removeDigitsAfterPoint(loc.getPitch(), 1);
	}

	@Override
	public int compareTo(Warp other) {
		return this.name.compareTo(other.getName());
	}
}