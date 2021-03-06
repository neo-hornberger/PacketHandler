package me.neo_0815.packethandler;

import me.neo_0815.packethandler.packet.Packet;

import java.util.UUID;

public class TestArrayPacket extends Packet {
	public UUID[] uuids = new UUID[0];
	public int[] ints = new int[0];
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		uuids = new UUID[buf.readUnsignedVarInt()];
		
		for(int i = 0; i < uuids.length; i++)
			uuids[i] = buf.readUUID();
		
		ints = new int[buf.readUnsignedVarInt()];
		
		for(int i = 0; i < ints.length; i++)
			ints[i] = buf.readVarInt();
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeUnsignedVarInt(uuids.length);
		
		for(final UUID uuid : uuids)
			buf.writeUUID(uuid);
		
		buf.writeUnsignedVarInt(uuids.length);
		
		for(final int i : ints)
			buf.writeVarInt(i);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		uuids = map.getOrDefault("uuids", () -> new UUID[0]);
		ints = map.getOrDefault("ints", () -> new int[0]);
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("uuids", uuids);
		map.put("ints", ints);
	}
}
