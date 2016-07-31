package us.battleaxe.bae.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import us.battleaxe.bae.Util;

public final class CmdItem extends CmdExecutor {

	public CmdItem() {
		super("item", "cmd.item", true);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected final boolean execute(final CommandSender sender, final String[] args) {
		if(!checkArgsLength(sender, args.length, 3, 1)) return false;
		if(args.length == 1) {
			if(!isPlayer(sender, true)) return false;
			Player player = (Player) sender;
			Material material = Material.matchMaterial(args[0]);
			if(material == null) {
				player.sendMessage(getCommandMessage("invalid-material"));
				return false;
			}
			player.getInventory().addItem(new ItemStack(material, material.getMaxStackSize()));
			player.sendMessage(getCommandMessage("success-self", Util.formatInt(material.getMaxStackSize()), material.toString(), String.valueOf(material.getId())));
			return true;
		}
		
		if(args.length == 2) {
			Player target = Bukkit.getPlayerExact(args[0]);
			//target == null ==> Arg0 = Item; Arg1 = Amount
			if(target == null) {
				if(!isPlayer(sender, true)) return false;
				Player player = (Player) sender;
				Material material = Material.matchMaterial(args[0]);
				if(material == null) {
					player.sendMessage(getCommandMessage("invalid-material-or-player"));
					return false;
				}
				if(!isPositiveNumber(sender, args[1])) return false;
				int amount = Integer.parseInt(args[1]);
				player.getInventory().addItem(new ItemStack(material, amount));
				player.sendMessage(getCommandMessage("success-self", Util.formatInt(amount), material.toString(), String.valueOf(material.getId())));
				return true;
			}
			
			//target != null ==> Arg0 = Target; Arg1 = Item
			Material material = Material.matchMaterial(args[1]);
			if(material == null) {
				sender.sendMessage(getCommandMessage("invalid-material"));
				return true;
			}
			target.getInventory().addItem(new ItemStack(material, material.getMaxStackSize()));
			target.sendMessage(getCommandMessage("target-success", Util.formatInt(material.getMaxStackSize()), material.toString(), String.valueOf(material.getId()), getName(sender)));
			sender.sendMessage(getCommandMessage("success-other", getName(sender), Util.formatInt(material.getMaxStackSize()), material.toString(), String.valueOf(material.getId())));
			return true;
		}
		
		if(args.length == 3) {
			Player target = Bukkit.getPlayerExact(args[0]);
			if(target == null) return noPlayer(sender, args[0]);
			final Material material = Material.matchMaterial(args[1]);
			if(material == null) {
				sender.sendMessage(getCommandMessage("invalid-material", args[1]));
				return false;
			}
			if(!isPositiveNumber(sender, args[2])) return false;
			int amount = Integer.parseInt(args[2]);
			target.getInventory().addItem(new ItemStack(material, amount));
			target.sendMessage(getCommandMessage("target-success", Util.formatInt(amount), material.toString(), String.valueOf(material.getId()), getName(sender)));
			sender.sendMessage(getCommandMessage("success-other", getName(sender), Util.formatInt(amount), material.toString(), String.valueOf(material.getId())));
			return true;
		}
		return false;
	}
}