package us.battleaxe.bae.commands;

import io.github.dommi2212.corelib.AdvancedItem;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
public class CmdHead extends CmdExecutor {

    public CmdHead() {
        super("head", "cmd.head", false);
    }

    @Override
    protected boolean execute(CommandSender sender, String[] args) {
        if(!checkArgsLength(sender, args.length, 1, 1)) return false;
        Player player = (Player) sender;

        AdvancedItem item = new AdvancedItem(Material.SKULL_ITEM, "§r" + args[0] + "'s Head", 1, 3);
        player.getInventory().addItem(item.setSkullOwner(args[0]).toItem());
        player.sendMessage(getCommandMessage("success", args[0]));
        return true;
    }
}
