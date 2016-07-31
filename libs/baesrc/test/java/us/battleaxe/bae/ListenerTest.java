package us.battleaxe.bae;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import us.battleaxe.bae.api.API;

public class ListenerTest implements Listener {
	
	private final API api;
	
	public ListenerTest(API api) {
		this.api = api;
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		PreparedStatement statement;
		try {
			statement = this.api.getMySQLClient().prepareStatement("SELECT `clicks` FROM t WHERE `name` = ?");
			statement.setString(1, e.getPlayer().getName());
			
			this.api.getMySQLClient().executeQueryAsynchronously(statement, new ThrowingConsumer<ResultSet>() {

				@Override
				public void accept(ResultSet t) {
					try {
						t.next();
						System.out.println(t.getInt(1));
					} catch (SQLException e) {
						e.printStackTrace();
					}			
				}
				@Override
				public void throwException(Exception e) {
					e.printStackTrace();
				}		
			});
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
}