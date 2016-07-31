package us.battleaxe.bae.api;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.permissions.Permission;
import us.battleaxe.bae.BAEssentials;
import us.battleaxe.bae.Util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class WarpManager {

	private final BAEssentials baeInstance;
	private final API api;
	
	private final File file;
	private final FileConfiguration config;
	private final Map<String, Warp> warps;
	private final Permission warpWildcard;
	
	public WarpManager(BAEssentials baeInstance, API api, File file, FileConfiguration config) {
		this.baeInstance = baeInstance;
		this.api = api;
		this.file = file;
		this.config = config;
		this.warps = new HashMap<String, Warp>();
		this.warpWildcard = new Permission("cmd.warp.*");

		Bukkit.getPluginManager().addPermission(this.warpWildcard);
		addPermission("cmd.warp.list");
		addPermission("cmd.warp");
		Bukkit.getPluginManager().getPermission("cmd.warp").addParent(Bukkit.getPluginManager().getPermission("cmd.warp.list"), true);

		for(String key : this.config.getKeys(false)) {
			try {
				Warp warp = new Warp(this.api, key,
						this.config.getString(key + ".world"),
						this.config.getDouble(key + ".x"),
						this.config.getDouble(key + ".y"),
						this.config.getDouble(key + ".z"),
						this.config.getDouble(key + ".yaw"),
						this.config.getDouble(key + ".pitch"));
				this.warps.put(key.toLowerCase(), warp);
				addPermission(warp.getPermission());
			} catch (Exception exc) {
				this.baeInstance.getLogger().log(Level.SEVERE, "Failed to load warp: " + key, exc);
			}
		}
	}
	
	public List<Warp> getWarps() {
		return new ArrayList<Warp>(this.warps.values());
	}
	
	public boolean exists(String name) {
		return this.warps.containsKey(name.toLowerCase());
	}
	
	public Warp getWarp(String name) {
		return this.warps.get(name.toLowerCase());
	}

	public boolean writeToFile(Warp warp) {
		this.config.set(warp.getName() + ".world", warp.getWorld());
		this.config.set(warp.getName() + ".x", warp.getX());
		this.config.set(warp.getName() + ".y", warp.getY());
		this.config.set(warp.getName() + ".z", warp.getZ());
		this.config.set(warp.getName() + ".yaw", (double) warp.getYaw());
		this.config.set(warp.getName() + ".pitch", (double) warp.getPitch());
		
		return save();
	}

	public Warp registerWarp(String name, Location loc) {
		if(this.warps.containsKey(name.toLowerCase())) {
			throw new IllegalArgumentException("A warp with that name already exists!");
		}
		
		Warp warp = new Warp(this.api, name,
				loc.getWorld().getName(),
				Util.removeDigitsAfterPoint(loc.getX(), 1),
				Util.removeDigitsAfterPoint(loc.getY(), 1),
				Util.removeDigitsAfterPoint(loc.getZ(), 1),
				Util.removeDigitsAfterPoint(loc.getYaw(), 1),
				Util.removeDigitsAfterPoint(loc.getPitch(), 1));
		this.warps.put(name.toLowerCase(), warp);
		
		Permission permission = new Permission(warp.getPermission());
		Bukkit.getPluginManager().addPermission(permission);
		permission.addParent(this.warpWildcard, true);
		
		return warp;
	}
	
	public void unregisterWarp(String name) {
		Warp warp = this.warps.remove(name.toLowerCase());
		Bukkit.getPluginManager().removePermission(warp.getPermission());
		this.config.set(warp.getName(), null);
	}
	
	public boolean save() {
		try {
			this.config.save(this.file);
			return true;
		} catch (IOException exc) {
			this.baeInstance.getLogger().log(Level.SEVERE, "Failed to save warps to file", exc);
			return false;
		}
	}

	private void addPermission(String name) {
		Permission permission = Bukkit.getPluginManager().getPermission(name);
		if(permission == null) {
			permission = new Permission(name);
			Bukkit.getPluginManager().addPermission(permission);
		}
		permission.addParent(this.warpWildcard, true);
	}
}