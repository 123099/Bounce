package us.battleaxe.bae;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class ListenerWeather implements Listener {

	private final boolean noWeather;
	
	public ListenerWeather(boolean noWeather) {
		this.noWeather = noWeather;
	}
	
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent e) {
		if(this.noWeather) e.setCancelled(true);
	}
}