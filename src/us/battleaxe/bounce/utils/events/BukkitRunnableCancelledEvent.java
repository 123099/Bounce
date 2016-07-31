package us.battleaxe.bounce.utils.events;

public class BukkitRunnableCancelledEvent extends Event{

	private int taskID;
	
	public BukkitRunnableCancelledEvent(int taskID) {
		this.taskID = taskID;
	}
	
	public int getTaskID() {
		return taskID;
	}
}
