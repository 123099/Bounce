package us.battleaxe.bounce.utils.events;

import java.util.ArrayList;
import java.util.List;

public class EventRaiser {

	private List<EventListener> listeners = new ArrayList<>();
	
	public void registerListener(EventListener listener) {
		listeners.add(listener);
	}
	
	public void unregisterListener(EventListener listener) {
		listeners.remove(listener);
	}
	
	public void removeAllListeners() {
		listeners.clear();
	}
	
	public int getListenerCount() {
		return listeners.size();
	}
	
	public <T extends Event> void raiseEvent(Object sender, T event) {
		for(int i = 0; i < listeners.size(); ++i)
			listeners.get(i).onEvent(sender, event);
	}
}
