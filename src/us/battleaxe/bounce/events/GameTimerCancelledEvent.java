package us.battleaxe.bounce.events;

import us.battleaxe.bounce.managers.GameManager;
import us.battleaxe.bounce.utils.events.BukkitRunnableCancelledEvent;

public class GameTimerCancelledEvent extends GameTimerEvent{

	public GameTimerCancelledEvent(int taskID, GameManager gameManager) {
		super(taskID, gameManager);
	}
}
