package us.battleaxe.bae.api;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import us.battleaxe.bae.ComponentFormat;
import us.battleaxe.bae.chat.BasicReplacementRule;
import us.battleaxe.bae.chat.ReplacementRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractChatFormatter {

	protected final Pattern colorPattern;
	protected final Pattern formatPattern;
	protected final List<ReplacementRule> rules;
	
	protected String format;
	
	public AbstractChatFormatter(String format) {
		this.colorPattern = Pattern.compile("§[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);
		this.formatPattern = Pattern.compile("\\{\\p{Graph}+?\\}");
		this.rules = Collections.synchronizedList(new ArrayList<ReplacementRule>());
		
		this.format = format;
		reload();
	}
	
	public String getFormat() {
		return this.format;
	}
	
	public void setFormat(String format) {
		this.format = format;
		reload();
	}
	
	public void addReplacementRule(ReplacementRule rule) {
		int toReplace = -1;
		synchronized (this.rules) {
			for(int i = 0; i < this.rules.size(); i++) {
				ReplacementRule active = this.rules.get(i);
				if(active.getReplaced().equals(rule.getReplaced())) {
					Matcher matcher = this.formatPattern.matcher(active.getReplaced());
					if(!matcher.find()) throw new IllegalArgumentException("Invalid regex for ReplacementRule (\"" + rule.getReplaced() + "\")");
					if(toReplace != -1) throw new IllegalStateException("To replacing String exists twice (\"" + rule.getReplaced() + "\")");
					toReplace = i;
				}
			}
			if(toReplace == -1) return; //Silently suppress
			this.rules.set(toReplace, rule);
		}
	}
	
	public ReplacementRule getCurrentReplacementRule(String replaced) {
		synchronized (this.rules) {
			for(ReplacementRule rule : this.rules) {
				if(rule.getReplaced().equals(replaced)) {
					return rule;
				}
			}
		}
		return null;
	}
	
	public BaseComponent format(Player player) {
		BaseComponent component = new TextComponent("");
		for(ReplacementRule rule : this.rules) {
			BaseComponent result = rule.getResult(player);
			rule.getDefaultFormat().format(result);
			component.addExtra(result);
		}
		return component;
	}
	
	protected void reload() {
		synchronized (this.rules) {
			this.rules.clear();
			
			Matcher colorMatcher = this.colorPattern.matcher(this.format);
			int pos = 0;
			ComponentFormat componentFormat = new ComponentFormat();
			while (colorMatcher.find()) {
				if(pos != colorMatcher.start()) {
					parse(this.format.substring(pos, colorMatcher.start()), componentFormat);
				}
				componentFormat.apply(ChatColor.getByChar(colorMatcher.group().charAt(1)));
				pos = colorMatcher.end();
			}
			if(pos != this.format.length()) {
				parse(this.format.substring(pos, this.format.length()), componentFormat);
			}
		}
	}

	private void parse(String subFormat, ComponentFormat componentFormat) {
		Matcher matcher = this.formatPattern.matcher(subFormat);
		int pos = 0;
		while (matcher.find()) {
			if(pos != matcher.start()) {
				this.rules.add(new BasicReplacementRule(subFormat.substring(pos, matcher.start()), componentFormat.clone()));
			}
			this.rules.add(new BasicReplacementRule(matcher.group(), componentFormat.clone()));
			pos = matcher.end();
		}
		if(pos != subFormat.length()) {
			this.rules.add(new BasicReplacementRule(subFormat.substring(pos, subFormat.length()), componentFormat.clone()));
		}
	}
}