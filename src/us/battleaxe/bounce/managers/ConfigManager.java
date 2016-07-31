package us.battleaxe.bounce.managers;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import us.battleaxe.bounce.Bounce;

public class ConfigManager {

	private static final String gameWorldsPath = "gameworlds";
	
	private static final String signPath = "sign";
	private static final String spawnPath = "spawn";
	
	private static final String settingsPath = "settings";
	
	private static final String minPlayersPath = "min_players";
	private static final String maxPlayersPath = "max_players";
	
	private static final String gameStartTimePath = "game_start_time";
	private static final String gameTimePath = "game_time";
	
	private static final String maxSpeedPath = "max_speed";
	
	private static final String pointsPerKillPath = "points_per_kill";
	
	private static final String swordDamagePath = "sword_damage";
	
	private static final String dashDamagePath = "dash_damage";
	private static final String dashTravelTimePath = "dash_travel_time";
	private static final String dashDistancePath = "dash_distance";
	private static final String dashCooldownPath = "dash_cooldown";
	private static final String dashRadiusPath = "dash_hit_radius";
	
	private static final String bowNamePath = "bow_name";
	private static final String arrowNamePath = "arrow_name";
	private static final String swordNamePath = "sword_name";
	private static final String poisonPotionNamePath = "poison_potion_name";
	
	private static final String deathMessagePath = "death_message";
	
	private Bounce bounce;
	private FileConfiguration config;
	
	public ConfigManager(Bounce bounce) {
		this.bounce = bounce;
		config = bounce.getConfig();
	}
	
	//-------------------------------------------------------
	//----------------Game Worlds----------------------------
	//-------------------------------------------------------
	public void setGameWorldSign(String worldName, Sign sign) {
		config.set(getPath(gameWorldsPath, worldName, signPath), SignManager.signToConfig(sign));
		bounce.saveConfig();
	}
	
	public void setGameWorldSpawn(String worldName, Location spawn) {
		config.set(getPath(gameWorldsPath, worldName, spawnPath), spawn.getBlockX() + "," + spawn.getBlockY() + "," + spawn.getBlockZ());
		bounce.saveConfig();
	}
	
	public Sign getGameWorldSign(String worldName) {
		return SignManager.configToSign(config.getString(getPath(gameWorldsPath, worldName, signPath)));
	}
	
	public Location getGameWorldSpawn(String worldName) {
		String locationString = config.getString(getPath(gameWorldsPath, worldName, spawnPath));
		
		if(locationString == null)
			return null;
		
		String[] split = locationString.split(",");
		
		if(split.length != 3) 
			return null;
		
		Location spawnLocation = new Location(Bukkit.getWorld(worldName), Integer.parseInt(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()));
		return spawnLocation;
	}
	
	public Set<String> getGameWorlds(){
		ConfigurationSection configSection = config.getConfigurationSection(gameWorldsPath);
		
		if(configSection != null)
			return configSection.getKeys(false);
		
		return new HashSet<String>();
	}
	
	public void removeGameWorld(String worldName) {
		config.set(getPath(gameWorldsPath, worldName), null);
		bounce.saveConfig();
	}
	
	//-------------------------------------------------------
	//----------------Game Settings--------------------------
	//-------------------------------------------------------
	public int getMinPlayers() {
		return config.getInt(getPath(settingsPath, minPlayersPath));
	}
	
	public int getMaxPlayers() {
		return config.getInt(getPath(settingsPath, maxPlayersPath));
	}
	
	public double getGameStartTime() {
		return config.getDouble(getPath(settingsPath, gameStartTimePath));
	}
	
	public double getGameTime() {
		return config.getInt(getPath(settingsPath, gameTimePath));
	}
	
	public double getMaxSpeed() {
		return config.getDouble(getPath(settingsPath, maxSpeedPath));
	}
	
	public int getPointsPerKill() {
		return config.getInt(getPath(settingsPath, pointsPerKillPath));
	}
	
	public double getSwordDamage() {
		return config.getDouble(getPath(settingsPath, swordDamagePath));
	}
	
	public double getDashDamage() {
		return config.getDouble(getPath(settingsPath, dashDamagePath));
	}
	
	public double getDashTravelTime() {
		return config.getDouble(getPath(settingsPath, dashTravelTimePath));
	}
	
	public double getDashDistance() {
		return config.getDouble(getPath(settingsPath, dashDistancePath));
	}
	
	public double getDashCooldown() {
		return config.getDouble(getPath(settingsPath, dashCooldownPath));
	}
	
	public double getDashRadius() {
		return config.getDouble(getPath(settingsPath, dashRadiusPath));
	}
	
	public String getBowName() {
		return config.getString(getPath(settingsPath, bowNamePath));
	}
	
	public String getArrowName() {
		return config.getString(getPath(settingsPath, arrowNamePath));
	}
	
	public String getSwordName() {
		return config.getString(getPath(settingsPath, swordNamePath));
	}
	
	public String getPoisonPotionName() {
		return config.getString(getPath(settingsPath, poisonPotionNamePath));
	}
	
	public String getDeathMessage() {
		return ChatColor.translateAlternateColorCodes('&', config.getString(getPath(settingsPath, deathMessagePath)));
	}
	
	//-------------------------------------------------------
	//----------------Config Utils---------------------------
	//-------------------------------------------------------
	public void saveDefaultConfig() {
		config.options().copyDefaults(true);
		bounce.saveConfig();
	}
	
	private String getPath(String... pathParts) {
		String path = "";
		
		for(String part : pathParts)
			path += part + ".";
		
		return path.substring(0, path.length() - 1);
	}
}
