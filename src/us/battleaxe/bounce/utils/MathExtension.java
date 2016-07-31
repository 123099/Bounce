package us.battleaxe.bounce.utils;

public final class MathExtension {

	public static int Clamp(int value, int min, int max) {
		if(value < min) return min;
		if(value > max) return max;
		return value;
	}
}
