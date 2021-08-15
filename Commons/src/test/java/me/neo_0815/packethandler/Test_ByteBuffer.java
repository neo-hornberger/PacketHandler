package me.neo_0815.packethandler;

import me.neo_0815.packethandler.RandomValueSource.ValueType;
import me.neo_0815.packethandler.packet.PacketBase;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class Test_ByteBuffer {
	private static final String METHODS = "me.neo_0815.packethandler.Test_ValueMethods#";
	private static final Random RAND = new Random();
	
	private ByteBuffer buffer;
	
	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	public class Constructors {
		
		private Stream<ByteBuffer> emptyBuffers() {
			return Stream.of(
					new ByteBuffer(),
					ByteBuffer.empty()
			);
		}
		
		@ParameterizedTest
		@MethodSource("emptyBuffers")
		public void construct_empty(final ByteBuffer buffer) {
			assertEquals(0, buffer.getSize());
		}
	}
	
	@BeforeAll
	public static void startTests() {
		System.out.println(Charset.defaultCharset());
	}
	
	@BeforeEach
	public void generateEmptyBuffer() {
		buffer = new ByteBuffer();
	}
	
	@ParameterizedTest
	@ValueSource(bytes = { Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE })
	@RandomValueSource(type = ValueType.BYTE, count = 5)
	public void _byte(final byte b) {
		assertDoesNotThrow(() -> buffer.write(b));
		assertEquals(Byte.BYTES, buffer.getSize());
		assertEquals(b, assertDoesNotThrow(() -> buffer.read()));
	}
	
	@ParameterizedTest
	@ValueSource(ints = { Byte.MIN_VALUE, -1, 0, 1, Byte.MAX_VALUE })
	@RandomValueSource(type = ValueType.BYTE, count = 5)
	public void _byte_i(final int b) {
		assertDoesNotThrow(() -> buffer.write(b));
		assertEquals(Byte.BYTES, buffer.getSize());
		assertEquals((byte) b, assertDoesNotThrow(() -> buffer.read()));
	}
	
	@ParameterizedTest
	@ValueSource(ints = { Integer.MIN_VALUE, Byte.MIN_VALUE - 1, Byte.MAX_VALUE + 1, Integer.MAX_VALUE })
	public void _byte_i__fails(final int b) {
		assertDoesNotThrow(() -> buffer.write(b));
		assertEquals(Byte.BYTES, buffer.getSize());
		assertNotEquals(b, (int) assertDoesNotThrow(() -> buffer.read()));
	}
	
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, 2, 5, 10, 20, 50, 100, 200, 400, 500, 1000, 2000 })
	public void _byte_array(final int length) {
		final byte[] bytes = new byte[length];
		
		RAND.nextBytes(bytes);
		
		assertDoesNotThrow(() -> buffer.write(bytes));
		assertEquals(length, buffer.getSize());
		assertArrayEquals(bytes, assertDoesNotThrow(() -> buffer.read(length)));
	}
	
	// TODO byte array with n items
	
	@ParameterizedTest
	@ValueSource(shorts = { Short.MIN_VALUE, -1, 0, 1, Short.MAX_VALUE })
	@RandomValueSource(type = ValueType.SHORT, count = 5)
	public void _short(final short s) {
		assertDoesNotThrow(() -> buffer.writeShort(s));
		assertEquals(Short.BYTES, buffer.getSize());
		assertEquals(s, assertDoesNotThrow(() -> buffer.readShort()));
	}
	
	// TODO write short as int
	
	@ParameterizedTest
	@ValueSource(ints = { Integer.MIN_VALUE, -1, 0, 1, Integer.MAX_VALUE })
	@RandomValueSource(type = ValueType.INT, count = 5)
	public void _int(final int i) {
		assertDoesNotThrow(() -> buffer.writeInt(i));
		assertEquals(Integer.BYTES, buffer.getSize());
		assertEquals(i, assertDoesNotThrow(() -> buffer.readInt()));
	}
	
	@ParameterizedTest
	@ValueSource(longs = { Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE })
	@RandomValueSource(type = ValueType.LONG, count = 5)
	public void _long(final long l) {
		assertDoesNotThrow(() -> buffer.writeLong(l));
		assertEquals(Long.BYTES, buffer.getSize());
		assertEquals(l, assertDoesNotThrow(() -> buffer.readLong()));
	}
	
	@ParameterizedTest
	@ValueSource(floats = { Float.MIN_VALUE, -1, 0, 1, Float.MAX_VALUE })
	@RandomValueSource(type = ValueType.FLOAT, count = 5)
	public void _float(final float f) {
		assertDoesNotThrow(() -> buffer.writeFloat(f));
		assertEquals(Float.BYTES, buffer.getSize());
		assertEquals(f, assertDoesNotThrow(() -> buffer.readFloat()));
	}
	
	@ParameterizedTest
	@ValueSource(doubles = { Double.MIN_VALUE, -1, 0, 1, Double.MAX_VALUE })
	@RandomValueSource(type = ValueType.DOUBLE, count = 5)
	public void _double(final double d) {
		assertDoesNotThrow(() -> buffer.writeDouble(d));
		assertEquals(Double.BYTES, buffer.getSize());
		assertEquals(d, assertDoesNotThrow(() -> buffer.readDouble()));
	}
	
	@ParameterizedTest
	@ValueSource(booleans = { true, false })
	public void _boolean(final boolean b) {
		assertDoesNotThrow(() -> buffer.writeBoolean(b));
		assertEquals(Byte.BYTES, buffer.getSize());
		assertEquals(b, assertDoesNotThrow(() -> buffer.readBoolean()));
	}
	
	@ParameterizedTest
	@ValueSource(chars = { 'a', 'A', '\0', ' ', 'ß', 'ö', 'Ö' })
	@RandomValueSource(type = ValueType.CHAR, count = 5)
	public void _char(final char c) {
		assertDoesNotThrow(() -> buffer.writeChar(c));
		assertEquals(Character.BYTES, buffer.getSize());
		assertEquals(c, assertDoesNotThrow(() -> buffer.readChar()));
	}
	
	@ParameterizedTest
	@ValueSource(strings = "\0")
	@MethodSource(METHODS + "String")
	public void _string(final String s) {
		assertDoesNotThrow(() -> buffer.writeString(s));
		assertEquals(Integer.BYTES + s.getBytes().length, buffer.getSize());
		assertEquals(s, assertDoesNotThrow(() -> buffer.readString()));
	}
	
	@ParameterizedTest
	@EnumSource(ChronoUnit.class)
	public void _enum(final Enum<?> e) {
		assertDoesNotThrow(() -> buffer.writeEnum(e));
		assertEquals(e.name().getBytes().length + 1, buffer.getSize());
		assertEquals(e, assertDoesNotThrow(() -> buffer.readEnum(e.getDeclaringClass())));
	}
	
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, Byte.MAX_VALUE, ByteBuffer.MAX_UBYTE })
	@RandomValueSource(type = ValueType.UBYTE, count = 5)
	public void _ubyte(final int ubyte) {
		assertDoesNotThrow(() -> buffer.writeUnsignedByte(ubyte));
		assertEquals(Byte.BYTES, buffer.getSize());
		assertEquals(ubyte, assertDoesNotThrow(() -> buffer.readUnsignedByte()));
	}
	
	@ParameterizedTest
	@ValueSource(ints = { 0, 1, Short.MAX_VALUE, ByteBuffer.MAX_USHORT })
	@RandomValueSource(type = ValueType.USHORT, count = 5)
	public void _ushort(final int ushort) {
		assertDoesNotThrow(() -> buffer.writeUnsignedShort(ushort));
		assertEquals(Short.BYTES, buffer.getSize());
		assertEquals(ushort, assertDoesNotThrow(() -> buffer.readUnsignedShort()));
	}
	
	@ParameterizedTest
	@ValueSource(longs = { 0, 1, Integer.MAX_VALUE, ByteBuffer.MAX_UINT })
	@RandomValueSource(type = ValueType.UINT, count = 5)
	public void _uint(final long uint) {
		assertDoesNotThrow(() -> buffer.writeUnsignedInt(uint));
		assertEquals(Integer.BYTES, buffer.getSize());
		assertEquals(uint, assertDoesNotThrow(() -> buffer.readUnsignedInt()));
	}
	
	// TODO var length numbers (signed & unsigned)
	
	@ParameterizedTest
	@MethodSource(METHODS + "BigInteger")
	public void _bigInteger(final BigInteger bi) {
		assertDoesNotThrow(() -> buffer.writeBigInteger(bi));
		//assertEquals(... + bi.toByteArray().length, buffer.getSize());
		assertTrue(buffer.getSize() >= bi.toByteArray().length + 1);
		assertEquals(bi, assertDoesNotThrow(() -> buffer.readBigInteger()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "BigDecimal")
	public void _bigDecimal(final BigDecimal bd) {
		assertDoesNotThrow(() -> buffer.writeBigDecimal(bd));
		//assertEquals(???, buffer.getSize());
		assertTrue(buffer.getSize() >= bd.unscaledValue().toByteArray().length + 2);
		assertEquals(bd, assertDoesNotThrow(() -> buffer.readBigDecimal()));
	}
	
	@ParameterizedTest
	@ValueSource(strings = "\0")
	@MethodSource(METHODS + "String")
	public void _string_fixedLength(final String s) {
		final int length = s.getBytes().length;
		
		assertDoesNotThrow(() -> buffer.writeFixedLengthString(s, length));
		assertEquals(length, buffer.getSize());
		assertEquals(s, assertDoesNotThrow(() -> buffer.readFixedLengthString(length)));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "String")
	public void _string_nullTerminated(final String s) {
		assertDoesNotThrow(() -> buffer.writeNullTerminatedString(s));
		assertEquals(s.getBytes().length + 1, buffer.getSize());
		assertEquals(s, assertDoesNotThrow(() -> buffer.readNullTerminatedString()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "UUID")
	public void _uuid(final UUID u) {
		assertDoesNotThrow(() -> buffer.writeUUID(u));
		assertEquals(Long.BYTES * 2, buffer.getSize());
		assertEquals(u, assertDoesNotThrow(() -> buffer.readUUID()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "Instant")
	public void _instant(final Instant i) {
		assertDoesNotThrow(() -> buffer.writeInstant(i));
		//assertEquals(Long.BYTES + ..., buffer.getSize());
		assertTrue(buffer.getSize() >= Long.BYTES + 1);
		assertEquals(i, assertDoesNotThrow(() -> buffer.readInstant()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "LocalDate")
	public void _localDate(final LocalDate ld) {
		assertDoesNotThrow(() -> buffer.writeLocalDate(ld));
		//assertEquals(... + 2, buffer.getSize());
		assertTrue(buffer.getSize() >= 3);
		assertEquals(ld, assertDoesNotThrow(() -> buffer.readLocalDate()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "LocalTime")
	public void _localTime(final LocalTime lt) {
		assertDoesNotThrow(() -> buffer.writeLocalTime(lt));
		//assertEquals(3 + ..., buffer.getSize());
		assertTrue(buffer.getSize() >= 4);
		assertEquals(lt, assertDoesNotThrow(() -> buffer.readLocalTime()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "LocalDateTime")
	public void _localDateTime(final LocalDateTime ldt) {
		assertDoesNotThrow(() -> buffer.writeLocalDateTime(ldt));
		//assertEquals(... + 2 + 3 + ..., buffer.getSize());
		assertTrue(buffer.getSize() >= 7);
		assertEquals(ldt, assertDoesNotThrow(() -> buffer.readLocalDateTime()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "ZoneOffset")
	public void _zoneOffset(final ZoneOffset zo) {
		assertDoesNotThrow(() -> buffer.writeZoneOffset(zo));
		assertEquals(4, buffer.getSize());
		assertEquals(zo, assertDoesNotThrow(() -> buffer.readZoneOffset()));
	}
	
	// TODO OffsetDateTime
	
	@ParameterizedTest
	@MethodSource(METHODS + "ZoneId")
	public void _zoneId(final ZoneId zi) {
		assertDoesNotThrow(() -> buffer.writeZoneId(zi));
		assertEquals(zi.getId().getBytes().length + 1, buffer.getSize());
		assertEquals(zi, assertDoesNotThrow(() -> buffer.readZoneId()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "ZonedDateTime")
	public void _zonedDateTime(final ZonedDateTime zdt) {
		assertDoesNotThrow(() -> buffer.writeZonedDateTime(zdt));
		//assertEquals(LDT + ZI + 4, buffer.getSize());
		assertTrue(buffer.getSize() >= 14);
		assertEquals(zdt, assertDoesNotThrow(() -> buffer.readZonedDateTime()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "Duration")
	public void _duration(final Duration d) {
		assertDoesNotThrow(() -> buffer.writeDuration(d));
		//assertEquals(... + ..., buffer.getSize());
		assertTrue(buffer.getSize() >= 2);
		assertEquals(d, assertDoesNotThrow(() -> buffer.readDuration()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "Period")
	public void _period(final Period p) {
		assertDoesNotThrow(() -> buffer.writePeriod(p));
		//assertEquals(... + ... + ..., buffer.getSize());
		assertTrue(buffer.getSize() >= 3);
		assertEquals(p, assertDoesNotThrow(() -> buffer.readPeriod()));
	}
	
	@ParameterizedTest
	@MethodSource(METHODS + "Pattern")
	public void _pattern(final Pattern p) {
		assertDoesNotThrow(() -> buffer.writePattern(p));
		//assertEquals(p.pattern().getBytes().length + 4 + ..., buffer.getSize());
		assertTrue(buffer.getSize() >= p.pattern().getBytes().length + 4 + 1);
		//assertEquals(p, assertDoesNotThrow(() -> buffer.readPattern()));
		final Pattern readP = assertDoesNotThrow(() -> buffer.readPattern());
		assertEquals(p.pattern(), readP.pattern());
		assertEquals(p.flags(), readP.flags());
	}
	
	// TODO Schema
	
	@ParameterizedTest
	@MethodSource(METHODS + "Packet")
	public void _packet(final PacketBase<?> packet) {
		assertDoesNotThrow(() -> buffer.writePacket(packet));
		//assertEquals(, buffer.getSize());
		//assertEquals(packet.toMap(), assertDoesNotThrow(() -> buffer.readPacket(packet).toMap()));
		final PacketMap map = packet.toMap();
		assertEquals(map, assertDoesNotThrow(() -> buffer.readPacket(packet).toMap()));
	}
	
	@ParameterizedTest
	@MethodValueSource(value = {
			"writeString(java.lang.String)",
			"writeString(java.lang.CharSequence)",
			"writeEnum(java.lang.Enum)",
			"writeVarBigInt(java.math.BigInteger)",
			"writeUnsignedVarBigInt(java.math.BigInteger)",
			"writeBigInteger(java.math.BigInteger)",
			"writeBigDecimal(java.math.BigDecimal)",
			"writeFixedLengthString(java.lang.String, int)",
			"writeNullTerminatedString(java.lang.String)",
			"writeUUID(java.util.UUID)",
			"writeInstant(java.time.Instant)",
			"writeLocalDate(java.time.LocalDate)",
			"writeLocalTime(java.time.LocalTime)",
			"writeLocalDateTime(java.time.LocalDateTime)",
			"writeZoneOffset(java.time.ZoneOffset)",
			"writeOffsetDateTime(java.time.OffsetDateTime)",
			"writeZoneId(java.time.ZoneId)",
			"writeZonedDateTime(java.time.ZonedDateTime)",
			"writeDuration(java.time.Duration)",
			"writePeriod(java.time.Period)",
			"writePattern(java.util.regex.Pattern)",
			"write(me.neo_0815.packethandler.schema.Schema, java.lang.Object)",
			"writePacket(me.neo_0815.packethandler.packet.PacketBase)",
			"writeByteArray(byte[])",
			"writeShortArray(short[])",
			"writeIntArray(int[])",
			"writeLongArray(long[])",
			"writeFloatArray(float[])",
			"writeDoubleArray(double[])",
			"writeBooleanArray(boolean[])",
			"writeCharArray(char[])",
			"writeArray(java.lang.Object[], me.neo_0815.packethandler.schema.Schema)",
			"writeArray(java.lang.Object[], java.util.function.Consumer)",
			"writeArray(java.lang.Object[], java.util.function.BiConsumer)",
			
			"read(me.neo_0815.packethandler.schema.Schema)",
			"readPacket(me.neo_0815.packethandler.packet.PacketBase)",
			"readArray(me.neo_0815.packethandler.schema.Schema)",
			"readArray(java.lang.Class, java.util.function.Function)",
			"readArray(java.lang.Class, java.util.function.BiFunction)",
			"readArray(java.util.function.IntFunction, java.util.function.Function)",
			"readArray(java.util.function.IntFunction, java.util.function.BiFunction)",
			
			"encrypt(me.neo_0815.encryption.Encryption)",
			"decrypt(me.neo_0815.encryption.Encryption)",
			"transferTo(me.neo_0815.packethandler.ByteBuffer)",
			"transferFrom(me.neo_0815.packethandler.ByteBuffer)",
			"writeToOutputStream(java.io.OutputStream)",
			"readFromInputStream(java.io.InputStream)",
			
			"toEncrypted(me.neo_0815.encryption.Encryption)",
			"toDecrypted(me.neo_0815.encryption.Encryption)",
			"copy(me.neo_0815.packethandler.ByteBufferGenerator)",
			
			"fromInputStream(java.io.InputStream)",
			"fromHexString(java.lang.String)",
			"fromOctalString(java.lang.String)",
			"fromEncoded(me.neo_0815.packethandler.ByteBuffer)",
			"fromEncrypted(me.neo_0815.packethandler.ByteBuffer, me.neo_0815.encryption.Encryption)",
			"empty(me.neo_0815.packethandler.ByteBufferGenerator)",
	}, prefix = "me.neo_0815.packethandler.ByteBuffer#")
	public void nullParam(final MethodValue method) {
		assertThrows(NullPointerException.class, () -> method.invoke(buffer, new Object[method.getMethod().getParameterCount()]));
	}
	
	@Test
	public void emptyBuffer_read() {
		assertThrows(IllegalStateException.class, buffer::read);
	}
}
