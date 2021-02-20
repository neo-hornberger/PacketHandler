package me.neo_0815.packethandler.packet;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.PacketMap;

import java.util.UUID;

public abstract class PacketUUID extends Packet {
	public UUID uuid;
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		uuid = buf.readUUID();
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeUUID(uuid);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		uuid = map.getOrDefault("uuid", UUID.randomUUID());
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("uuid", uuid);
	}
}
