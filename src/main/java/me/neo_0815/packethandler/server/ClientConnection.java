package me.neo_0815.packethandler.server;

import lombok.EqualsAndHashCode;
import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.PacketConnect;
import me.neo_0815.packethandler.packet.system.PacketDisconnect;
import me.neo_0815.packethandler.packet.system.PacketWake;
import me.neo_0815.packethandler.packet.system.SystemPacketType;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public final class ClientConnection extends Connection {
	private final Server server;
	
	@EqualsAndHashCode.Include
	private UUID uuid;
	
	long lastPacket;
	
	public ClientConnection(final Server server, final Socket client, final UUID uuid, final Properties properties) throws IOException {
		super(client != null ? client.getOutputStream() : null, properties);
		
		this.server = server;
		this.uuid = uuid;
		
		initThreads(client);
	}
	
	protected void changeUUID(final UUID uuid) {
		this.uuid = uuid;
		
		sendPacket(SystemPacketType.CHANGE_UUID, PacketMap.of("uuid", uuid));
	}
	
	@Override
	protected void onPacketReceived(final PacketBase<?> packet, final long id) {
		resetLastPacket();
		
		server.onPacketReceived(uuid, packet, id);
	}
	
	@Override
	protected void onSystemPacketReceived(final PacketBase<?> packet) {
		if(packet instanceof PacketDisconnect) stop();
		else if(packet instanceof PacketWake || packet instanceof PacketConnect) resetLastPacket();
		
		server.onSystemPacketReceived(uuid, packet);
	}
	
	@Override
	protected void onUnknownPacketReceived(final UnknownPacket packet) {
		server.onUnknownPacketReceived(uuid, packet);
	}
	
	@Override
	protected void onMessageReceived(final String message) {
		resetLastPacket();
		
		server.onMessageReceived(uuid, message);
	}
	
	private void resetLastPacket() {
		lastPacket = System.currentTimeMillis();
	}
}
