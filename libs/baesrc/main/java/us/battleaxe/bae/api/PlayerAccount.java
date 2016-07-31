package us.battleaxe.bae.api;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;

import us.battleaxe.bae.ThrowingConsumer;
import us.battleaxe.bae.permissions.PlayerGroup;

public interface PlayerAccount {

	int getId();
	UUID getUUID();
	PlayerGroup getGroup();
	int getGold();
	void setGold(int newBalance);
	void giveGold(int toGive);
	void takeGold(int toTake);
	void updateRemoteGold(ThrowingConsumer<Integer> consumer) throws SQLException;
	void setLastLocation(Location location);
	Location getLastLocation();
	void setLastMessageRecipient(OnlinePlayerAccount recipient);
	OnlinePlayerAccount getLastMessageRecipient();
	void setLocalGroup(PlayerGroup group);
	void updateRemoteGroup(ThrowingConsumer<Integer> consumer) throws SQLException;
	void addRemotePermission(String permission, boolean value, String scope, ThrowingConsumer<Integer> consumer) throws SQLException;
	void removeRemotePermission(String permission, ThrowingConsumer<Integer> consumer) throws SQLException;
	boolean loadPermissions();
	boolean loadGroupAndApply();
	boolean hasWildcardPermission();
	void loadPermissionsAsynchronously();
	void loadGroupAndApplyAsynchronously();
	void addPermission(String permission, boolean value);
	void setWildcard(boolean hasWildcard);
	void removePermission(String permission);
	boolean isPermissionSet(String permission);
	Map<String, Boolean> getPermissions();
	
}