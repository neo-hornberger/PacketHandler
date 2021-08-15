package me.neo_0815.packethandler.executor;

import me.neo_0815.packethandler.packet.PacketBase;

import java.util.UUID;

@FunctionalInterface
public interface ExecutorClientBiFunction<T extends PacketBase<?>> extends ExecutorFunction<T> {
	
	void accept(final T packet, final long id);
	
	@Override
	default void accept(final UUID client, final T packet, final long id) {
		accept(packet, id);
	}
}
