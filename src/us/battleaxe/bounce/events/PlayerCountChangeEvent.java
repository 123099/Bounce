package us.battleaxe.bounce.events;

import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.events.Event;

public class PlayerCountChangeEvent extends Event{

	private GameManager gameManager;
	private int playerCount;
	
	public PlayerCountChangeEvent(GameManager gameManager, int playerCount) {
		this.gameManager = gameManager;
		this.playerCount = playerCount;
	}
	
	public GameManager getGameManager() {
		return gameManager;
	}
	
	public int getPlayerCount() {
		return playerCount;
	}
}
