package us.battleaxe.bae;

import me.dommi2212.BungeeBridge.BungeeBridgeC;
import me.dommi2212.BungeeBridge.packets.PacketConnectPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.chat.ListenerChat;
import us.battleaxe.bae.commands.CmdExecutor;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * The Main-Class for accessing <b>B</b>attle<b>a</b>xe<b>E</b>ssentials and its API.<br>
 * A singleton API-Object can be obtained by using {@link #getAPI()}
 */
public class BAEssentials extends JavaPlugin {
	
	private static volatile boolean isAccessible;
	private static ImplementedAPI implementedAPI;
	private FileConfiguration config;

	/**
	 * <b>This method is not part of the API!</b>
	 *
	 * @see JavaPlugin#onEnable()
	 */
	@Override
	public void onEnable() {
		PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new ListenerLogin(), this);
		
		try {
			checkDependency(pm, "BungeeBridgeC");
			if(BungeeBridgeC.getBungeename().length() == 0) throw new UnknownDependencyException("BungeeBridgeC");
			if(BungeeBridgeC.getBungeename().length() > 16) throw new IllegalArgumentException("The servername defined in BungeeCord is limited to 16 letters!");
			
			this.config = loadConfig();
			implementedAPI = ImplementedAPI.boot(this);
			
			pm.registerEvents(new ListenerAsyncLogin(this, implementedAPI), this);
			pm.registerEvents(new ListenerChat(implementedAPI), this);
			pm.registerEvents(new ListenerDeath(this, implementedAPI), this);
			pm.registerEvents(new ListenerJoin(this, implementedAPI), this);
			pm.registerEvents(new ListenerCommand(), this);
			pm.registerEvents(new ListenerWeather(this.config.getBoolean("no-weather")), this);
			pm.registerEvents(new ListenerWorldSeparator(implementedAPI.getWorldSeparator()), this);
			pm.registerEvents(new ListenerStaffActions(implementedAPI), this);
			pm.registerEvents(new ListenerQuit(), this);

//			pm.registerEvents(new ListenerTest(implementedAPI), this);
			
			CmdExecutor.registerAllCommands(this, implementedAPI);

			isAccessible = true;
		} catch (Exception e) { //BAE fails to boot --> Make the server inaccessible and restart after 120s
			getLogger().log(Level.SEVERE, "Failed to enable BAE! Making server inaccessible and restarting in 120s!", e);
			Bukkit.getScheduler().runTaskLater(this, new Runnable() {		
				@Override
				public void run() {
					getLogger().info("Rebooting...");
					Bukkit.shutdown();
				}
			}, 120 * 20L);
		}
	}

	/**
	 * <b>This method is not part of the API!</b>
	 * @see JavaPlugin#onDisable()
	 */
	@Override
	public void onDisable() {
		if(this.config != null) {
			for(Player online : Bukkit.getOnlinePlayers()) {
				PacketConnectPlayer packet = new PacketConnectPlayer(online.getUniqueId(), this.config.getString("fallback-server"));
				packet.send();
			}
		}
		
		try {
			//I didn't find a better solution...
			Thread.sleep(2000L);
		} catch (InterruptedException exc) {
			getLogger().log(Level.SEVERE, "Error whilst waiting for players to leave server", exc);
		}
		if(implementedAPI != null) {
			try {
				this.getLogger().info("Disposing API and shutting down...");
				implementedAPI.dispose();
			} catch (IllegalAccessException e) {
				this.getLogger().log(Level.SEVERE, "API already disposed!", e);
			} catch (Exception e) {
				this.getLogger().log(Level.SEVERE, "Error whilst disposing API!", e);
			}
		} else {
			this.getLogger().warning("API-Instance not set" + (isAccessible ? "! Did something go wrong on boot?" : " cause BAE failed to boot correctly!")); 
		}
	}

	/**
	 * <b>This method is not part of the API!</b>
	 * @see JavaPlugin#getConfig()
	 */
	@Override
	public FileConfiguration getConfig() {
		return this.config;
	}

	/**
	 * Gets the Accessibility-Flag, which indicates whether this server is operational and can be joined.<br>
	 * A value of <b>false</b> indicates that there is a severe problem with the server and that it can't handle any players at the moment.
	 * If a server fails to boot it will be put into this state and stop/reboot after 120 seconds.<br>
	 * A value of <b>true</b> indicates that there is no severe problem from BAEssentials perspective.
	 *
	 * @see #setAccessible(boolean)
	 * @return Whether the server is accessible or not.
	 */
	public static boolean isAccessible() {
		return isAccessible;
	}

	/**
	 * Sets the Accessibility-Flag, which indicates whether this server is operational and can be joined.<br>
	 * A value of <b>false</b> indicates that there is a severe problem with the server and that it can't handle any players at the moment.
	 * If a server fails to boot it will be put into this state and start/reboot after 120 seconds.<br>
	 * A value of <b>true</b> indicates that there is no severe problem from BAEssentials perspective.<br>
	 * <br>
	 * Setting this to <b>true</b> will allow players to join the server again. This should <b>only</b> be done if the problem has been successfully resolved.<br>
	 * Setting this to <b>false</b> will kick all players and stop new players from joining, but it won't stop/reboot the server after 120 seconds in this state.
	 *
	 * @see #isAccessible()
	 * @param accessible The new value of the Accessibility-Flag
	 */
	public static void setAccessible(boolean accessible) {
		isAccessible = accessible;
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.kickPlayer("§cThe server you were previously connected to encountered a severe problem and has been stopped! Please try again later!");
		}
	}

	/**
	 * Used to obtain a singleton API-Object.
	 *
	 * @return An API-Object, which can be used to access any API-methods.
	 */
	public static API getAPI() {
		return (API) implementedAPI;
	}
	
	private FileConfiguration loadConfig() throws IOException {
		getLogger().info("[Config] Loading configuration...");
		if(!getDataFolder().exists()) {
			getLogger().info("[Config] No pluginfolder found! Creating...");
			if(!getDataFolder().mkdir()) {
				throw new IOException("[Config] Failed to create pluginfolder!");
			}
		}
		
		File file = new File(getDataFolder(), "config.yml");		
		if(!file.exists()) {
			getLogger().info("[Config] No config.yml found! Creating...");
			saveDefaultConfig();
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		if(!config.isSet("mysql")) { //The most important key isn't set --> The configuration is damaged or invalid
			throw new IllegalArgumentException("Invalid config.yml!");
		}
		getLogger().info("[Config] Configuration loaded!");
		return config;
	}

	private void checkDependency(PluginManager pm, String dependency) {
		Plugin plugin = pm.getPlugin(dependency);
		if(plugin == null || !plugin.isEnabled()) throw new UnknownDependencyException(dependency);
	}
}