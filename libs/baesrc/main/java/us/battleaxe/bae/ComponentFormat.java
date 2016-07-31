package us.battleaxe.bae;

import org.apache.commons.lang.Validate;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;

public class ComponentFormat implements Cloneable {

	private ChatColor color;
	private boolean obfuscated;
	private boolean bold;
	private boolean strikethrough;
	private boolean underlined;
	private boolean italic;
	
	public ComponentFormat() {
		this(ChatColor.RESET);
	}
	
	public ComponentFormat(ChatColor color) {
		Validate.notNull(color, "Color can't be null!");
		Validate.isTrue(isColor(color), color.getName() + " is not a color but a formatting code!");
		this.color = color;
	}
	
	public ChatColor getColor() {
		return this.color;
	}
	
	public boolean isObfuscated() {
		return this.obfuscated;
	}

	public boolean isBold() {
		return this.bold;
	}

	public boolean isStrikethrough() {
		return this.strikethrough;
	}

	public boolean isUnderline() {
		return this.underlined;
	}

	public boolean isItalic() {
		return this.italic;
	}
	
	public ComponentFormat setColor(ChatColor color) {
		Validate.notNull(color, "Color can't be null!");
		Validate.isTrue(isColor(color), color.getName() + " is not a color but a formatting code!");
		if(color == ChatColor.RESET) {
			return reset();
		}
		this.color = color;
		return this;
	}
	
	public ComponentFormat setObfuscated(boolean obfuscated) {
		this.obfuscated = obfuscated;
		return this;
	}

	public ComponentFormat setBold(boolean bold) {
		this.bold = bold;
		return this;
	}

	public ComponentFormat setStrikethrough(boolean strikethrough) {
		this.strikethrough = strikethrough;
		return this;
	}

	public ComponentFormat setUnderlined(boolean underlined) {
		this.underlined = underlined;
		return this;
	}

	public ComponentFormat setItalic(boolean italic) {
		this.italic = italic;
		return this;
	}
	
	public ComponentFormat apply(ChatColor effect) {
		Validate.notNull(effect, "Effect can't be null!");
		switch (effect) {
		case RESET:
			reset();
			break;
		case AQUA:
		case BLACK:
		case BLUE:
		case DARK_AQUA:
		case DARK_BLUE:
		case DARK_GRAY:
		case DARK_GREEN:
		case DARK_PURPLE:
		case DARK_RED:
		case GOLD:
		case GRAY:
		case GREEN:
		case LIGHT_PURPLE:
		case RED:
		case WHITE:
		case YELLOW:
			this.color = effect;
			break;
		case BOLD:
			this.bold = true;
			break;
		case ITALIC:
			this.italic = true;
			break;
		case MAGIC:
			this.obfuscated = true;
			break;
		case STRIKETHROUGH:
			this.strikethrough = true;
			break;
		case UNDERLINE:
			this.underlined = true;
			break;
		default:
			break;
		}
		return this;
	}
	
	public ComponentFormat reset() {
		this.color = ChatColor.RESET;
		this.obfuscated = false;
		this.bold = false;
		this.strikethrough = false;
		this.underlined = false;
		this.italic = false;
		return this;
	}
	
	public BaseComponent format(BaseComponent component) {
		component.setColor(this.color);
		component.setObfuscated(this.obfuscated);
		component.setBold(this.bold);
		component.setStrikethrough(this.strikethrough);
		component.setUnderlined(this.underlined);
		component.setItalic(this.italic);
		return component;
	}
	
	@Override
	public ComponentFormat clone() {
		return new ComponentFormat(this.color)
				.setObfuscated(this.obfuscated)
				.setBold(this.bold)
				.setStrikethrough(this.strikethrough)
				.setUnderlined(this.underlined)
				.setItalic(this.italic);
	}
	
	@Override
	public String toString() {
		return "ComponentFormat [color=" + this.color.getName()
				+ ", obfuscated=" + this.obfuscated
				+ ", bold=" + this.bold
				+ ", strikethrough=" + this.strikethrough
				+ ", underlined="+ this.underlined
				+ ", italic=" + this.italic + "]";
	}

	private boolean isColor(ChatColor color) {
		switch (color) {
		case AQUA:
		case BLACK:
		case BLUE:
		case DARK_AQUA:
		case DARK_BLUE:
		case DARK_GRAY:
		case DARK_GREEN:
		case DARK_PURPLE:
		case DARK_RED:
		case GOLD:
		case GRAY:
		case GREEN:
		case LIGHT_PURPLE:
		case RED:
		case WHITE:
		case YELLOW:
		case RESET:
			return true;
		default:
			return false;
		}
	}
}