package me.neo_0815.packethandler.packet;

import lombok.RequiredArgsConstructor;
import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.PacketMap;

import java.util.Arrays;

@RequiredArgsConstructor
public final class UnknownPacket extends Packet {
	public static final long ID = Long.MIN_VALUE;
	
	public final long id;
	public final int length;
	
	public byte[] bytes = new byte[0];
	
	public UnknownPacket() {
		this(ID);
	}
	
	public UnknownPacket(final long id) {
		this(id, -1);
	}
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		bytes = buf.readAll();
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.write(bytes);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		bytes = map.getOrDefault("bytes", () -> new byte[0]);
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("bytes", bytes);
	}
	
	@Override
	public String toString() {
		return "UnknownPacket[id=" + id + ", length=" + length + ", bytes=" + Arrays.toString(bytes) + "]";
	}
}
