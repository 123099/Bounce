package us.battleaxe.bounce.commands.bouncecommands;

import us.battleaxe.bounce.commands.CommandNames;
import us.battleaxe.bounce.commands.PlayerCommandExecutor;

public class PlayerCommandBounce extends PlayerCommandExecutor{
	
	public PlayerCommandBounce() {
		getExecutors().setDefaultExecutor(new BounceCommandAdd());
		getExecutors().registerExecutor(CommandNames.BounceAdd, new BounceCommandAdd());
	}
}
