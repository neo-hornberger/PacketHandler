package me.neo_0815.packethandler;

import me.neo_0815.packethandler.packet.Packet;

import java.util.UUID;

public class TestArrayPacket extends Packet {
	public UUID[] uuids = new UUID[0];
	public Integer[] ints = new Integer[0];
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		uuids = buf.readArray(UUID[]::new, ByteBuffer::readUUID);
		ints = buf.readArray(Integer[]::new, ByteBuffer::readVarInt);
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeArray(uuids, ByteBuffer::writeUUID);
		buf.writeArray(ints, ByteBuffer::writeVarInt);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		uuids = map.getOrDefault("uuids", () -> new UUID[0]);
		ints = map.getOrDefault("ints", () -> new Integer[0]);
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("uuids", uuids);
		map.put("ints", ints);
	}
}
