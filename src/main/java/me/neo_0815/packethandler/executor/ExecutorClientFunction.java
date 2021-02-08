package me.neo_0815.packethandler.executor;

import me.neo_0815.packethandler.packet.PacketBase;

@FunctionalInterface
public interface ExecutorClientFunction<T extends PacketBase<?>> extends ExecutorClientBiFunction<T> {
	
	void accept(final T packet);
	
	@Override
	default void accept(final T packet, final long id) {
		accept(packet);
	}
}
