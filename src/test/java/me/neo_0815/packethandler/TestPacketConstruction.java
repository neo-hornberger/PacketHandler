package me.neo_0815.packethandler;

import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;

public class TestPacketConstruction {
	
	public static void main(final String[] args) {
		final UnaryPacketRegistry<PacketRegistry> reg = new UnaryPacketRegistry<>(new PacketRegistry());
		
		reg.registerPackets(TestPacketType.values());
		
		final PacketConstructionMode pcm = PacketConstructionMode.DEFAULT;
		final PacketMap pm = PacketMap.of();
		
		System.out.println(pcm.encodePacket(ByteBufferGenerator.DEFAULT_GENERATOR, 0, pm, reg));
	}
}
