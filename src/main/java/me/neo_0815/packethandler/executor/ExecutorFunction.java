package me.neo_0815.packethandler.executor;

import me.neo_0815.packethandler.packet.PacketBase;

import java.util.UUID;

@FunctionalInterface
public interface ExecutorFunction<T extends PacketBase<?>> {
	
	void accept(final UUID client, final T packet, final long id);
}
