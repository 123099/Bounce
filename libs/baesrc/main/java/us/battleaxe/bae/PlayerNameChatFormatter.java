package us.battleaxe.bae;

import java.text.NumberFormat;
import java.util.Locale;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import us.battleaxe.bae.api.API;
import us.battleaxe.bae.api.AbstractChatFormatter;
import us.battleaxe.bae.chat.AdvancedReplacementRule;

public class PlayerNameChatFormatter extends AbstractChatFormatter {

	private final API api;
	private final NumberFormat numberFormat;
	
	public PlayerNameChatFormatter(API api, String format) {
		super(format);
		this.api = api;
		this.numberFormat = NumberFormat.getInstance(Locale.US);
		
		addDefaultRules();
	}
	
	private void addDefaultRules() {
		addReplacementRule(new AdvancedReplacementRule("{name}", getCurrentReplacementRule("{name}")) {		
			@Override
			public BaseComponent getResult(Player player) {
				return this.defaultFormat.format(new TextComponent(player.getName()));
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