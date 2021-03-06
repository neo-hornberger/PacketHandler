package me.neo_0815.packethandler.server;

import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.PacketDisconnect;
import me.neo_0815.packethandler.packet.system.PacketWake;
import me.neo_0815.packethandler.packet.system.SystemPacketType;

import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class ClientConnection extends Connection {
	private final Server server;
	
	@EqualsAndHashCode.Include
	private UUID uuid;
	
	long lastPacket;
	int clearingCount;
	
	public ClientConnection(final Server server, final Socket socket, final UUID uuid, final Properties properties) throws IOException {
		super(socket, properties);
		
		this.server = server;
		this.uuid = uuid;
		
		resetClearing();
	}
	
	protected void changeUUID(final UUID uuid) {
		this.uuid = uuid;
		
		sendPacket(SystemPacketType.CHANGE_UUID, PacketMap.of("uuid", uuid));
	}
	
	@Override
	protected void onPacketReceived(final PacketBase<?> packet, final long id) {
		resetClearing();
		
		server.onPacketReceived(uuid, packet, id);
	}
	
	@Override
	protected void onSystemPacketReceived(final PacketBase<?> packet) {
		if(packet instanceof PacketDisconnect) stop();
		else if(packet instanceof PacketWake) resetClearing();
		
		server.onSystemPacketReceived(uuid, packet);
	}
	
	@Override
	protected void onUnknownPacketReceived(final UnknownPacket packet) {
		server.onUnknownPacketReceived(uuid, packet);
	}
	
	@Override
	protected void onMessageReceived(final String message) {
		resetClearing();
		
		server.onMessageReceived(uuid, message);
	}
	
	private void resetClearing() {
		lastPacket = System.currentTimeMillis();
		clearingCount = 0;
	}
	
	@Override
	public String toString() {
		return "ClientConnection" + super.toString();
	}
}
