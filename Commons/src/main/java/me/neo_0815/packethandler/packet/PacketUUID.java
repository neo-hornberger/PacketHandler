package me.neo_0815.packethandler.packet;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.PacketMap;

import java.util.UUID;

public abstract class PacketUUID<B extends ByteBuffer> extends PacketBase<B> {
	public UUID uuid;
	
	@Override
	public void fromBuffer(final B buf) {
		uuid = buf.readUUID();
	}
	
	@Override
	public void toBuffer(final B buf) {
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
