package us.battleaxe.bae;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import us.battleaxe.bae.api.OfflinePlayerAccount;
import us.battleaxe.bae.api.OnlinePlayerAccount;
import us.battleaxe.bae.permissions.BAEPermissible;
import us.battleaxe.bae.permissions.PlayerGroup;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ImplementedAccount implements OfflinePlayerAccount, OnlinePlayerAccount {
	
	private final BAEssentials instance;
	private final UUID uuid;
	private final ImplementedAPI api;
	private final int id;
	private final Map<String, Boolean> permissions;
	private final Map<UUID, Long> teleportRequests;
	private final List<UUID> hiddenPlayers;
	
	private volatile boolean loggedIn;
	
	private Player player;
	private AtomicInteger gold;
	private BAEPermissible permissible;
	private PermissionAttachment permissionContainer;
	
	private volatile PlayerGroup group;
	private volatile boolean hasWildcard;
	private volatile boolean groupHasWildcard;
	private Location lastLocation;
	private UUID lastMessageRecipient;
	private String tabHeader;
	private String tabFooter;
	private String tabPrefix;
	private String tabSuffix;
	
	public ImplementedAccount(BAEssentials instance, ImplementedAPI api, UUID uuid, int id) {
		this(instance, api, uuid, id, 0, 1);
	}
	
	public ImplementedAccount(BAEssentials instance, ImplementedAPI api, UUID uuid, int id, int gold, int groupId) {
		this.instance = instance;
		this.api = api;
		this.uuid = uuid;
		this.id = id;
		this.permissions = Collections.synchronizedMap(new HashMap<String, Boolean>());
		this.teleportRequests = new HashMap<UUID, Long>();
		this.hiddenPlayers = new ArrayList<UUID>();
		
		this.gold = new AtomicInteger(gold);
		this.group = this.api.getPermissionManager().getGroup(groupId);
		this.tabHeader = this.api.getDefaultTabHeader();
		this.tabFooter = this.api.getDefaultTabFooter();
		this.tabPrefix = this.group.getPrefix();
		this.tabSuffix = this.group.getSuffix();
	}
	
	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public UUID getUUID() {
		return this.uuid;
	}
	
	@Override
	public Player getPlayer() {
		return this.player;
	}
	
	@Override
	public int getGold() {
		return this.gold.get();
	}
	
	@Override
	public PlayerGroup getGroup() {
		return this.group;
	}
	
	@Override
	public int getPing() {
		return this.api.getPing(player);
	}

	@Override
	public void setLocalGroup(PlayerGroup group) {
		this.group = group;
	}
	
	@Override
	public void setLocalGroupAndApply(PlayerGroup group) {
		if(this.tabPrefix.equals(this.group.getPrefix())) {
			setTabPrefix(group.getPrefix());
		}
		if(this.tabSuffix.equals(this.group.getSuffix())) {
			setTabSuffix(group.getSuffix());
		}
		
		this.groupHasWildcard = false;
		this.permissible.clearPermissions();
		this.group = group;
		applyPermissions();
	}
	
	@Override
	public void updateRemoteGroup(ThrowingConsumer<Integer> consumer) throws SQLException {
		PreparedStatement statement = this.api.getMySQLClient().prepareStatement("UPDATE `players` SET `group` = ? WHERE `id` = ?;");
		statement.setInt(1, this.group.getId());
		statement.setInt(2, this.id);
		
		this.api.getMySQLClient().executeUpdateAsynchronously(statement, consumer);
	}
	
	@Override
	public void clearPermissions(boolean keepOwnWildcard) {
		this.hasWildcard = this.hasWildcard && keepOwnWildcard;
		this.groupHasWildcard = false;
		this.permissible.clearPermissions();
		applyWildcard();
	}
	
	@Override
	public void setWildcard(boolean hasWildcard) {
		this.hasWildcard = hasWildcard;
	}
	
	@Override
	public void setAndApplyWildcard(boolean hasWildcard) {
		setWildcard(hasWildcard);
		applyWildcard();
	}
	
	@Override
	public void applyWildcard() {
		this.permissible.setWildcard(hasWildcardPermission());
	}
	
	@Override
	public void setGold(int newBalance) {
		this.gold.set(newBalance);
	}
	
	@Override
	public void giveGold(int toGive) {
		if(toGive < 0) {
			throw new IllegalArgumentException("Can't give a negative amount of gold!");
		}
		try {
			this.gold.set(Math.addExact(this.gold.get(), toGive));
		} catch (ArithmeticException exc) {
			this.gold.set(Integer.MAX_VALUE);
		}
	}
	
	@Override
	public void takeGold(int toTake) {
		if(toTake < 0) {
			throw new IllegalArgumentException("Can't take a negative amount of gold!");
		}
		this.gold.set(Math.max(this.gold.get() - toTake, 0));
	}
	
	@Override
	public void updateRemoteGold(ThrowingConsumer<Integer> consumer) throws SQLException {
		PreparedStatement statement = this.api.getMySQLClient().prepareStatement("UPDATE `players` SET `gold` = ? WHERE `id` = ?;");
		statement.setInt(1, this.gold.get());
		statement.setInt(2, this.id);
		
		this.api.getMySQLClient().executeUpdateAsynchronously(statement, consumer);
	}
	
	@Override
	public void setLastLocation(Location location) {
		this.lastLocation = location;
	}
	
	@Override
	public Location getLastLocation() {
		return this.lastLocation;
	}
	
	@Override
	public void updateLastLocation() {
		this.lastLocation = getPlayer().getLocation();
	}
	
	@Override
	public boolean hasTeleportationRequest(Player issuer) {
		UUID uuid = issuer.getUniqueId();
		if(this.teleportRequests.containsKey(uuid)) {
			long issued = this.teleportRequests.get(uuid);
			if(issued + (this.api.getTeleportationRequestTimeout() * 1000) < System.currentTimeMillis()) {
				this.teleportRequests.remove(uuid);
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public void issueTeleportationRequest(Player issuer) {
		this.teleportRequests.put(issuer.getUniqueId(), System.currentTimeMillis());
	}
	
	@Override
	public void removeTeleportationRequest(Player issuer) {
		this.teleportRequests.remove(issuer.getUniqueId());
	}
	
	@Override
	public void sendTabHeaderFooter() {
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void sendTabHeaderFooter(String header, String footer) {
		this.tabHeader = header;
		this.tabFooter = footer;
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void sendTabHeader(String header) {
		this.tabHeader = header;
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void sendTabFooter(String footer) {
		this.tabFooter = footer;
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void clearTabHeaderFooter() {
		this.tabHeader = null;
		this.tabFooter = null;
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void clearTabHeader() {
		this.tabHeader = null;
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void clearTabFooter() {
		this.tabFooter = null;
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void resetTabHeaderFooter() {
		this.tabHeader = this.api.getDefaultTabHeader();
		this.tabFooter = this.api.getDefaultTabFooter();
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void resetTabHeader() {
		this.tabHeader = this.api.getDefaultTabHeader();
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public void resetTabFooter() {
		this.tabFooter = this.api.getDefaultTabFooter();
		this.api.sendTabHeaderFooter(this.player, this.tabHeader, this.tabFooter);
	}
	
	@Override
	public String getTabPrefix() {
		return this.tabPrefix;
	}
	
	@Override
	public String getTabSuffix() {
		return this.tabSuffix;
	}
	
	@Override
	public void setTabPrefix(String prefix) {
		this.tabPrefix = prefix;
		this.api.getScoreboardHandler().update(this);
	}
	
	@Override
	public void setTabSuffix(String suffix) {
		this.tabSuffix = suffix;
		this.api.getScoreboardHandler().update(this);
	}
	
	@Override
	public void resetTabPrefix() {
		setTabPrefix(this.group.getPrefix());
	}
	
	@Override
	public void resetTabSuffix() {
		setTabSuffix(this.group.getSuffix());
	}
	
	@Override
	public void setLastMessageRecipient(OnlinePlayerAccount recipient) {
		this.lastMessageRecipient = recipient.getUUID();
	}
	
	@Override
	public OnlinePlayerAccount getLastMessageRecipient() {
		if(this.lastMessageRecipient != null) {
			return this.api.getOnlineAccount(this.lastMessageRecipient);
		}
		return null;
	}
	
	@Override
	public void applyPermissions() {
		for(PlayerGroup group : this.group.getInheritanceChain()) {
			if(group.hasWildcard()) {
				this.groupHasWildcard = true;
			}
			for(Entry<String, Boolean> entry : group.getPermissions().entrySet()) {
				this.permissionContainer.setPermission(entry.getKey(), entry.getValue());
			}
		}
		
		for(Entry<String, Boolean> entry : this.permissions.entrySet()) {
			this.permissionContainer.setPermission(entry.getKey(), entry.getValue());
		}

		applyWildcard();
	}
	
	@Override
	public void addPermission(String permission, boolean value) {
		if(permission.equals("*")) {
			if(!value) {
				throw new IllegalArgumentException("Can't add inverted Wildcard-Permission to Player-Account " + this.id);
			}
			this.hasWildcard = true;
		} else {
			this.permissions.put(permission.toLowerCase(), value);
		}
	}
	
	@Override
	public void addAndApply(String permission, boolean value) {
		addPermission(permission, value);
		if(permission.equals("*")) {
			this.hasWildcard = true;
			applyWildcard();
		} else {
			this.permissionContainer.setPermission(permission, value);
		}
	}
	
	@Override
	public void addRemotePermission(String permission, boolean value, String scope, ThrowingConsumer<Integer> consumer) throws SQLException {
		PreparedStatement statement = this.api.getMySQLClient().prepareStatement("INSERT INTO `permissions` (`holder`, `isplayer`, `action`, `permission`, `scope`) VALUES (?, b'1', ?, ?, ?);");
		statement.setInt(1, this.id);
		statement.setBoolean(2, value);
		statement.setString(3, permission);
		statement.setString(4, scope);
		
		this.api.getMySQLClient().executeUpdateAsynchronously(statement, consumer);
	}
	
	@Override
	public void removePermission(String permission) {
		if(permission.equals("*")) {
			this.hasWildcard = false;
		} else {
			this.permissions.remove(permission.toLowerCase());
		}
	}
	
	@Override
	public void removeAndApply(String permission) {
		removePermission(permission);
		this.permissionContainer.unsetPermission(permission);
	}

	@Override
	public void removeRemotePermission(String permission, ThrowingConsumer<Integer> consumer) throws SQLException {
		PreparedStatement statement = this.api.getMySQLClient().prepareStatement("DELETE FROM `permissions` WHERE `permission` = ? AND `holder` = ? AND `isplayer` = b'1';");
		statement.setString(1, permission);
		statement.setInt(2, this.id);
		
		this.api.getMySQLClient().executeUpdateAsynchronously(statement, consumer);
	}

	@Override
	public boolean isPermissionSet(String permission) {
		if(permission.equals("*")) {
			return this.hasWildcard;
		}
		return this.permissions.containsKey(permission.toLowerCase());
	}
	
	@Override
	public void disconnect(String message) {
		Bukkit.getScheduler().runTask(this.instance, new Runnable() {
			@Override
			public void run() {
				player.kickPlayer(message);
				loggedIn = false;
			}
		});
	}
	
	@Override
	public boolean hasWildcardPermission() {
		return this.hasWildcard || this.groupHasWildcard;
	}
	
	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return this.permissible.getEffectivePermissions();
	}
	
	@Override
	public Map<String, Boolean> getPermissions() {
		return new HashMap<String, Boolean>(this.permissions);
	}
	
	@Override
	public OnlinePlayerAccount login(Player player) throws Exception {
		if(this.loggedIn) {
			throw new IllegalAccessException("Player-Login already processed!");
		}
		
		this.loggedIn = true;
		this.player = player;
		this.permissible = new BAEPermissible(this.player, this.permissionContainer);
		this.permissionContainer = this.permissible.addAttachment(this.instance);
		this.api.getPermissionManager().getInjector().inject(this.player, this.permissible);
		this.api.getScoreboardHandler().update(this);
		
		applyPermissions();
		
		Bukkit.getScheduler().runTask(this.instance, new Runnable() {
			@Override
			public void run() {
				if(player.isOnline()) {
					sendTabHeaderFooter();
				}
			}
		});
		
		return (OnlinePlayerAccount) this;
	}
	
	@Override
	public boolean isLoggedIn() {
		return this.loggedIn;
	}
	
	@Override
	public OnlinePlayerAccount getOnlineAccount() {
		if(this.loggedIn) {
			return (OnlinePlayerAccount) this;
		}
		return null;
	}
	
	@Override
	public boolean loadPermissions() {
		PreparedStatement statement;
		try {
			statement = this.api.getMySQLClient().prepareStatement("SELECT `action`,`permission`,`scope` FROM `permissions` WHERE `holder` = ? AND `isplayer` = b'1';");
			statement.setInt(1, this.id);
			
			ResultSet rs = this.api.getMySQLClient().executeQuery(statement);
			this.permissions.clear();
			this.hasWildcard = false;
			while (rs.next()) {
				if(!rs.getString(3).equals("*")) {
					if(!this.api.getBungeename().matches(rs.getString(3))) {
						continue;
					}
				}
				addPermission(rs.getString(2), rs.getBoolean(1));
			}
			return true;
		} catch (SQLException exc) {
			instance.getLogger().log(Level.SEVERE, "Failed to load Player-Permissions [" + this.uuid.toString() + "/" + this.id + "]", exc);
			if(this.loggedIn) {
				disconnect(this.api.getMessage("general.synchronization-failure"));
			}
			return false;
		}	
	}
	
	@Override
	public void loadPermissionsAsynchronously() {
		PreparedStatement statement;
		try {
			statement = this.api.getMySQLClient().prepareStatement("SELECT `action`,`permission` FROM `permissions` WHERE `holder` = ? AND `isplayer` = b'1';");
			statement.setInt(1, this.id);
		} catch (SQLException exc) {
			instance.getLogger().log(Level.SEVERE, "Failed to load Player-Permissions [" + this.uuid.toString() + "/" + this.id + "]", exc);
			if(loggedIn) {
				disconnect(this.api.getMessage("general.synchronization-failure"));
			}
			return;
		}
		
		this.api.getMySQLClient().executeQueryAsynchronously(statement, new ThrowingConsumer<ResultSet>() {				
			@Override
			public void accept(ResultSet rs) {
				permissions.clear();
				hasWildcard = false;
				try {
					while (rs.next()) {
						if(!rs.getString(3).equals("*")) {
							if(!api.getBungeename().matches(rs.getString(3))) {
								continue;
							}
						}
						addPermission(rs.getString(2), rs.getBoolean(1));
					}
				} catch (SQLException exc) {
					instance.getLogger().log(Level.SEVERE, "Failed to load Player-Permissions [" + uuid.toString() + "/" + id + "]", exc);
					if(loggedIn) {
						disconnect(api.getMessage("general.synchronization-failure"));
					}
				}
			}
			
			@Override
			public void throwException(Exception exc) {
				instance.getLogger().log(Level.SEVERE, "Failed to load Player-Permissions [" + uuid.toString() + "/" + id + "]", exc);
				if(loggedIn) {
					disconnect(api.getMessage("general.synchronization-failure"));
				}
			}
		});	
	}
	
	@Override
	public boolean loadGroupAndApply() {
		PreparedStatement statement;
		try {
			statement = this.api.getMySQLClient().prepareStatement("SELECT `group` FROM `players` WHERE `id` = ?;");
			statement.setInt(1, this.id);
			
			ResultSet rs = this.api.getMySQLClient().executeQuery(statement);
			rs.next();
			this.groupHasWildcard = false;
			if(this.loggedIn) {
				this.permissible.clearPermissions();
				setLocalGroupAndApply(this.api.getPermissionManager().getGroup(rs.getInt(1)));
			}
			return true;
		} catch (SQLException exc) {
			instance.getLogger().log(Level.SEVERE, "Failed to load Player-Group [" + this.uuid.toString() + "/" + this.id + "]", exc);
			if(loggedIn) {
				disconnect(this.api.getMessage("general.synchronization-failure"));
			}
			return false;
		}	
	}
	
	@Override
	public void loadGroupAndApplyAsynchronously() {
		PreparedStatement statement;
		try {
			statement = this.api.getMySQLClient().prepareStatement("SELECT `group` FROM `players` WHERE `id` = ?;");
			statement.setInt(1, this.id);
		} catch (SQLException exc) {
			instance.getLogger().log(Level.SEVERE, "Failed to load Player-Group [" + this.uuid.toString() + "/" + this.id + "]", exc);
			if(loggedIn) {
				disconnect(this.api.getMessage("general.synchronization-failure"));
			}
			return;
		}
		
		this.api.getMySQLClient().executeQueryAsynchronously(statement, new ThrowingConsumer<ResultSet>() {				
			@Override
			public void accept(ResultSet rs) {
				try {
					rs.next();
					groupHasWildcard = false;
					if(loggedIn) {
						permissible.clearPermissions();
						setLocalGroupAndApply(api.getPermissionManager().getGroup(rs.getInt(1)));
					}
				} catch (SQLException exc) {
					instance.getLogger().log(Level.SEVERE, "Failed to load Player-Group [" + uuid.toString() + "/" + id + "]", exc);
					if(loggedIn) {
						disconnect(api.getMessage("general.synchronization-failure"));
					}
				}
			}
			
			@Override
			public void throwException(Exception exc) {
				instance.getLogger().log(Level.SEVERE, "Failed to load Player-Group [" + uuid.toString() + "/" + id + "]", exc);
				if(loggedIn) {
					disconnect(api.getMessage("general.synchronization-failure"));
				}
			}
		});	
	}
}