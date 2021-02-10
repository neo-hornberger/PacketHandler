package me.neo_0815.packethandler;

import me.neo_0815.packethandler.PacketConstructionMode.PacketIdPair;
import me.neo_0815.packethandler.registry.PacketRegistry;
import me.neo_0815.packethandler.registry.UnaryPacketRegistry;

public class TestPacketConstruction {
	
	public static void main(final String[] args) {
		final UnaryPacketRegistry<PacketRegistry> reg = new UnaryPacketRegistry<>(new PacketRegistry());
		
		reg.registerPackets(TestPacketType.values());
		//reg.registerSystemPackets();
		
		final PacketConstructionMode pcm;
		
		pcm = PacketConstructionMode.DEFAULT;
		//pcm = new PacketConstructionMode(
		//		new PacketConstructionMode.EncodingStrategy().length((buf, length, id) -> buf.writeUnsignedVarInt(length + PacketConstructionMode.calcSVarNumLength(id))).id(ByteBuffer::writeVarLong).content(),
		//		new PacketConstructionMode.DecodingStrategy().length(ByteBuffer::readUnsignedVarInt).id(ByteBuffer::readVarLong).modifyLength(PacketConstructionMode.DecodingStrategy.ModifyLengthIdDecoding.ID_SVARNUM).content()
		//);
		
		final ByteBufferGenerator<ByteBuffer> bbg = ByteBufferGenerator.DEFAULT_GENERATOR;
		final PacketMap pm = PacketMap.of("name", "Neo");
		
		final ByteBuffer buf;
		
		//buf = pcm.encodePacket(bbg, 0, pm, reg);
		//buf = pcm.encodePacket(bbg, -3, pm, reg);
		//buf = pcm.encodePacket(bbg, new PacketWake(), -3, reg);
		buf = new ByteBuffer().writeInt(42).writeInt(300).writeString("Hello World!").writeInt(42);
		//buf = new ByteBuffer().writeLong(42).writeInt(300).writeString("Hello World!").writeInt(42);
		
		System.out.println(buf);
		
		final PacketIdPair pip = pcm.decodePacket(bbg, buf, reg);
		
		System.out.println(pip);
		System.out.println(pcm.encodePacket(bbg, pip.packet(), pip.id(), reg));
	}
}
