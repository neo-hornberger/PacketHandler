package me.neo_0815.packethandler.packet.system;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.packet.Packet;

public final class PacketWake extends Packet {
	public int count;
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		count = buf.readUnsignedVarInt();
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeUnsignedVarInt(count);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		count = map.getOrDefault("count", 0);
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("count", count);
	}
}
