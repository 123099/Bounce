package us.battleaxe.bae.api;

import java.util.List;

import org.bukkit.entity.Player;

/* TODO: Actually use this
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
*/

public class SpellChecker{

	/* TODO: Actually use this
	private final static AmericanEnglish english = new AmericanEnglish();
	private final static String key = "spellchecker.";
	
	public final static boolean checkSpelling(Player player, String message, API api){
		final JLanguageTool languageTool = new JLanguageTool(english);
		final List<RuleMatch> matches;
		try {
			matches = languageTool.check(message);
		} catch (Exception e) {
			e.printStackTrace();
			player.sendMessage(api.getMessage(key + "error"));
			return true;
		}
		if(matches == null) return false;
		for(RuleMatch match : matches){
			player.sendMessage(api.getMessage(key + "corrections", String.valueOf(match.getColumn()), 
					match.getShortMessage(), String.valueOf(match.getSuggestedReplacements())));
		}
		return true;
	}*/
}