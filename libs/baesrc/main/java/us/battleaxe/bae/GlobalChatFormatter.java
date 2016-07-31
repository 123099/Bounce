package us.battleaxe.bae;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.AbstractChatFormatter;
import us.battleaxe.bae.chat.AdvancedReplacementRule;
import us.battleaxe.bae.permissions.PlayerGroup;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GlobalChatFormatter extends AbstractChatFormatter {
	
	private final API api;
	private final List<String> filteredWords;
	private final List<String> replacementWords;
	private final PlayerNameChatFormatter playerNameChatFormatter;
	private final NumberFormat numberFormat;
	
	public GlobalChatFormatter(API api, ConfigurationSection config, List<String> filteredWords, List<String> replacementWords) {
		super(ChatColor.translateAlternateColorCodes('&', config.getString("format")));
		this.api = api;
		this.filteredWords = filteredWords;
		this.replacementWords = replacementWords;
		this.playerNameChatFormatter = new PlayerNameChatFormatter(this.api, ChatColor.translateAlternateColorCodes('&', config.getString("player-name-hover")));
		this.numberFormat = NumberFormat.getInstance(Locale.US);

		addDefaultRules();
	}
	
	public List<String> getFilteredWords() {
		return new ArrayList<String>(this.filteredWords);
	}
	
	public List<String> getReplacementWords() {
		return new ArrayList<String>(this.replacementWords);
	}
	
	private void addDefaultRules() {
		addReplacementRule(new AdvancedReplacementRule("{full_name}", getCurrentReplacementRule("{full_name}")) {
			@Override
			public BaseComponent getResult(Player player) {
				PlayerGroup group = api.getOnlineAccount(player).getGroup();
				String fullName = ChatColor.translateAlternateColorCodes('&', group.getPrefix() + " " + player.getName() + " " + group.getSuffix());
				TextComponent component = new TextComponent(TextComponent.fromLegacyText(fullName));
				this.defaultFormat.format(component);
				component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new BaseComponent[] { playerNameChatFormatter.format(player) }));
				return component;
			}
		});
		addReplacementRule(new AdvancedReplacementRule("{name}", getCurrentReplacementRule("{name}")) {
			@Override
			public BaseComponent getResult(Player player) {
				TextComponent component = new TextComponent(player.getName());
				this.defaultFormat.format(component);
				component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new BaseComponent[] { playerNameChatFormatter.format(player) }));
				return component;
			}
		});
		addReplacementRule(new AdvancedReplacementRule("{rank}", getCurrentReplacementRule("{rank}")) {		
			@Override
			public BaseComponent getResult(Player player) {
				return this.defaultFormat.format(new TextComponent(api.getOnlineAccount(player).getGroup().getName()));
			}
		});
		addReplacementRule(new AdvancedReplacementRule("{prefix}", getCurrentReplacementRule("{prefix}")) {
			@Override
			public BaseComponent getResult(Player player) {
				TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', api.getOnlineAccount(player).getGroup().getPrefix()));
				this.defaultFormat.format(component);
				return component;
			}
		});
		addReplacementRule(new AdvancedReplacementRule("{suffix}", getCurrentReplacementRule("{suffix}")) {
			@Override
			public BaseComponent getResult(Player player) {
				TextComponent component = new TextComponent(ChatColor.translateAlternateColorCodes('&', api.getOnlineAccount(player).getGroup().getSuffix()));
				this.defaultFormat.format(component);
				return component;
			}
		});
		addReplacementRule(new AdvancedReplacementRule("{gold}", getCurrentReplacementRule("{gold}")) {
			@Override
			public BaseComponent getResult(Player player) {
				TextComponent component = new TextComponent(numberFormat.format((int) api.getOnlineAccount(player).getGold()));
				this.defaultFormat.format(component);
				return component;
			}
		});
	}
}