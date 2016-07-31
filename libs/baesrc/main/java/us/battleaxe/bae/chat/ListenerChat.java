package us.battleaxe.bae.chat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.events.BAEChatEvent;

public class ListenerChat implements Listener {

	private final API api;
	private final List<String> replacementWords;
	private final Pattern pattern;
	
	public ListenerChat(API api) {
		this.api = api;
		this.replacementWords = this.api.getChatFormatter().getReplacementWords();
		
		StringBuilder regex = new StringBuilder("\\b(?:");
		for(String word : this.api.getChatFormatter().getFilteredWords()) {
			regex.append("|").append(word);
		}
		//Remove first pipe ("|")
		regex.replace(5, 6, "").append(")\\b");
		this.pattern = Pattern.compile(regex.toString(), Pattern.CASE_INSENSITIVE);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onChat(AsyncPlayerChatEvent e) {
		Player player = e.getPlayer();
		e.setCancelled(true);
		if(this.api.getStaffManager().isInStaffChat(player)){
			String message = this.api.getMessage("staffmode.staffchat", player.getName(), e.getMessage());
			for(UUID uuid : this.api.getStaffManager().getStaffChatMembers()) {
				Bukkit.getPlayer(uuid).sendMessage(message);
			}
			return;
		}

		BAEChatEvent chatEvent = new BAEChatEvent(e.getPlayer());
		Bukkit.getPluginManager().callEvent(chatEvent);
		if(chatEvent.isCancelled()) return;
		
//		if(player.hasPermission("bae.staff") && SpellChecker.checkSpelling(player, e.getMessage(), api)) return;
		
		BaseComponent component = this.api.getChatFormatter().format(player);
		StringBuffer sb = new StringBuffer();
		Matcher matcher = this.pattern.matcher(e.getMessage());
		while(matcher.find()) {
			matcher.appendReplacement(sb, replacementWords.get(ThreadLocalRandom.current().nextInt(this.replacementWords.size())));
		}
		matcher.appendTail(sb);
		component.addExtra(new TextComponent(sb.toString()));
		if(!this.api.getWorldSeparator().getChatSeparated()) {
			Bukkit.spigot().broadcast(component);
			return;
		}
		
		for(Player online : Bukkit.getOnlinePlayers()) {
			if(player.getWorld().equals(online.getWorld())) {
				online.spigot().sendMessage(component);
			}
		}
	}
}