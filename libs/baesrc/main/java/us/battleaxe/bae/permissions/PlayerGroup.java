package us.battleaxe.bae.permissions;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import us.battleaxe.bae.ThrowingConsumer;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.OnlinePlayerAccount;

public class PlayerGroup {
	
	private final API api;
	private final int id;
	private final String name;
	private final Map<String, Boolean> permissions;
	private final List<PlayerGroup> inheritanceChain;
	
	private String prefix;
	private String suffix;
	private PlayerGroup parent;
	private boolean hasWildcard;
	
	public PlayerGroup(API api, PlayerGroupDescription description) {
		this(api, description.getId(), description.getName(), null, description.getPrefix(), description.getSuffix());
	}

	public PlayerGroup(API api, int id, String name, PlayerGroup parent, String prefix, String suffix) {
		this.api = api;
		this.id = id;
		this.name = name;
		this.permissions = Collections.synchronizedMap(new HashMap<String, Boolean>());
		this.inheritanceChain = new CopyOnWriteArrayList<PlayerGroup>();
		
		this.parent = parent;
		this.prefix = prefix;
		this.suffix = suffix;
		
		buildInheritanceChain();
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public Map<String, Boolean> getPermissions() {
		return new HashMap<String, Boolean>(this.permissions);
	}
	
	public List<PlayerGroup> getInheritanceChain() {
		return new ArrayList<PlayerGroup>(this.inheritanceChain);
	}
	
	public PlayerGroup getParent() {
		return this.parent;
	}
	
	public String getPrefix() {
		return this.prefix;
	}
	
	public String getSuffix() {
		return this.suffix;
	}
	
	public void addPermission(String permission, boolean value) {
		this.permissions.put(permission, value);
	}
	
	public void addAndApply(String permission, boolean value) {
		if(permission.equals("*")) {
			if(!value) {
				throw new IllegalArgumentException("Can't add inverted Wildcard-Permission to PlayerGroup " + this.id);
			}
			this.hasWildcard = true;
		} else {
			this.permissions.put(permission, value);
		}
		for(Player online : Bukkit.getOnlinePlayers()) {
			OnlinePlayerAccount account = this.api.getOnlineAccount(online);
			if(account.getGroup().inheritanceChain.contains(this)) {
				account.clearPermissions(true);
				account.applyPermissions();
			}
		}
	}
	
	public void addRemotePermission(String permission, boolean value, String scope, ThrowingConsumer<Integer> consumer) throws SQLException {
		PreparedStatement statement = this.api.getMySQLClient().prepareStatement("INSERT INTO `permissions` (`holder`, `isplayer`, `action`, `permission`, `scope`) VALUES (?, b'0', ?, ?, ?);");
		statement.setInt(1, this.id);
		statement.setBoolean(2, value);
		statement.setString(3, permission);
		statement.setString(4, scope);
		
		this.api.getMySQLClient().executeUpdateAsynchronously(statement, consumer);
	}
	
	public void removePermission(String permission) {
		this.permissions.remove(permission);
	}
	
	public void removeAndApply(String permission) {
		if(permission.equals("*")) {
			this.hasWildcard = false;
		} else {
			this.permissions.remove(permission);
		}
		for(Player online : Bukkit.getOnlinePlayers()) {
			OnlinePlayerAccount account = this.api.getOnlineAccount(online);
			if(account.getGroup().inheritanceChain.contains(this)) {
				account.clearPermissions(true);
				account.applyPermissions();
			}
		}
	}

	public void removeRemotePermission(String permission, ThrowingConsumer<Integer> consumer) throws SQLException {
		PreparedStatement statement = this.api.getMySQLClient().prepareStatement("DELETE FROM `permissions` WHERE `permission` = ? AND `holder` = ? AND `isplayer` = b'0';");
		statement.setString(1, permission);
		statement.setInt(2, this.id);
		
		this.api.getMySQLClient().executeUpdateAsynchronously(statement, consumer);
	}

	public void setWildcard(boolean hasWildcard) {
		this.hasWildcard = hasWildcard;
	}
	
	public boolean hasWildcard() {
		return this.hasWildcard;
	}

	public void setParent(PlayerGroup parent) {
		this.parent = parent;
		buildInheritanceChain();
		
		for(Player online : Bukkit.getOnlinePlayers()) {
			OnlinePlayerAccount account = api.getOnlineAccount(online);
			account.clearPermissions(true);
			account.applyPermissions();
		}
	}

	public void removeFromInheritanceChain(PlayerGroup group) {
		if(group == null) {
			throw new IllegalArgumentException("Removed group cannot be null!");
		}
		if(this.inheritanceChain.contains(group)) {
			if(group.equals(this.parent)) {
				this.parent = this.inheritanceChain.get(this.inheritanceChain.indexOf(group) - 1);
			}
			this.inheritanceChain.remove(group);
		}
	}

	public void setPrefix(String prefix) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			OnlinePlayerAccount account = api.getOnlineAccount(online);
			if(account.getTabPrefix().equals(this.prefix)) {
				account.setTabPrefix(prefix);
			}
		}
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		for(Player online : Bukkit.getOnlinePlayers()) {
			OnlinePlayerAccount account = api.getOnlineAccount(online);
			if(account.getTabSuffix().equals(this.suffix)) {
				account.setTabSuffix(suffix);
			}
		}
		this.suffix = suffix;
	}
	
	public boolean isPermissionSet(String permission) {
		if(permission.equals("*") && this.hasWildcard) return true; 
		return this.permissions.containsKey(permission);
	}
	
	public void clearPermissions() {
		this.hasWildcard = false;
		this.permissions.clear();
	}

	@Override
	public String toString() {
		return "PlayerGroup[Id: " + this.id
				+ "; Name: " + this.name
				+ "; Prefix: " + this.prefix
				+ "; Suffix: " + this.suffix
				+ "; Parent: " + (this.parent != null ? this.parent.toString() : "<None>") + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj == null) {
			return false;
		}
		if(getClass() != obj.getClass()) {
			return false;
		}
		return (this.id == ((PlayerGroup) obj).getId());
	}
	
	private void buildInheritanceChain() {
		//Order by "Age" (Child as last)
		this.inheritanceChain.clear();
		if(this.parent != null) {
			this.inheritanceChain.addAll(this.parent.getInheritanceChain());
		}
		this.inheritanceChain.add(this);
	}
}