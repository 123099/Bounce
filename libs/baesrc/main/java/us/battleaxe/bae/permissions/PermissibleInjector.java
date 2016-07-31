package us.battleaxe.bae.permissions;

import java.lang.reflect.Field;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;

import us.battleaxe.bae.Util;

public class PermissibleInjector {
	
	private final String version;
	private final Class<?> classCraftEntity;
	
	public PermissibleInjector() throws Exception {
		this.version = Util.retrieveBukkitVersion();
		this.classCraftEntity = Class.forName("org.bukkit.craftbukkit." + this.version + ".entity.CraftHumanEntity");
	}

	public void inject(Player player, PermissibleBase permissible) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
		Field fieldPerm = this.classCraftEntity.getDeclaredField("perm");
		fieldPerm.setAccessible(true);
		fieldPerm.set(player, permissible);
	}
}