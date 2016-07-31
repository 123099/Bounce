package us.battleaxe.bounce.commands;

import java.util.HashMap;

public class PlayerCommandExecutorList {
	
	private HashMap<String, PlayerCommandExecutor> executors;
	
	public PlayerCommandExecutorList() {
		executors = new HashMap<>();
	}
	
	public void registerExecutor(String commandName, PlayerCommandExecutor executor) {
		commandName = commandName.toLowerCase();
		if(executors.containsKey(commandName)) return;
		executors.put(commandName, executor);
	}
	
	public void removeExecutor(String commandName) {
		commandName = commandName.toLowerCase();
		if(!executors.containsKey(commandName)) return;
		
		executors.remove(commandName);
	}
	
	public void setDefaultExecutor(PlayerCommandExecutor executor) {
		executors.put("default", executor);
	}
	
	public PlayerCommandExecutor getExecutor(String commandName) {
		return executors.get(commandName.toLowerCase());
	}
	
	public PlayerCommandExecutor getDefaultExecutor() {
		return executors.get("default");
	}
}
