package me.neo_0815.packethandler;

@FunctionalInterface
public interface ByteBufferGenerator<B extends ByteBuffer> {
	ByteBufferGenerator<ByteBuffer> DEFAULT_GENERATOR = ByteBuffer::new;
	
	B generate();
	
	static <B extends ByteBuffer> ByteBufferGenerator<B> nullGenerator() {
		return () -> null;
	}
}
