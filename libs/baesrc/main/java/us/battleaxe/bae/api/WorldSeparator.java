package us.battleaxe.bae.api;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import us.battleaxe.bae.api.visibility.GroupVisibilityPolicy;

public class WorldSeparator {
	
	private final API api;
	
	private boolean separateTab;
	private boolean separateChat;
	private boolean separateDeathMessages;

	private final GroupVisibilityPolicy policy;

	public WorldSeparator(API api) {
		this(api, true, true, true);
	}
	
	public WorldSeparator(API api, boolean separateTab, boolean separateChat, boolean separateDeathMessages) {
		this.api = api;
		this.separateTab = separateTab;
		this.separateChat = separateChat;
		this.separateDeathMessages = separateDeathMessages;

		this.policy = new GroupVisibilityPolicy();
		this.api.getVisibilityManager().registerPolicy((int) 1e5, policy);
	}

	public void setSeparateTab(boolean separateTab) {
		this.separateTab = separateTab;
	}
	
	public void setSeparateChat(boolean separateChat) {
		this.separateChat = separateChat;
	}
	
	public void setSeparateDeathMessages(boolean separateDeathMessages) {
		this.separateDeathMessages = separateDeathMessages;
	}

	public boolean getTabSeparated() {
		return separateTab;
	}
	
	public boolean getChatSeparated() {
		return separateChat;
	}
	
	public boolean getDeathMessagesSeparated() {
		return separateDeathMessages;
	}

	public void updateWorld(Player player) {
		if(!this.separateTab) return;
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(player.equals(online)) continue;
			if(player.getWorld().equals(online.getWorld())) {
				policy.setVisibility(player, online, null);
				policy.setVisibility(online, player, null);
			} else {
				policy.setVisibility(player, online, false);
				policy.setVisibility(online, player, false);
			}
		}
	}
	
	public boolean broadcastDeath(Player player, String deathMessage) {
		if(!this.separateDeathMessages) return false;
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(player.getWorld().equals(online.getWorld())) {
				online.sendMessage(deathMessage);
			}
		}
		return true;
	}
}