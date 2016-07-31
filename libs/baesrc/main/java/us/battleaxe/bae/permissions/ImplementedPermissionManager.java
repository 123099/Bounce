package us.battleaxe.bae.permissions;

import me.dommi2212.BungeeBridge.BungeeBridgeC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import us.battleaxe.bae.BAEssentials;
import us.battleaxe.bae.MySQLClient;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.OfflinePlayerAccount;
import us.battleaxe.bae.api.OnlinePlayerAccount;
import us.battleaxe.bae.api.PermissionManager;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class ImplementedPermissionManager implements PermissionManager {

	private final BAEssentials instance;
	private final API api;
	private final Map<Integer, PlayerGroup> groups;
	private final Map<Integer, PlayerGroupDescription> queue;
	private final PermissibleInjector injector;
	private final int permSyncInterval;
	private final AtomicLong lastSync;
	
	private BukkitTask syncTask;
	private volatile boolean warned;
	
	public ImplementedPermissionManager(BAEssentials instance, API api) throws Exception {
		this.instance = instance;
		this.api = api;
		this.groups = Collections.synchronizedMap(new HashMap<Integer, PlayerGroup>());
		this.queue = new HashMap<Integer, PlayerGroupDescription>();
		this.injector = new PermissibleInjector();
		this.permSyncInterval = this.instance.getConfig().getInt("permission-synchronization-interval");

		loadGroups();
		for(PlayerGroup group : this.groups.values()) {
			loadGroupPerms(group);
		}
		
		this.lastSync = new AtomicLong(System.currentTimeMillis());
		enablePermSync();
	}
	
	@Override
	public PlayerGroup getGroup(int id) {
		return this.groups.get((Integer) id);
	}
	
	@Override
	public PlayerGroup getGroup(String name) {
		if(name == null) {
			throw new IllegalArgumentException("Groupname can't be null!");
		}
		synchronized (this.groups) {
			for(PlayerGroup group : this.groups.values()) {
				if(group.getName().equalsIgnoreCase(name)) {
					return group;
				}
			}
		}
		return null;
	}
	
	@Override
	public PlayerGroup parseGroup(String input) {
		try {
			return getGroup(Integer.valueOf(input));
		} catch (NumberFormatException exc) {
			return getGroup(input);
		}
	}
	
	@Override
	public List<PlayerGroup> getGroups() {
		return new ArrayList<PlayerGroup>(this.groups.values());
	}
	
	@Override
	public PermissibleInjector getInjector() {
		return this.injector;
	}

	@Override
	public void registerGroup(PlayerGroup group) {
		this.groups.put(group.getId(), group);
	}

	@Override
	public void unregisterGroup(PlayerGroup group) {
		for(PlayerGroup other : getGroups()) {
			if(group.equals(other)) continue;
			other.removeFromInheritanceChain(group);
		}
		
		for(Player online : Bukkit.getOnlinePlayers()) {
			OnlinePlayerAccount account = api.getOnlineAccount(online);
			if(group.equals(account.getGroup())) {
				account.setLocalGroupAndApply(this.groups.get((Integer) 1));
			}
		}
		
		this.groups.remove(group.getId());
	}
	
	public void disable() {
		this.syncTask.cancel();
	}
	
	private void enablePermSync() {
		this.syncTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this.instance, new Runnable() {
			@Override
			public void run() {
				PreparedStatement statement = null;
				try {
					statement = api.getMySQLClient().prepareStatement("SELECT * FROM `permission_updates` WHERE `issued` > ? AND `source` NOT LIKE ? ORDER BY `issued`;");
					statement.setLong(1, lastSync.get());
					statement.setString(2, BungeeBridgeC.getBungeename());
				} catch (SQLException exc) {
					String message = api.getMessage("general.mysql-notify-check-failure", api.getBungeename(), new SimpleDateFormat("KK:mm:ss a YYYY-MM-dd").format(new Date()));
					for(Player online : Bukkit.getOnlinePlayers()) {
						if(online.hasPermission("bae.error-notification")) online.sendMessage(message);
					}
					Bukkit.getConsoleSender().sendMessage(message);
					instance.getLogger().log(Level.SEVERE, "Failed to prepare check for notifications", exc);
					reportException();
					return;
				}
				
				ResultSet rs;
				try {
					rs = api.getMySQLClient().executeQuery(statement);
					while (rs.next()) {
						int updated = rs.getInt(1);
						UpdateAction action = UpdateAction.valueOf(rs.getByte(2));
						
						if(action.isPlayerAction()) {
							OfflinePlayerAccount offline = api.getOfflineAccount(updated);
							if(offline == null || !offline.isLoggedIn()) {
								continue;
							}
							OnlinePlayerAccount account = offline.getOnlineAccount();
							
							switch (action) {
							case PLAYER_GROUP_UPDATE: {
								if(!account.loadGroupAndApply()) break;
								break;
							}
							case PLAYER_PERMISSION_UPDATE: {
								if(!account.loadPermissions()) break;
								//Make sure, that the player didn't leave
								if(offline.isLoggedIn()) {
									account.clearPermissions(false);
									account.applyPermissions();
								}
								break;
							}
							default:
								break;
							}
						} else {
							PlayerGroup group = groups.get((Integer) updated);
							
							switch (action) {
							case GROUP_CREATION: {
								PlayerGroupDescription desc = loadGroupData(updated);
								PlayerGroup created = new PlayerGroup(api, updated, desc.getName(), groups.get((Integer) desc.getParent()), desc.getPrefix(), desc.getSuffix());
								registerGroup(created);
								break;
							}
							case GROUP_DELETION: {
								unregisterGroup(group);
								break;
							}
							case GROUP_PARENT_UPDATE: {
								group.setParent(groups.get((Integer) loadGroupData(updated).getParent()));
								break;
							}
							case GROUP_PERMISSION_UPDATE: {
								loadGroupPerms(group);
								for(Player online : Bukkit.getOnlinePlayers()) {
									OnlinePlayerAccount account = api.getOnlineAccount(online);
									account.clearPermissions(true);
									account.applyPermissions();
								}
								break;
							}
							case GROUP_PREFIX_SUFFIX_UPDATE: {
								PlayerGroupDescription desc = loadGroupData(updated);
								group.setPrefix(desc.getPrefix());
								group.setSuffix(desc.getSuffix());
								break;
							}
							default:
								break;
							}
						}
					}
				} catch (SQLException exc) {
					String message = api.getMessage("general.mysql-notify-process-failure", api.getBungeename(), new SimpleDateFormat("KK:mm:ss a YYYY-MM-dd").format(new Date()));
					for(Player online : Bukkit.getOnlinePlayers()) {
						if(online.hasPermission("bae.error-notification")) online.sendMessage(message);
					}
					Bukkit.getConsoleSender().sendMessage(message);
					instance.getLogger().log(Level.SEVERE, "Failed to process notifications", exc);
					reportException();
					return;
				}
				lastSync.set(System.currentTimeMillis());
			}
		}, 20 * this.permSyncInterval, 20 * this.permSyncInterval);
	}

	private void loadGroupPerms(PlayerGroup group) throws SQLException {
		group.clearPermissions();
		PreparedStatement statement = this.api.getMySQLClient().prepareStatement("SELECT `action`,`permission`,`scope` FROM `permissions` WHERE `holder` = ? AND `isplayer` = b'0';");
		statement.setInt(1, group.getId());
		
		ResultSet rs = this.api.getMySQLClient().executeQuery(statement);
		while (rs.next()) {		
			if(!rs.getString(3).equals("*")) {
				if(!api.getBungeename().matches(rs.getString(3))) {
					continue;
				}
			}
			if(rs.getString(2).equals("*")) {
				if(!rs.getBoolean(1)) {
					throw new IllegalArgumentException("Can't add inverted Wildcard-Permission to group " + group.getId());
				}
				group.setWildcard(true);
			} else {
				group.addPermission(rs.getString(2), rs.getBoolean(1));
			}
		}
	}
	
	private void loadGroups() throws SQLException, IllegalArgumentException{
		MySQLClient client = this.api.getMySQLClient();
		ResultSet rs = client.executeQuery(client.prepareStatement("SELECT * FROM `groups`;"));
		
		while (rs.next()) {
			PlayerGroupDescription description = new PlayerGroupDescription(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5));
			if(description.getParent() == 0) {
				this.groups.put(description.getId(), new PlayerGroup(this.api, description));
			} else {
				this.queue.put(description.getId(), description);
			}
		}
		
		Iterator<PlayerGroupDescription> iterator = this.queue.values().iterator();
		while (iterator.hasNext()) {
			PlayerGroupDescription desc = iterator.next();
			if(!groups.containsKey((Integer) desc.getId())) {
				this.groups.put((Integer) desc.getId(), loadGroup(desc, desc.getId()));
			}
		}
	}
	
	private PlayerGroup loadGroup(PlayerGroupDescription desc, int caller) {
		if(caller == desc.getParent()) {
			throw new IllegalArgumentException("Group " + desc.getName() + " (Id: " + desc.getId() + ") stands at the end of its own inheritance chain!");
		}
		
		if(this.groups.containsKey((Integer) desc.getParent())) {
			return new PlayerGroup(this.api, desc.getId(), desc.getName(), this.groups.get((Integer) desc.getParent()), desc.getPrefix(), desc.getSuffix());
		} else if(this.queue.containsKey((Integer) desc.getParent())) {
			PlayerGroupDescription parentDesc = this.queue.get((Integer) desc.getParent());
			PlayerGroup parent = loadGroup(parentDesc, caller);
			this.groups.put((Integer) parent.getId(), parent);
			return new PlayerGroup(this.api, desc.getId(), desc.getName(), parent, desc.getPrefix(), desc.getSuffix());
		}
		
		throw new IllegalArgumentException("Group " + desc.getName() + " (Id: " + desc.getId() + ") has been declared with unknown parent-group-id " + desc.getParent());	
	}
	
	private PlayerGroupDescription loadGroupData(int id) {
		try {
			PreparedStatement statement = this.api.getMySQLClient().prepareStatement("SELECT * FROM `groups` WHERE `id` = ?;");
			statement.setInt(1, id);
			ResultSet rs = this.api.getMySQLClient().executeQuery(statement);
			rs.next();
			
			return new PlayerGroupDescription(id, rs.getString(2), rs.getInt(3), rs.getString(4), rs.getString(5));
		} catch (SQLException exc) {
			this.instance.getLogger().log(Level.SEVERE, "Failed to load groupdata for group " + id, exc);
			return null;
		}
	}
	
	private void reportException() {
		if(this.warned) {
			Bukkit.shutdown();
		} else {
			this.warned = true;
		}
	}
	
	public static enum UpdateAction {
	
		GROUP_PERMISSION_UPDATE((byte) -1),
		GROUP_DELETION((byte) -2),
		GROUP_CREATION((byte) -3),
		GROUP_PREFIX_SUFFIX_UPDATE((byte) -4),
		GROUP_PARENT_UPDATE((byte) -5),
		
		PLAYER_PERMISSION_UPDATE((byte) 1),
		PLAYER_GROUP_UPDATE((byte) 2);
		
		private static final HashMap<Byte, UpdateAction> actions = new HashMap<Byte, UpdateAction>();
		private byte id;
		
		private UpdateAction(byte id) {
			this.id = id;
		}

		public byte getId() {
			return id;
		}
		
		public boolean isPlayerAction() {
			return Byte.compare(this.id, (byte) 0) > 0;
		}
		
		public static UpdateAction valueOf(byte id) {
			return actions.get((Byte) id);
		}
		
		static {
			for(UpdateAction action : values()) {
				actions.put((Byte) action.getId(), action);
			}
		}
	}
}