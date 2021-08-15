package me.neo_0815.packethandler.executor;

import me.neo_0815.packethandler.packet.PacketBase;

import java.util.UUID;

@FunctionalInterface
public interface ExecutorServerFunction<T extends PacketBase<?>> extends ExecutorFunction<T> {
	
	void accept(final UUID client, final T packet);
	
	@Override
	default void accept(final UUID client, final T packet, final long id) {
		accept(client, packet);
	}
}
