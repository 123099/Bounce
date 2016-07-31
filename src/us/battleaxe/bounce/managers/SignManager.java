package us.battleaxe.bounce.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;

import us.battleaxe.bounce.utils.MathExtension;

public class SignManager {

	private GameManager gameManager;
	private Sign statusSign;
	
	public SignManager(GameManager gameManager, Sign sign) {
		this.gameManager = gameManager;
		statusSign = sign;
	}
	
	public void updateSignStatus() {
		if(statusSign == null)
			return;
		
		statusSign.setLine(0, gameManager.getInGamePlayerCount() + " / " + gameManager.getMaxPlayers());
		statusSign.setLine(1, gameManager.getGameStatus().toString());
		statusSign.setLine(2, MathExtension.Clamp(gameManager.getMinPlayers() - gameManager.getInGamePlayerCount(), 0, gameManager.getMinPlayers()) + " more needed");
		statusSign.update();
	}
	
	public static String signToConfig(Sign sign) {
		Location l = sign.getLocation();
		return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ(); 
	}
	
	public static Sign configToSign(String configValue) {
		String[] split = configValue.split(",");
		if(split.length != 4) 
			return null;
		
		Location signLocation = new Location(Bukkit.getServer().getWorld(split[0].trim()), Integer.parseInt(split[1].trim()), Integer.parseInt(split[2].trim()), Integer.parseInt(split[3].trim()));
		BlockState blockState = signLocation.getBlock().getState();
		if(blockState instanceof Sign)
			return (Sign) blockState;
		else
			return null;
	}
}
