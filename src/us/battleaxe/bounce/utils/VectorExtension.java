package us.battleaxe.bounce.utils;

import org.bukkit.util.Vector;

public final class VectorExtension {

	public static Vector project(Vector vectorToProject, Vector projectOn) {
		return projectOn.multiply(vectorToProject.dot(projectOn) / projectOn.lengthSquared());
	}
	
	public static Vector reflectAlong(Vector vectorToReflect, Vector axis, float bounciness)
	{
		return vectorToReflect.subtract(project(vectorToReflect, axis).multiply(1 + bounciness));
	}
}
