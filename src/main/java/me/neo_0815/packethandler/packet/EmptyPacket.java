package me.neo_0815.packethandler.packet;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.PacketMap;

public abstract class EmptyPacket extends Packet {
	
	@Override
	public final void fromBuffer(final ByteBuffer buf) {
	}
	
	@Override
	public final void toBuffer(final ByteBuffer buf) {
	}
	
	@Override
	public final void fromMap(final PacketMap map) {
	}
	
	@Override
	public final void intoMap(final PacketMap map) {
	}
	
	@Override
	public String toString() {
		return getClassName() + "[empty]";
	}
}
