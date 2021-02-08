package me.neo_0815.packethandler;

import me.neo_0815.json.JSON;
import me.neo_0815.packethandler.bridge.Bridge;
import me.neo_0815.packethandler.bridge.IHostPortPairCreator.HostPortPair;
import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import java.io.IOException;
import java.util.UUID;

public class TestPacketHandler {
	public static final long START_TIME = System.currentTimeMillis();
	
	public static void main(final String[] args) throws IOException {
		//		final PacketConstructionMode conMode = PacketConstructionMode.createMode(PacketConstructionMode.LENGTH_VARINT_CONTENT_ID_VARINT, PacketConstructionMode.ID_VARINT, PacketConstructionMode.CONTENT);
		final PacketConstructionMode conMode = PacketConstructionMode.DEFAULT;
		
		final UnaryPacketRegistry<PacketRegistry> reg = new UnaryPacketRegistry<>(new PacketRegistry());
		reg.registerPackets(TestPacketType.values());
		
		reg.registerSystemPackets();
		
		final Properties prop = new Properties();
		prop.setPacketRegistry(reg);
		prop.setPacketConstructionMode(conMode);
		
		final Bridge bridge = new Bridge(8080, prop, new HostPortPair(8081), prop) {
			
			@Override
			protected void onClientConnected(final UUID client) {
				log("Bridge: %s", client);
			}
			
			@Override
			protected void onClientDisconnected(final UUID client) {
				log("Bridge: __ %s", client);
			}
			
			@Override
			protected PacketType onPacketReceived(final Side sender, final UUID client, final PacketBase<?> packet, final long id) {
				log("Bridge: %s %s -- %s", sender, client, packet);
				
				return createPacketType(packet, id);
			}
		};
		bridge.start();
		
		final Server server = new Server(8081, prop) {
			
			@Override
			protected void onClientConnected(final UUID client) {
				log("Server: %s", client);
			}
			
			@Override
			protected void onClientUUIDChanged(final UUID client, final UUID oldUUID) {
				log("Server: %s => %s", oldUUID, client);
			}
			
			@Override
			public void onPacketReceived(final UUID client, final PacketBase<?> packet, final long id) {
				log("Server: %s -- %s", client, packet);
				log("Server: %s", getClients());
				
				sendPacket(client, TestPacketType.PACKET, PacketMap.of("name", "§" + packet.toMap().getOrDefault("name", "")));
				
				//				log("Server: Disconnecting %s...", client);
				//				disconnectClient(client);
				
				//				changeClientUUID(client, UUID.randomUUID());
			}
		};
		server.start();
		
		final Client client = new Client("localhost", 8080, prop) {
			
			@Override
			protected void onPacketReceived(final PacketBase<?> packet, final long id) {
				log("Client: %s -- %s", getUuid(), packet);
				
				//				log("Client: Disconnecting...");
				//				disconnect();
				
				//				changeUUID(UUID.randomUUID());
			}
			
			@Override
			protected void onUUIDChanged(final UUID oldUUID, final UUID newUUID) {
				log("Client: %s => %s", oldUUID, newUUID);
			}
		};
		client.start();
		
		client.sendPacket(JSON.parseJSON("{\"name\":\"Neo\",\"age\":16}", TestPacketType.JSON.packetClass()), TestPacketType.JSON);
		//		client.sendPacket(TestPacketType.PACKET, TestPacketType.PACKET);
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
