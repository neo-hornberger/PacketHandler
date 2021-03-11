package me.neo_0815.packethandler;

public interface IByteBufferable<B extends ByteBuffer> {
	
	/**
	 * Reads data from the {@link ByteBuffer} {@code buf}.
	 *
	 * @param buf the {@link ByteBuffer} to read from
	 */
	void fromBuffer(final B buf);
	
	/**
	 * Writes data to the {@link ByteBuffer} {@code buf}.
	 *
	 * @param buf the {@link ByteBuffer} to write to
	 */
	void toBuffer(final B buf);
}
