package me.neo_0815.packethandler.packet.system;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.packet.Packet;

public final class PacketPrimitiveMessage extends Packet {
	public String message;
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		message = buf.readString();
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeString(message);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		message = map.getOrDefault("message", "");
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("message", message);
	}
}
