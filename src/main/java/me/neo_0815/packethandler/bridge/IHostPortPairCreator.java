package me.neo_0815.packethandler.bridge;

import lombok.Value;
import lombok.experimental.Tolerate;

import java.util.function.Function;
import java.util.function.ToIntFunction;

@FunctionalInterface
public interface IHostPortPairCreator<T> {
	
	HostPortPair create(T obj);
	
	static <T> IHostPortPairCreator<T> create(final Function<T, String> hostFunction, final ToIntFunction<T> portFunction) {
		return obj -> new HostPortPair(hostFunction.apply(obj), portFunction.applyAsInt(obj));
	}
	
	static <T> IHostPortPairCreator<T> create(final String host, final ToIntFunction<T> portFunction) {
		return obj -> new HostPortPair(host, portFunction.applyAsInt(obj));
	}
	
	static <T> IHostPortPairCreator<T> create(final Function<T, String> hostFunction, final int port) {
		return obj -> new HostPortPair(hostFunction.apply(obj), port);
	}
	
	static <T> IHostPortPairCreator<T> create(final ToIntFunction<T> portFunction) {
		return obj -> new HostPortPair(portFunction.applyAsInt(obj));
	}
	
	@Value
	class HostPortPair {
		String host;
		int port;
		
		@Tolerate
		public HostPortPair(final int port) {
			this(null, port);
		}
	}
}
