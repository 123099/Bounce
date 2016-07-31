package us.battleaxe.bae.chat;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.BaseComponent;
import us.battleaxe.bae.ComponentFormat;

public interface ReplacementRule {

	String getReplaced();
	ComponentFormat getDefaultFormat();
	BaseComponent getResult(Player player);

}