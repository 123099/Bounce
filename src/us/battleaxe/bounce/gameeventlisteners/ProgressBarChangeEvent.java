package us.battleaxe.bounce.gameeventlisteners;

import us.battleaxe.bounce.utils.events.Event;

public class ProgressBarChangeEvent extends Event{

	private String progressBar;
	
	public ProgressBarChangeEvent(String progressBar) {
		this.progressBar = progressBar;
	}
	
	public String getProgressBar() {
		return progressBar;
	}
}
