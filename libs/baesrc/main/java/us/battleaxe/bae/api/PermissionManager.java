package us.battleaxe.bae.api;

import java.util.List;

import us.battleaxe.bae.permissions.PermissibleInjector;
import us.battleaxe.bae.permissions.PlayerGroup;

public interface PermissionManager {

	PlayerGroup getGroup(int id);
	PlayerGroup getGroup(String name);
	PlayerGroup parseGroup(String input);
	List<PlayerGroup> getGroups();
	PermissibleInjector getInjector();
	void registerGroup(PlayerGroup group);
	void unregisterGroup(PlayerGroup group);

}