package me.neo_0815.packethandler;

import me.neo_0815.packethandler.executor.PacketExecutor;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import lombok.SneakyThrows;

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
		
		final TestArrayPacket tap = new TestArrayPacket();
		tap.uuids = new UUID[2];
		tap.uuids[0] = UUID.randomUUID();
		tap.ints = new int[2];
		tap.ints[0] = 42;
		tap.ints[1] = 69;
		System.out.println(tap);
	}
	
	public static void wow(final TestEmptyPacket packet, final long id) {
		System.out.println(packet + "\t" + id);
	}
}
