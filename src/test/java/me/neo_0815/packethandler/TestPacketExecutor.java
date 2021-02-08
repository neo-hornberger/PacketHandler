package me.neo_0815.packethandler;

import lombok.SneakyThrows;
import me.neo_0815.packethandler.executor.PacketExecutor;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import java.util.UUID;

public class TestPacketExecutor {
	
	@SneakyThrows
	public static void main(final String[] args) {
		final UnaryPacketRegistry<PacketRegistry> reg = new UnaryPacketRegistry<>(new PacketRegistry());
		
		reg.registerPackets(TestPacketType.values());
		
		final Server server = new Server(8080, new Properties().setPacketRegistry(reg)) {
			
			@Override
			protected void onPacketReceived(final UUID client, final PacketBase<?> packet, final long id) {
			}
		};
		
		final PacketExecutor executor = new PacketExecutor(server) {
			
			@Override
			protected void init() {
				register(TestPacketType.EMPTY, TestPacketExecutor::wow);
			}
		};
		
		System.out.println(executor.execute(null, 2));
	}
	
	public static void wow(final TestEmptyPacket packet, final long id) {
		System.out.println(packet + "\t" + id);
	}
}
