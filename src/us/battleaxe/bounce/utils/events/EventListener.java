package us.battleaxe.bounce.utils.events;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public interface EventListener {
	
	public default void onEvent(Object sender, Object event) {
		Method[] methods = getClass().getMethods();
		for(Method method : methods)
		{
			Class[] parameterTypes = method.getParameterTypes();
			if(parameterTypes.length == 2 && parameterTypes[0] == Object.class && (parameterTypes[1] != Object.class && parameterTypes[1] == event.getClass()))
			{
				method.setAccessible(true);
				try { method.invoke(this, sender, event); }
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { e.printStackTrace(); }
			}
		}
	}
}
