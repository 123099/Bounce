package us.battleaxe.bae;

import java.util.function.Consumer;

public interface ThrowingConsumer<T> extends Consumer<T> {

	public void throwException(Exception exception);
	
}
