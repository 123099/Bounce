package us.battleaxe.bounce;

import org.bukkit.ChatColor;

public enum GameStatus {

	InLobby(ChatColor.UNDERLINE + "In Lobby"),
	Starting(ChatColor.UNDERLINE + "Starting"),
	InProgress(ChatColor.UNDERLINE + "In Progress");
	
	private String statusDisplay;
	
	private GameStatus(String display) {
		statusDisplay = display;
	}
	
	@Override
	public String toString() {
		return statusDisplay;
	}
}
