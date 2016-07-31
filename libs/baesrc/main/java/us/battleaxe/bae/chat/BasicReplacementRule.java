package us.battleaxe.bae.chat;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import us.battleaxe.bae.ComponentFormat;

public final class BasicReplacementRule implements ReplacementRule {

	private final String replaced;
	private final ComponentFormat defaultFormat;
	
	public BasicReplacementRule(String replaced, ComponentFormat defaultFormat) {
		this.replaced = replaced;
		this.defaultFormat = defaultFormat;
	}
	
	@Override
	public String getReplaced() {
		return this.replaced;
	}
	
	@Override
	public ComponentFormat getDefaultFormat() {
		return this.defaultFormat;
	}

	@Override
	public BaseComponent getResult(Player player) {
		TextComponent component = new TextComponent(this.replaced);
		this.defaultFormat.format(component);
		return component;
	}
}