package us.battleaxe.bounce;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import us.battleaxe.bounce.bukkiteventlisteners.BlockBounceEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.BlockBreakEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.PlayerChangeWorldEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.PlayerDisableStaffModeEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.PlayerHungerEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.PlayerJoinLeaveServerEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.PlayerPickupEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.PlayerPvPEventListener;
import us.battleaxe.bounce.bukkiteventlisteners.SignInteractEventListener;
import us.battleaxe.bounce.commands.CommandNames;
import us.battleaxe.bounce.commands.PlayerCommandQuit;
import us.battleaxe.bounce.commands.bouncecommands.PlayerCommandBounce;
import us.battleaxe.bounce.managers.ConfigManager;
import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.Constants;

public class Bounce extends JavaPlugin {

	private Map<World, GameManager> gameManagers;
	
	private ConfigManager configManager;
	
	@Override
	public void onEnable() {
		gameManagers = new HashMap<>();
		configManager = new ConfigManager(this);
		
		loadGameSettings();
		reloadGames();
		registerEvents();
		registerCommands();
	}
	
	@Override
	public void onDisable() {
		gameManagers.clear();
	}
	
	public void addGameManager(GameManager gameManager) {		
		gameManagers.put(gameManager.getGameWorld(), gameManager);
	}
	
	public void removeGameManager(GameManager gameManager) {
		if(gameManager == null) return;
		
		gameManager.destroy();
		gameManagers.remove(gameManager.getGameWorld());
		
		configManager.removeGameWorld(gameManager.getGameWorld().getName());
	}
	
	public GameManager getGameManager(World world) {
		return gameManagers.get(world);
	}
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
	
	private void loadGameManagers() {
		for(String worldName : configManager.getGameWorlds())
		{
			World world = getServer().getWorld(worldName);
			if(world == null)
				world = Bukkit.createWorld(new WorldCreator(worldName));
			
			addGameManager(new GameManager(world, configManager));
		}
	}
	
	private void loadGameSettings() {
		configManager.saveDefaultConfig();
		
		Constants.BowName = configManager.getBowName();
		Constants.MaxSpeed = configManager.getMaxSpeed();
		Constants.ArrowName = configManager.getArrowName();
		Constants.SwordName = configManager.getSwordName();
		Constants.TimeForGame = configManager.getGameTime();
		Constants.MinPlayers = configManager.getMinPlayers();
		Constants.MaxPlayers = configManager.getMaxPlayers();
		Constants.DashDamage = configManager.getDashDamage();
		Constants.DashRadius = configManager.getDashRadius();
		Constants.SwordDamage = configManager.getSwordDamage();
		Constants.DashCooldown = configManager.getDashCooldown();
		Constants.DeathMessage = configManager.getDeathMessage();
		Constants.PointsPerKill = configManager.getPointsPerKill();
		Constants.DashTravelTime = configManager.getDashTravelTime();
		Constants.TimeToStartGame = configManager.getGameStartTime();
		Constants.PoisonPotionName = configManager.getPoisonPotionName();
	}
	
	private void registerEvents() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new PlayerDisableStaffModeEventListener(), this);
		pm.registerEvents(new PlayerJoinLeaveServerEventListener(), this);
		pm.registerEvents(new PlayerChangeWorldEventListener(), this);
		pm.registerEvents(new SignInteractEventListener(this), this);
		pm.registerEvents(new PlayerHungerEventListener(), this);
		pm.registerEvents(new PlayerPickupEventListener(), this);
		pm.registerEvents(new BlockBounceEventListener(), this);
		pm.registerEvents(new BlockBreakEventListener(), this);
		pm.registerEvents(new PlayerPvPEventListener(), this);
	}
	
	private void registerCommands() {
		getCommand(CommandNames.Bounce).setExecutor(new PlayerCommandBounce());
		getCommand(CommandNames.Quit).setExecutor(new PlayerCommandQuit());
	}
	
	public void reloadGames() {
		if(gameManagers != null)
			for(World world : gameManagers.keySet())
				gameManagers.get(world).destroy();
		
		gameManagers.clear();
		
		loadGameManagers();
	}
}
