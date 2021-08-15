package me.neo_0815.packethandler;

import me.neo_0815.packethandler.ByteBuffer.ByteOrder;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

public class TestByteBuffer {
	private static final UUID uuid = UUID.randomUUID();
	private static final Instant instant = Instant.parse("2002-09-05T00:00:00.00Z");
	
	public static void main(final String[] args) {
		//		final ByteBuffer buf = new ByteBuffer(), buf2 = new ByteBuffer();
		//
		//		buf.writeVarInt(69);
		//		buf.writeVarInt(256);
		//		buf.writeVarInt(-1);
		//
		//		buf2.writeInt(69);
		//		buf2.writeInt(256);
		//		buf2.writeInt(-1);
		//
		//		System.out.println(buf);
		//		System.out.println(buf2);
		//		System.out.println();
		//
		//		System.out.println(buf.readVarInt());
		//		System.out.println(buf.readVarInt());
		//		System.out.println(buf.readVarInt());
		
		test(new Buffer());
		
		final Buffer buffer = new Buffer();
		
		buffer.byteOrder = ByteOrder.LITTLE_ENDIAN;
		
		test(buffer);
		
		final ByteBuffer buf = new ByteBuffer();
		final int vi = 64;
		final long vl = 420;
		final BigInteger vbi = BigInteger.valueOf(vl);
		
		System.out.println(vi);
		System.out.println(vl);
		System.out.println(vbi);
		
		buf.writeVarInt(vi);
		buf.writeVarLong(vl);
		buf.writeVarBigInt(vbi);
		
		System.out.println();
		System.out.println(buf);
		System.out.println();
		
		System.out.println(buf.readVarInt());
		System.out.println(buf.readVarLong());
		System.out.println(buf.readVarBigInt());
		
		System.out.println();
		System.out.println(buf);
	}
	
	private static void test(final ByteBuffer buf) {
		buf.writeShort(-1);
		buf.writeUnsignedByte(195);
		buf.writeUnsignedInt(Integer.MAX_VALUE + 1L);
		buf.writeBigInteger(BigInteger.valueOf(420));
		buf.writeBigDecimal(BigDecimal.valueOf(420d));
		buf.writeUUID(uuid);
		buf.writeInstant(instant);
		buf.writeString("🍩");
		
		System.out.println(buf);
		System.out.println(Arrays.toString(buf.toByteArray()));
		System.out.println("0x" + buf.toHexString());
		System.out.println("0o" + buf.toOctalString());
		System.out.println();
		
		System.out.println(buf.readUnsignedShort());
		System.out.println(buf.readUnsignedByte());
		System.out.println(buf.readUnsignedInt());
		System.out.println(buf.readBigInteger());
		System.out.println(buf.readBigDecimal());
		System.out.println(buf.readUUID());
		System.out.println(buf.readInstant());
		System.out.println(buf.readString());
		
		System.out.println();
		System.out.println(buf);
		System.out.println(Arrays.toString(buf.toByteArray()));
		System.out.println();
	}
	
	private static class Buffer extends ByteBuffer {
	}
}
