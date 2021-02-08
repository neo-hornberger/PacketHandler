package me.neo_0815.packethandler;

import me.neo_0815.packethandler.packet.Packet;

public class TestPacket extends Packet {
	public String name;
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		name = buf.readString();
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeString(name);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		name = map.getOrDefault("name", "");
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("name", name);
	}
}
