package us.battleaxe.bae.chat;

import us.battleaxe.bae.ComponentFormat;

public abstract class AdvancedReplacementRule implements ReplacementRule {

	protected final String replaced;
	protected final ComponentFormat defaultFormat;
	
	public AdvancedReplacementRule(String replaced, ReplacementRule current) {
		this(replaced, current != null ? current.getDefaultFormat() : new ComponentFormat());
	}
	
	public AdvancedReplacementRule(String replaced, ComponentFormat defaultFormat) {
		this.replaced = replaced;
		this.defaultFormat = defaultFormat;
	}
	
	@Override
	public ComponentFormat getDefaultFormat() {
		return this.defaultFormat;
	}
	
	@Override
	public String getReplaced() {
		return this.replaced;
	}
}