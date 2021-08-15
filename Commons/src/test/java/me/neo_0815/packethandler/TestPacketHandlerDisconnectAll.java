package me.neo_0815.packethandler;

import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import lombok.SneakyThrows;

import java.io.IOException;
import java.util.UUID;

public class TestPacketHandlerDisconnectAll {
	
	private static final long START_TIME = System.currentTimeMillis();
	
	public static void main(final String[] args) throws IOException {
		final PacketConstructionMode conMode = PacketConstructionMode.DEFAULT;
		
		final UnaryPacketRegistry<PacketRegistry> reg = new UnaryPacketRegistry<>(new PacketRegistry());
		reg.registerPackets(TestPacketType.values());
		
		reg.registerSystemPackets();
		
		final Properties prop = new Properties();
		prop.setPacketRegistry(reg);
		prop.setPacketConstructionMode(conMode);
		prop.setExceptionHandlingType(Properties.ExceptionHandlingType.STOP);
		
		final Server server = new Server(8080, prop) {
			
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
				
				if(packet instanceof TestPacket) {
					stop();
				}
			}
			
			@Override
			protected void onSystemPacketReceived(final UUID client, final PacketBase<?> packet) {
				log("Server: %s -- %s", client, packet);
				
				super.onSystemPacketReceived(client, packet);
			}
		};
		server.start();
		
		startClient(prop);
		startClient(prop);
		startClient(prop);
		
		sleep(1000);
		
		server.broadcastPacket(TestPacketType.PACKET);
	}
	
	@SneakyThrows
	private static void startClient(final Properties prop) {
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
				
				if(packet instanceof TestPacket) {
					sendPacket(TestPacketType.PACKET);
				}
			}
			
			@Override
			protected void onSystemPacketReceived(final PacketBase<?> packet) {
				log("Client: %s -- %s", getUuid(), packet);
				
				super.onSystemPacketReceived(packet);
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
	
	public static void sleep(final long millis) {
		try {
			Thread.sleep(millis);
		}catch(final InterruptedException e) {
			e.printStackTrace();
		}
	}
}
