package us.battleaxe.bounce.utils;

import java.util.HashMap;

import org.bukkit.entity.Player;

public final class PlayerExtension {

	private static HashMap<Player, HashMap<String, Object>> customAttributes = new HashMap<>();
	
	public static void AddAttribute(Player player, String attribute, Object value) {
		getPlayerAttributes(player).put(attribute, value);
	}
	
	public static void RemoveAttribute(Player player, String attribute) {
		getPlayerAttributes(player).remove(attribute);
	}
	
	public static void RemoveAllAttributes(Player player) {
		customAttributes.remove(player);
	}
	
	public static Object GetAttribute(Player player, String attribute) {
		return getPlayerAttributes(player).get(attribute);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T GetAttribute(Class<T> clazz, Player player, String attribute) {
		Object result = GetAttribute(player, attribute);
		return result == null ? null : (T)result;
	}
	
	public static boolean ContainsAttribute(Player player, String attribute) {
		return GetAttribute(player, attribute) != null;
	}
	
	private static HashMap<String, Object> getPlayerAttributes(Player player){
		HashMap<String, Object> playerAttributes = customAttributes.get(player);
		
		if(playerAttributes == null) { 
			playerAttributes = new HashMap<String, Object>();
			customAttributes.put(player, playerAttributes);
		}
		
		return playerAttributes;
	}
}
