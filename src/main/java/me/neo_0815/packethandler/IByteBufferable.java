package me.neo_0815.packethandler;

public interface IByteBufferable<B extends ByteBuffer> {
	
	/**
	 * Reads data from the {@link ByteBuffer} 'buf'.
	 *
	 * @param buf the {@link ByteBuffer}
	 */
	void fromBuffer(final B buf);
	
	/**
	 * Writes data to the {@link ByteBuffer} 'buf'.
	 *
	 * @param buf the {@link ByteBuffer}
	 */
	void toBuffer(final B buf);
}
