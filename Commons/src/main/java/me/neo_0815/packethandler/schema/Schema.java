package me.neo_0815.packethandler.schema;

import me.neo_0815.packethandler.ByteBuffer;

public interface Schema<T, B extends ByteBuffer> {
	
	/**
	 * Reads data from the {@link ByteBuffer} {@code buf}.
	 *
	 * @param buf the {@link ByteBuffer} to read from
	 */
	void fromBuffer(final T obj, final B buf);
	
	/**
	 * Writes data to the {@link ByteBuffer} {@code buf}.
	 *
	 * @param buf the {@link ByteBuffer} to write to
	 */
	void toBuffer(final T obj, final B buf);
	
	/**
	 * Creates a new instance of the contained type.
	 *
	 * @return the newly created instance
	 */
	T newInstance();
}
