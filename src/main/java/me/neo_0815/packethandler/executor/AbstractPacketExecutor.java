package me.neo_0815.packethandler.executor;

import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import java.util.UUID;
import java.util.function.Function;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
abstract class AbstractPacketExecutor {
	private final Function<UUID, AbstractPacketRegistry> registryFunction;
	
	public AbstractPacketExecutor(final Connection connection) {
		this(__ -> connection.registry());
	}
	
	public AbstractPacketExecutor(final Server server) {
		this(server::registry);
	}
	
	protected abstract void init();
	
	protected AbstractPacketRegistry packetRegistry() {
		return packetRegistry(null);
	}
	
	protected AbstractPacketRegistry packetRegistry(final UUID client) {
		return registryFunction.apply(client);
	}
	
	public final boolean execute(final PacketBase<?> packet, final long id) {
		return execute(null, packet, id);
	}
	
	public abstract boolean execute(final UUID client, final PacketBase<?> packet, final long id);
}
