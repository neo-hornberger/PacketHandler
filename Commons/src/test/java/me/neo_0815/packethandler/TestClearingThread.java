package me.neo_0815.packethandler;

import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.system.PacketWake;
import me.neo_0815.packethandler.packet.system.SystemPacketType;
import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import java.io.IOException;
import java.util.UUID;

public class TestClearingThread {
	private static final long START_TIME = System.currentTimeMillis();
	
	public static void main(final String[] args) throws IOException {
		final PacketConstructionMode conMode = PacketConstructionMode.DEFAULT;
		
		final UnaryPacketRegistry<PacketRegistry> reg = new UnaryPacketRegistry<>(new PacketRegistry());
		reg.registerPackets(TestPacketType.values());
		
		reg.registerSystemPackets();
		
		final Properties prop = new Properties();
		prop.setPacketRegistry(reg);
		prop.setPacketConstructionMode(conMode);
		prop.setClearingInterval(1_000L);
		
		final Server server = new Server(8080, prop.copy()) {
			
			@Override
			protected void onClientConnected(final UUID client) {
				log("Server: %s", client);
			}
			
			@Override
			protected void onClientDisconnected(final UUID client) {
				log("Server: __ %s", client);
			}
			
			@Override
			protected void onPacketReceived(final UUID client, final PacketBase<?> packet, final long id) {
				log("Server: %s -- %s", client, packet);
				log("Server: %s", getClients());
			}
			
			@Override
			protected void onSystemPacketReceived(final UUID client, final PacketBase<?> packet) {
				super.onSystemPacketReceived(client, packet);
				
				log("Server: %s ~~ %s", client, packet);
			}
		};
		server.start();
		
		prop.setClearingEnabled(false);
		
		final Client client = new Client("localhost", 8080, prop) {
			
			@Override
			protected void onConnected() {
				log("Client: %s", getUuid());
			}
			
			@Override
			protected void onDisconnected() {
				log("Client: __ %s", getUuid());
			}
			
			@Override
			protected void onPacketReceived(final PacketBase<?> packet, final long id) {
				log("Client: %s -- %s", getUuid(), packet);
			}
			
			@Override
			protected void onSystemPacketReceived(final PacketBase<?> packet) {
				super.onSystemPacketReceived(packet);
				
				if(packet instanceof PacketWake && ((PacketWake) packet).count == 5) {
					sendPacket(SystemPacketType.WAKE);
				}
				
				log("Client: %s ~~ %s", getUuid(), packet);
			}
		};
		client.start();
	}
	
	public static void log(final Object obj) {
		log0(obj.toString());
	}
	
	public static void log(final String msg, final Object... args) {
		log0(msg, args);
	}
	
	private static void log0(final String msg, final Object... args) {
		System.out.printf("[" + (System.currentTimeMillis() - START_TIME) + "] " + msg + "%n", args);
	}
}
