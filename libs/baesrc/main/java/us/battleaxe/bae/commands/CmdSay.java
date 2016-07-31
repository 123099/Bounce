package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import us.battleaxe.bae.Util;

public final class CmdSay extends CmdExecutor {
	
	public CmdSay() {
		super("say", "cmd.say", true);
	}
	
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, -1, 1)) return false;
		if(!isConsole(sender)) return false;
		
		try {
			TextComponent msg = new TextComponent(ComponentSerializer.parse(Util.compile(args)));
			for(Player online : Bukkit.getOnlinePlayers()) {
				online.spigot().sendMessage(msg);
			}
			sender.sendMessage(getCommandMessage("success", true, msg.toLegacyText()));
			return true;
		} catch (Exception e) {
			sender.sendMessage(getCommandMessage("invalid-json"));
			e.printStackTrace();
			return false;
		}
	}
}