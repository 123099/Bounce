package us.battleaxe.bounce.events;

import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.events.BukkitRunnableCancelledEvent;

public class GameTimerEvent extends BukkitRunnableCancelledEvent{

	private GameManager gameManager;
	
	public GameTimerEvent(int taskID, GameManager gameManager) {
		super(taskID);
		this.gameManager = gameManager;
	}

	public GameManager getGameManager() {
		return gameManager;
	}
}