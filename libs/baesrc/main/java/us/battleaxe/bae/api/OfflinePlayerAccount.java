package us.battleaxe.bae.api;

import org.bukkit.entity.Player;

public interface OfflinePlayerAccount extends PlayerAccount {
	
	OnlinePlayerAccount login(Player player) throws Exception;
	OnlinePlayerAccount getOnlineAccount();
	boolean isLoggedIn();
	
}