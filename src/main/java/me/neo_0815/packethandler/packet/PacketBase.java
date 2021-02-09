package me.neo_0815.packethandler.packet;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.IByteBufferable;
import me.neo_0815.packethandler.PacketMap;

public abstract class PacketBase<B extends ByteBuffer> implements IByteBufferable<B> {
	
	/**
	 * Gets data from the {@link PacketMap} 'map'.
	 *
	 * @param map the {@link PacketMap}
	 */
	public abstract void fromMap(final PacketMap map);
	
	/**
	 * Puts data into the {@link PacketMap} 'map'.
	 *
	 * @param map the {@link PacketMap}
	 */
	public abstract void intoMap(final PacketMap map);
	
	/**
	 * Returns data within a {@link PacketMap}.
	 *
	 * @return a {@link PacketMap} filled with the data
	 */
	public final PacketMap toMap() {
		final PacketMap map = PacketMap.of();
		
		intoMap(map);
		
		return map;
	}
	
	@Override
	public String toString() {
		return getClassName() + "[data=" + toMap() + "]";
	}
	
	protected final String getClassName() {
		return getClass().getSimpleName();
	}
}
