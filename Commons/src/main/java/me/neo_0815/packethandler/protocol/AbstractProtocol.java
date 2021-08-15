package me.neo_0815.packethandler.protocol;

import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.executor.AbstractPacketExecutor;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.server.Server;

import java.util.UUID;

public abstract class AbstractProtocol<P extends IProtocol<P>> extends AbstractPacketExecutor {
	
	public AbstractProtocol(final Connection connection) {
		super(connection);
	}
	
	public AbstractProtocol(final Server server) {
		super(server);
	}
	
	protected void onStateChangeError(final P state, final UUID client, final PacketBase<?> packet, final long id) {
	}
}
