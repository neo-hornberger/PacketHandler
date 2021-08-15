package me.neo_0815.packethandler.schema;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.ByteBuffer.ByteOrder;

public interface EndianSchema<T, B extends ByteBuffer> extends Schema<T, B> {
	
	@Override
	default void fromBuffer(final T obj, final B buf) {
		fromBuffer(obj, buf, buf.byteOrder);
	}
	
	void fromBuffer(final T obj, final B buf, final ByteOrder order);
	
	@Override
	default void toBuffer(final T obj, final B buf) {
		toBuffer(obj, buf, buf.byteOrder);
	}
	
	void toBuffer(T obj, B buf, ByteOrder order);
}
