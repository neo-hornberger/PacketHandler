package me.neo_0815.packethandler;

import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.protocol.IProtocol;
import me.neo_0815.packethandler.protocol.Protocol;
import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import lombok.SneakyThrows;

import java.util.UUID;

public class TestProtocol {
	
	@SneakyThrows
	public static void main(final String[] args) {
		final UnaryPacketRegistry<PacketRegistry> reg = new UnaryPacketRegistry<>(new PacketRegistry());
		
		reg.registerPackets(TestPacketType.values());
		
		final Server server = new Server(8080, new Properties().setPacketRegistry(reg)) {
			
			@Override
			protected void onPacketReceived(final UUID client, final PacketBase<?> packet, final long id) {
			}
		};
		
		final Protocol<ProtocolStates> executor = new Protocol<>(server, ProtocolStates.PHASE_1) {
			
			@Override
			protected void init() {
				register(ProtocolStates.PHASE_1, ProtocolStates.PHASE_2, TestPacketType.EMPTY, TestPacketExecutor::wow);
			}
		};
		
		System.out.println(executor.execute(null, 1));
		System.out.println(executor.execute(null, 1));
	}
	
	public static void wow(final TestEmptyPacket packet, final long id) {
		System.out.println(packet + "\t" + id);
	}
	
	private enum ProtocolStates implements IProtocol<ProtocolStates> {
		PHASE_1,
		PHASE_2,
		PHASE_3
	}
}
