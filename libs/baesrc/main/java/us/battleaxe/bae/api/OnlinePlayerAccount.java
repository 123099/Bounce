package us.battleaxe.bae.api;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import us.battleaxe.bae.permissions.PlayerGroup;

public interface OnlinePlayerAccount extends PlayerAccount {

	Player getPlayer();
	int getPing();
	void setLocalGroupAndApply(PlayerGroup group);
	void clearPermissions(boolean keepOwnWildcard);
	void setAndApplyWildcard(boolean hasWildcard);
	void applyWildcard();
	void updateLastLocation();
	boolean hasTeleportationRequest(Player issuer);
	void issueTeleportationRequest(Player issuer);
	void removeTeleportationRequest(Player issuer);
	void sendTabHeaderFooter();
	void sendTabHeaderFooter(String header, String footer);
	void sendTabHeader(String header);
	void sendTabFooter(String footer);
	void clearTabHeaderFooter();
	void clearTabHeader();
	void clearTabFooter();
	void resetTabHeaderFooter();
	void resetTabHeader();
	void resetTabFooter();
	String getTabPrefix();
	String getTabSuffix();
	void setTabPrefix(String prefix);
	void setTabSuffix(String suffix);
	void resetTabPrefix();
	void resetTabSuffix();
	void applyPermissions();
	void addAndApply(String permission, boolean value);
	void removeAndApply(String permission);
	void disconnect(String message);
	Set<PermissionAttachmentInfo> getEffectivePermissions();

}