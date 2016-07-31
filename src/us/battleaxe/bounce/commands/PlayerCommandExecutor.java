package us.battleaxe.bounce.commands;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PlayerCommandExecutor implements CommandExecutor {

	private PlayerCommandExecutorList executorList;
	
	public PlayerCommandExecutor() {
		executorList = new PlayerCommandExecutorList();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player) {
			Player player = (Player)sender;
			return onPlayerCommand(player, command, label, args);		
		}
		else
			sender.sendMessage("This command may only be used by a player!");
		
		return false;
	}

	public boolean onPlayerCommand(Player player, Command command, String label, String[] args) {
		PlayerCommandExecutor executor;
		if(args.length == 0)
			executor = executorList.getDefaultExecutor();
		else
			executor = executorList.getExecutor(args[0]);
		
		if(executor == null)
			return false;
		else
			return executor.onPlayerCommand(player, command, label, args);
	}
	
	protected PlayerCommandExecutorList getExecutors() {
		return executorList;
	}
}
