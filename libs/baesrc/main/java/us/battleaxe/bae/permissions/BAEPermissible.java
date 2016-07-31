package us.battleaxe.bae.permissions;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;

//Maybe it would increase performance to fill this class with Bukkit's code and remove unnecessary calls to #recalculatePermissions()
//I'm leaving it how it is at the moment. Maybe we will have to change it later.
public class BAEPermissible extends PermissibleBase {

	private final PermissionAttachment permissionContainer;
	private volatile boolean hasWildcard;

	public BAEPermissible(Player player, PermissionAttachment permissionContainer) {
		super(player);
		this.permissionContainer = permissionContainer;
	}

	@Override
	public synchronized boolean isPermissionSet(String name) {
		if(name.equals("*") && this.hasWildcard) return true; 
		return super.isPermissionSet(name);
	}

	@Override
	public synchronized boolean isPermissionSet(Permission perm) {
		return super.isPermissionSet(perm);
	}

	@Override
	public synchronized boolean hasPermission(String inName) {
		if(inName == null) {
			throw new IllegalArgumentException("Permission name cannot be null");
		}

		if(inName.equals("*")) {
			return this.hasWildcard;
		} else {
			if(isPermissionSet(inName)) {
				return super.hasPermission(inName);
			} else {
				return this.hasWildcard;
			}		
		}
	}

	@Override
	public synchronized boolean hasPermission(Permission perm) {
		if(perm.getName().equals("*")) {
			return this.hasWildcard;
		} else {
			if(isPermissionSet(perm)) {
				return super.hasPermission(perm);
			} else {
				return this.hasWildcard;
			}		
		}
	}

	@Override
	public synchronized PermissionAttachment addAttachment(Plugin plugin, String name, boolean value) {
		return super.addAttachment(plugin, name, value);
	}
	
	@Override
	public synchronized PermissionAttachment addAttachment(Plugin plugin) {
		return super.addAttachment(plugin);
	}

	@Override
	public synchronized void removeAttachment(PermissionAttachment attachment) {
		super.removeAttachment(attachment);
	}
	
	@Override
	public synchronized void recalculatePermissions() {
		super.recalculatePermissions();
	}

	@Override
	public synchronized void clearPermissions() {
		if(this.permissionContainer != null) {
			for(String key : this.permissionContainer.getPermissions().keySet()) {
				this.permissionContainer.unsetPermission(key);
			}
		}
		super.clearPermissions();
	}
	
	@Override
	public synchronized PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks) {
		return super.addAttachment(plugin, name, value, ticks);
	}
	
	@Override
	public synchronized PermissionAttachment addAttachment(Plugin plugin, int ticks) {
		return super.addAttachment(plugin, ticks);
	}
	
	@Override
	public synchronized Set<PermissionAttachmentInfo> getEffectivePermissions() {
		return super.getEffectivePermissions();
	}

	public void setWildcard(boolean hasWildcard) {
		this.hasWildcard = hasWildcard;
	}
	
	public boolean hasWildcard() {
		return this.hasWildcard;
	}
}