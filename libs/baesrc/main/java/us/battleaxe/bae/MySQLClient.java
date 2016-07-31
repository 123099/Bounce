package us.battleaxe.bae;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

import org.bukkit.Bukkit;

public class MySQLClient {
	
	private final BAEssentials owner;
	private final String host;
	private final int port;
	private final String user;
	private final String password;
	private final String database;
	
	private boolean closed;
	
	private ExecutorService executor;
	private Connection connection;
	
	public MySQLClient(BAEssentials owner, String host, int port, String user, String password, String database) throws Exception {
		this.owner = owner;
		this.host = host;
		this.port = port;
		this.user = user;
		this.password = password;
		this.database = database;
		
		Class.forName("com.mysql.jdbc.Driver");
		this.executor = Executors.newCachedThreadPool();
	}
	
	public void openConnection() throws SQLException {
		this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true", this.user, this.password);
	}
	
	public void closeConnection() {
		this.closed = true;
		try {
			this.connection.close();
		} catch (SQLException e) {
			this.owner.getLogger().log(Level.WARNING, "Error whilst closing connection", e);
		}
	}
	
	public PreparedStatement prepareStatement(String statement) throws SQLException {
		return prepareStatement(statement, false);
	}
	
	public PreparedStatement prepareStatement(String statement, boolean returnGeneratedKeys) throws SQLException {
		reconnectIfNecessary();
		PreparedStatement ps = this.connection.prepareStatement(statement, returnGeneratedKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);
		ps.setQueryTimeout(1);
		return ps;
	}
	
	public ResultSet executeQuery(PreparedStatement statement) throws SQLException, SQLTimeoutException {
		reconnectIfNecessary();
		return statement.executeQuery();
	}
	
	public void executeQueryAsynchronously(final PreparedStatement statement, final ThrowingConsumer<ResultSet> consumer) {
		reconnectIfNecessary();
		this.executor.execute(new Runnable() {		
			@Override
			public void run() {
				try {
					ResultSet rs = statement.executeQuery();
					consumer.accept(rs);
				} catch (Exception e) {
					consumer.throwException(e);
				}
			}
		});
	}
	
	public int executeUpdate(PreparedStatement statement) throws SQLException, SQLTimeoutException {
		reconnectIfNecessary();
		return statement.executeUpdate();
	}
	
	public void executeUpdateAsynchronously(final PreparedStatement statement, final ThrowingConsumer<Integer> consumer) {
		reconnectIfNecessary();
		this.executor.execute(new Runnable() {		
			@Override
			public void run() {
				try {
					int result = statement.executeUpdate();
					consumer.accept(result);
				} catch (Exception e) {
					consumer.throwException(e);
				}
			}
		});
	}
	
	public boolean reconnectIfNecessary() {
		if(this.closed) {
			throw new IllegalStateException("Connection already closed!");
		}
		if(!isValid()) {
			this.owner.getLogger().warning("MySQL-Connection died! Trying to reconnect...");
			try {
				openConnection();
				this.owner.getLogger().info("Reconnected!");
				return true;
			} catch (SQLException e) {
				this.owner.getLogger().log(Level.SEVERE, "Failed to reconnect to MySQL! Making server inaccessible and trying to reconnect again in 15 seconds!", e);
				BAEssentials.setAccessible(false);
				Bukkit.getScheduler().runTaskLater(this.owner, new Runnable() {
					@Override
					public void run() {
						try {
							openConnection();
							owner.getLogger().info("Reconnected to MySQL!");
							BAEssentials.setAccessible(true);
						} catch (SQLException e) {
							owner.getLogger().log(Level.SEVERE, "Failed to reconnect to MySQL again! Stopping server...", e);
							Bukkit.shutdown();
						}
					}
				}, 15 * 20L);
				return false;
			}
		}
		return true;
	}
	
	public boolean isValid() {
		try {
			return !(this.connection == null || this.connection.isClosed());
		} catch (SQLException e) {
			//Can't really happen...
			this.owner.getLogger().log(Level.WARNING, "Invalid connection!", e);
			return false;
		}
	}
}