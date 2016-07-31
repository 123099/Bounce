package us.battleaxe.bae;

import me.dommi2212.BungeeBridge.BungeeBridgeC;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.Hologram;
import us.battleaxe.bae.api.OfflinePlayerAccount;
import us.battleaxe.bae.api.OnlinePlayerAccount;
import us.battleaxe.bae.api.PermissionManager;
import us.battleaxe.bae.api.StaffManager;
import us.battleaxe.bae.api.WarpManager;
import us.battleaxe.bae.api.WorldSeparator;
import us.battleaxe.bae.api.visibility.VisibilityManager;
import us.battleaxe.bae.permissions.ImplementedPermissionManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

public class ImplementedAPI implements API {
	
	private static ImplementedAPI instance;
	
	private final BAEssentials baeInstance;
	private final MySQLClient mySQLClient;
	private final WorldSeparator worldSeparator;
	private final GlobalChatFormatter chatFormatter;
	private final PingRetriever retriever;
	private final TablistBroadcaster broadcaster;
	private final ImplementedPermissionManager permManager;
	private final WarpManager warpManager;
	private final AutoRestarter autoRestarter;
	private final ScoreboardHandler scoreboardHandler;
	private final StaffManager staffManager;
	private final AutoBroadcaster autoBroadcaster;
	private final VisibilityManager visibilityManager;
	private final Map<UUID, OfflinePlayerAccount> uuidAccounts;
	private final Map<Integer, OfflinePlayerAccount> idAccounts;
	private final Map<String, List<Hologram>> hologramsByPlugins;
	private final Map<String, List<Hologram>> hologramsByWorlds;

	private final FileConfiguration config;
	private final FileConfiguration messagesConfig;
	private final FileConfiguration filterConfig;
	private final FileConfiguration staffBookConfig;
	private final FileConfiguration broadcasterConfig;

	private String tabHeader;
	private String tabFooter;
	
	private boolean isDisposed;
	
	private ImplementedAPI(BAEssentials baeInstance) throws Exception {
		this.baeInstance = baeInstance;
		this.config = this.baeInstance.getConfig();

		this.visibilityManager = new VisibilityManager(baeInstance);

		this.messagesConfig = loadFileConfig("messages.yml", "Messages");
		if(this.messagesConfig.get("general.no-permission") == null) throw new IllegalArgumentException("Invalid messages.yml!");
		this.filterConfig = loadFileConfig("filter.yml", "ChatFilter");
		if(this.filterConfig.get("filtered-words") == null) throw new IllegalArgumentException("Invalid filter.yml!");
		this.staffBookConfig = loadFileConfig("book.yml", "Staffbook");
		if(this.staffBookConfig.get("author") == null) throw new IllegalArgumentException("Invalid book.yml!");
		this.broadcasterConfig = loadFileConfig("broadcaster.yml", "AutoBroadcaster");
		if(this.broadcasterConfig.get("enabled") == null) throw new IllegalArgumentException("Invalid broadcaster.yml!");
		
		ConfigurationSection mysqlSection = this.config.getConfigurationSection("mysql");
		this.mySQLClient = new MySQLClient(this.baeInstance, mysqlSection.getString("host"), mysqlSection.getInt("port"), mysqlSection.getString("user"), mysqlSection.getString("password"), mysqlSection.getString("database"));
		this.mySQLClient.openConnection();
		
		ConfigurationSection wsSection = this.config.getConfigurationSection("world-separator");
		this.worldSeparator = new WorldSeparator(this, wsSection.getBoolean("tab"), wsSection.getBoolean("chat"), wsSection.getBoolean("deaths"));
		
		ConfigurationSection chatSection = this.config.getConfigurationSection("chat");
		this.chatFormatter = new GlobalChatFormatter(this, chatSection, this.filterConfig.getStringList("filtered-words"), this.filterConfig.getStringList("replacement-words"));
		
		this.retriever = new PingRetriever();
		this.broadcaster = new TablistBroadcaster();
		this.permManager = new ImplementedPermissionManager(this.baeInstance, this);
		this.warpManager = new WarpManager(this.baeInstance, this, new File(this.baeInstance.getDataFolder(), "warps.yml"), loadFileConfig("warps.yml", "Warps"));
		this.autoRestarter = new AutoRestarter(this.baeInstance, this);
		this.scoreboardHandler = new ScoreboardHandler();
		this.staffManager = new StaffManager(this, this.staffBookConfig);
		this.autoBroadcaster = new AutoBroadcaster(this.baeInstance, this.broadcasterConfig);

		this.uuidAccounts = Collections.synchronizedMap(new HashMap<UUID, OfflinePlayerAccount>());
		this.idAccounts = Collections.synchronizedMap(new HashMap<Integer, OfflinePlayerAccount>());
		this.hologramsByPlugins = Collections.synchronizedMap(new HashMap<String, List<Hologram>>());
		this.hologramsByWorlds = Collections.synchronizedMap(new HashMap<String, List<Hologram>>());

		this.tabHeader = ChatColor.translateAlternateColorCodes('&', this.config.getString("tab-header"));
		this.tabFooter = ChatColor.translateAlternateColorCodes('&', this.config.getString("tab-footer"));
		
		if(this.config.getBoolean("auto-restart.enabled")) this.autoRestarter.enable();
		if(this.config.getBoolean("tab-list-handling")) this.scoreboardHandler.enable();
		if(this.broadcasterConfig.getBoolean("enabled")) this.autoBroadcaster.enable();
	}

	@Override
	public MySQLClient getMySQLClient() {
		return this.mySQLClient;
	}

	@Override
	public WorldSeparator getWorldSeparator() {
		return this.worldSeparator;
	}

	@Override
	public GlobalChatFormatter getChatFormatter() {
		return this.chatFormatter;
	}

	@Override
	public PermissionManager getPermissionManager() {
		return this.permManager;
	}

	@Override
	public WarpManager getWarpManager() {
		return this.warpManager;
	}
	
	@Override
	public AutoRestarter getAutoRestarter() {
		return this.autoRestarter;
	}

	@Override
	public AutoBroadcaster getAutoBroadcaster() {
		return this.autoBroadcaster;
	}
	
	@Override
	public ScoreboardHandler getScoreboardHandler() {
		return this.scoreboardHandler;
	}
	
	@Override
	public StaffManager getStaffManager() {
		return this.staffManager;
	}

	@Override
	public VisibilityManager getVisibilityManager() {
		return visibilityManager;
	}

	@Override
	public String getMessage(String path) {
		return getMessage(path, false, new String[0]);
	}

	@Override
	public String getMessage(String path, String... replacements) {
		return getMessage(path, false, replacements);
	}

	@Override
	public String getMessage(String path, boolean ignoreColor, String[] replacements) {
		String msg = this.messagesConfig.getString(path);
		if(msg != null) {
			if(ignoreColor) {
				msg = ChatColor.translateAlternateColorCodes('&', msg);
			}
			for(int i = 0; i < replacements.length; i++) {
				String replaced = replacements[i] != null ? replacements[i] : "null";
				msg = msg.replace("{" + i + "}", replaced);
			}
			return ignoreColor ? msg : ChatColor.translateAlternateColorCodes('&', msg);
		}
		this.baeInstance.getLogger().warning("Couldn't find message " + path);
		return null;
	}

	@Override
	public TextComponent getComponentMessage(String path) {
		return getComponentMessage(path, new String[0]);
	}

	@Override
	public TextComponent getComponentMessage(String path, String[] replacements) {
		String msg = this.messagesConfig.getString(path);
		if(msg != null) {
			for(int i = 0; i < replacements.length; i++) {
				String replaced = replacements[i] != null ? replacements[i] : "null";
				msg = msg.replace("{" + i + "}", replaced);
			}
			try {
				return new TextComponent(ComponentSerializer.parse(msg));
			} catch (Exception e) {
				this.baeInstance.getLogger().log(Level.SEVERE, "Couldn't read component-message ", e);
				return null;
			}
		}
		this.baeInstance.getLogger().warning("Couldn't find component-message " + path);
		return null;
	}

	@Override
	public long getTeleportationRequestTimeout() {
		return this.baeInstance.getConfig().getLong("teleportation-request-timeout");
	}

	@Override
	public OnlinePlayerAccount getOnlineAccount(UUID uuid) {
		return this.uuidAccounts.get(uuid).getOnlineAccount();
	}
	
	@Override
	public OnlinePlayerAccount getOnlineAccount(String name) {
		return getOnlineAccount(Bukkit.getPlayer(name));
	}
	
	@Override
	public OnlinePlayerAccount getOnlineAccount(int id) {
		return this.idAccounts.get((Integer) id).getOnlineAccount();
	}

	@Override
	public OnlinePlayerAccount getOnlineAccount(Player player) {
		return getOnlineAccount(player.getUniqueId());
	}

	@Override
	public OfflinePlayerAccount getOfflineAccount(UUID uuid) {
		return this.uuidAccounts.get(uuid);
	}
	
	@Override
	public OfflinePlayerAccount getOfflineAccount(int id) {
		return this.idAccounts.get((Integer) id);
	}

	@Override
	public OfflinePlayerAccount loadAccount(UUID uuid) throws Exception {
		PreparedStatement loadStatement = this.mySQLClient.prepareStatement("SELECT * FROM `players` WHERE `uuid` = ?;");
		loadStatement.setBytes(1, Util.toByteArray(uuid));
		
		ResultSet rs = this.mySQLClient.executeQuery(loadStatement);
		ImplementedAccount account;
		
		if(rs.next()) {
			account = new ImplementedAccount(this.baeInstance, this, uuid, rs.getInt(2), rs.getInt(3), rs.getInt(4));
			this.uuidAccounts.put(uuid, account);
			this.idAccounts.put((Integer) rs.getInt(2), account);
			return (OfflinePlayerAccount) account;
		}
		return null;
	}

	@Override
	public void createAccount(UUID uuid) throws Exception {
		PreparedStatement createStatement = this.mySQLClient.prepareStatement("INSERT INTO `players` (`uuid`, `gold`, `group`) VALUES (?, 0, 1);", true);
		createStatement.setBytes(1, Util.toByteArray(uuid));
		this.mySQLClient.executeUpdate(createStatement);
		
		ResultSet id = createStatement.getGeneratedKeys();
		id.next();
		ImplementedAccount account = new ImplementedAccount(this.baeInstance, this, uuid, id.getInt(1));
		this.uuidAccounts.put(uuid, account);
		this.idAccounts.put(id.getInt(1), account);
	}

	@Override
	public String getDefaultTabHeader() {
		return this.tabHeader;
	}

	@Override
	public String getDefaultTabFooter() {
		return this.tabFooter;
	}
	
	@Override
	public void setDefaultTabHeader(String header) {
		this.tabHeader = header;
	}

	@Override
	public void setDefaultTabFooter(String footer) {
		this.tabFooter = footer;
	}
	
	@Override
	public String getBungeename() {
		return BungeeBridgeC.getBungeename();
	}
	
	@Override
	public void sendKeepAlive() {
		BungeeBridgeC.sendKeepAlive();
	}

	@Override
	public Hologram createHologram(JavaPlugin owner, Location location, String text) {
		Hologram hologram = new StandHologram(this, owner, location, text);
		if(!this.hologramsByPlugins.containsKey(owner.getName())) {
			this.hologramsByPlugins.put(owner.getName(), new CopyOnWriteArrayList<Hologram>());
		}
		if(!this.hologramsByWorlds.containsKey(location.getWorld().getName())) {
			this.hologramsByWorlds.put(location.getWorld().getName(), new CopyOnWriteArrayList<Hologram>());
		}
		this.hologramsByPlugins.get(owner.getName()).add(hologram);
		this.hologramsByWorlds.get(location.getWorld().getName()).add(hologram);
		return hologram;
	}

	@Override
	public List<Hologram> getHolograms(JavaPlugin owner) {
		return new ArrayList<Hologram>(this.hologramsByPlugins.get(owner.getName()));
	}

	@Override
	public List<Hologram> getHolograms(World world) {
		return new ArrayList<Hologram>(this.hologramsByWorlds.get(world.getName()));
	}

	public int getPing(Player player) {
		try {
			return this.retriever.getPing(player);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException exc) {
			this.baeInstance.getLogger().log(Level.SEVERE, "Failed to retrieve " + player + "'s ping", exc);
			return -1;
		}
	}
	
	public void sendTabHeaderFooter(Player player, String header, String footer) {
		try {
			this.broadcaster.send(player, header, footer);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException exc) {
			this.baeInstance.getLogger().log(Level.SEVERE, "Failed to send header \"" + header + "\" and footer \"" + footer + "\" to player " + player.getName(), exc);
		}
	}

	public void setHologramWorld(Hologram hologram, World prior) { //TODO sync
		this.hologramsByWorlds.get(prior.getName()).remove(hologram);
		this.hologramsByWorlds.get(hologram.getLocation().getWorld().getName()).add(hologram);
	}
	
	/**
	 * Called on shutdown of the server. This method can only be invoked <b>once!</b>
	 * @throws IllegalAccessException if the method is invoked another time
	 * @throws Exception any Exception that occurs whilst disposing the ImplementedAPI-Object. 
	 */
	public void dispose() throws Exception {
		if(isDisposed) {
			throw new IllegalArgumentException("API-Instance already disposed!");
		}
		
		this.mySQLClient.closeConnection();
		this.permManager.disable();
		this.autoRestarter.disable();
		this.scoreboardHandler.disable();
		this.staffManager.clearAllPlayers();
		this.autoBroadcaster.disable();
		for(List<Hologram> holograms : this.hologramsByPlugins.values()) {
			for(Hologram hologram : holograms) hologram.setVisible(false);
		}
		this.hologramsByPlugins.clear();
		this.hologramsByWorlds.clear();
		
		this.isDisposed = true;
	}

	private FileConfiguration loadFileConfig(String fileName, String prefix) {
		this.baeInstance.getLogger().info("[" + prefix + "] Loading configuration of " + fileName);
		File file = new File(this.baeInstance.getDataFolder(), fileName);
		if(!file.exists()) {
			this.baeInstance.getLogger().info("[" + prefix + "] No " + fileName + " found! Creating...");
			this.baeInstance.saveResource(fileName, false);
		}
		
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		this.baeInstance.getLogger().info("[" + prefix + "] FileConfiguration loaded!");
		return config;
	}
	
	/**
	 * Called on boot of the server. This method can only be invoked <b>once!</b>
	 * @param baeInstance The plugin-instance of BAEssentials
	 * @return The created API-Object.
	 * @throws IllegalAccessException if the method is invoked another time
	 * @throws Exception any Exception that occurs whilst creating the ImplementedAPI-Object. 
	 */
	public static ImplementedAPI boot(BAEssentials baeInstance) throws Exception {
		if(instance != null) {
			throw new IllegalAccessException("Can't reset singleton-instance!");
		}
		
		return (instance = new ImplementedAPI(baeInstance));
	}
}