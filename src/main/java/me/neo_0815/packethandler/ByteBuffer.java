package me.neo_0815.packethandler;

import me.neo_0815.encryption.Encryption;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.*;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The class ByteBuffer stores bytes that will be sent through an {@link OutputStream} and reads bytes from an {@link InputStream}.
 * All types that use more than one byte are stored in the {@linkplain #byteOrder chosen byte-order} format.
 *
 * @author Neo Hornberger
 */
public class ByteBuffer {
	/**
	 * A constant holding the maximum value an {@code unsigned byte} can have,
	 * 2<sup>8</sup>-1 ({@value}).
	 */
	public static final int MAX_UBYTE = 0xFF;
	/**
	 * A constant holding the maximum value an {@code unsigned short} can have,
	 * 2<sup>16</sup>-1 ({@value}).
	 */
	public static final int MAX_USHORT = 0xFFFF;
	/**
	 * A constant holding the maximum value an {@code unsigned int} can have,
	 * 2<sup>32</sup>-1 ({@value}).
	 */
	public static final long MAX_UINT = 0xFFFFFFFFL;
	
	private static final Map<Class<?>, BiConsumer<ByteBuffer, Object>> CACHE = new HashMap<>();
	
	private static final Encoder ENCODER = Base64.getEncoder();
	private static final Decoder DECODER = Base64.getDecoder();
	
	static {
		addToCache(Byte.class, ByteBuffer::write);
		addToCache(Integer.class, ByteBuffer::writeInt);
		addToCache(Short.class, ByteBuffer::writeShort);
		addToCache(Long.class, ByteBuffer::writeLong);
		addToCache(Float.class, ByteBuffer::writeFloat);
		addToCache(Double.class, ByteBuffer::writeDouble);
		addToCache(Boolean.class, ByteBuffer::writeBoolean);
		addToCache(Character.class, ByteBuffer::writeChar);
		addToCache(String.class, ByteBuffer::writeString);
		addToCache(CharSequence.class, ByteBuffer::writeString);
		addToCache(Enum.class, ByteBuffer::writeEnum);
		addToCache(BigInteger.class, ByteBuffer::writeBigInteger);
		addToCache(BigDecimal.class, ByteBuffer::writeBigDecimal);
		addToCache(UUID.class, ByteBuffer::writeUUID);
		addToCache(Instant.class, ByteBuffer::writeInstant);
		addToCache(LocalDate.class, ByteBuffer::writeLocalDate);
		addToCache(LocalTime.class, ByteBuffer::writeLocalTime);
		addToCache(LocalDateTime.class, ByteBuffer::writeLocalDateTime);
		addToCache(ZoneOffset.class, ByteBuffer::writeZoneOffset);
		addToCache(OffsetDateTime.class, ByteBuffer::writeOffsetDateTime);
		addToCache(ZoneId.class, ByteBuffer::writeZoneId);
		addToCache(ZonedDateTime.class, ByteBuffer::writeZonedDateTime);
		addToCache(Duration.class, ByteBuffer::writeDuration);
		addToCache(Period.class, ByteBuffer::writePeriod);
		addToCache(Pattern.class, ByteBuffer::writePattern);
		addToCache(IByteBufferable.class, ByteBuffer::write);
		addToCache(byte[].class, ByteBuffer::writeByteArray);
		addToCache(short[].class, ByteBuffer::writeShortArray);
		addToCache(int[].class, ByteBuffer::writeIntArray);
		addToCache(long[].class, ByteBuffer::writeLongArray);
		addToCache(float[].class, ByteBuffer::writeFloatArray);
		addToCache(double[].class, ByteBuffer::writeDoubleArray);
		addToCache(boolean[].class, ByteBuffer::writeBooleanArray);
		addToCache(char[].class, ByteBuffer::writeCharArray);
	}
	
	public ByteOrder byteOrder = ByteOrder.BIG_ENDIAN;
	public Charset charset = Charset.defaultCharset();
	
	private final ByteList bytes = new ByteList();
	
	@Getter
	private int limit = -1, writeCursor = 0, readCursor = 0;
	private int mark = 0;
	
	private ByteBufferInputStream in;
	private ByteBufferOutputStream out;
	
	private boolean writeLock = false, readLock = false;
	
	/**
	 * Constructs a new empty instance.
	 */
	public ByteBuffer() {
	}
	
	/**
	 * Constructs a new instance containing all bytes of the byte array {@code bytes}.
	 *
	 * @param bytes the bytes to write
	 *
	 * @see #write(byte[])
	 */
	public ByteBuffer(final byte[] bytes) {
		write(bytes);
	}
	
	/**
	 * Constructs a new instance containing all bytes of the ByteBuffer {@code buffer}.
	 *
	 * @param buffer the {@code ByteBuffer} of which the bytes are copied from
	 *
	 * @see #write(byte[])
	 */
	public ByteBuffer(final ByteBuffer buffer) {
		byteOrder = buffer.byteOrder;
		charset = buffer.charset;
		
		write(buffer.toByteArray());
	}
	
	/**
	 * Writes the byte {@code b} to the buffer.
	 *
	 * @param b the byte to write
	 *
	 * @return the current instance
	 */
	public ByteBuffer write(final byte b) {
		if(writeLock) throw new IllegalStateException("Writing is locked");
		if(limit > -1 && writeCursor >= limit)
			throw new IllegalStateException("Reached limit of " + limit + " elements");
		
		bytes.add(b);
		
		writeCursor++;
		
		return this;
	}
	
	/**
	 * Writes the int {@code b} as a byte to the buffer.
	 *
	 * @param b the byte to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	public ByteBuffer write(final int b) {
		return write((byte) b);
	}
	
	/**
	 * Writes the long {@code b} as a byte to the buffer.
	 *
	 * @param b the byte to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	private ByteBuffer write(final long b) {
		return write((byte) b);
	}
	
	/**
	 * Writes the bytes contained in the byte array {@code bytes} to the buffer.
	 *
	 * @param bytes the bytes to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	public ByteBuffer write(final byte[] bytes) {
		for(final byte b : bytes)
			write(b);
		
		return this;
	}
	
	/**
	 * Writes the first {@code n} bytes contained in the byte array {@code bytes} to the buffer.
	 *
	 * @param bytes the bytes to write
	 * @param n     the count of bytes to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	public ByteBuffer write(final byte[] bytes, final int n) {
		for(int i = 0; i < n; i++)
			write(bytes[i]);
		
		return this;
	}
	
	/**
	 * Writes the short {@code s} to the buffer.
	 *
	 * @param s the short to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	public ByteBuffer writeShort(final short s) {
		if(byteOrder.isBigEndian()) {
			write(s >>> 8);
			write(s);
		}else {
			write(s);
			write(s >>> 8);
		}
		
		return this;
	}
	
	/**
	 * Writes the int {@code s} as a short to the buffer.
	 *
	 * @param s the short to write
	 *
	 * @return the current instance
	 *
	 * @see #writeShort(short)
	 */
	public ByteBuffer writeShort(final int s) {
		return writeShort((short) s);
	}
	
	/**
	 * Writes the int {@code i} to the buffer.
	 *
	 * @param i the int to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	public ByteBuffer writeInt(final int i) {
		if(byteOrder.isBigEndian()) {
			write(i >>> 24);
			write(i >>> 16);
			write(i >>> 8);
			write(i);
		}else {
			write(i);
			write(i >>> 8);
			write(i >>> 16);
			write(i >>> 24);
		}
		
		return this;
	}
	
	/**
	 * Writes the long {@code l} to the buffer.
	 *
	 * @param l the long to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	public ByteBuffer writeLong(final long l) {
		if(byteOrder.isBigEndian()) {
			write(l >>> 56);
			write(l >>> 48);
			write(l >>> 40);
			write(l >>> 32);
			write(l >>> 24);
			write(l >>> 16);
			write(l >>> 8);
			write(l);
		}else {
			write(l);
			write(l >>> 8);
			write(l >>> 16);
			write(l >>> 24);
			write(l >>> 32);
			write(l >>> 40);
			write(l >>> 48);
			write(l >>> 56);
		}
		
		return this;
	}
	
	/**
	 * Writes the float {@code f} to the buffer.
	 *
	 * @param f the float to write
	 *
	 * @return the current instance
	 *
	 * @see #writeInt(int)
	 */
	public ByteBuffer writeFloat(final float f) {
		return writeInt(Float.floatToRawIntBits(f));
	}
	
	/**
	 * Writes the double {@code d} to the buffer.
	 *
	 * @param d the double to write
	 *
	 * @return the current instance
	 *
	 * @see #writeLong(long)
	 */
	public ByteBuffer writeDouble(final double d) {
		return writeLong(Double.doubleToRawLongBits(d));
	}
	
	/**
	 * Writes the boolean {@code b} as one byte to the buffer.
	 * ({@code 0b00000001 = true} or {@code 0b00000000 = false})
	 *
	 * @param b the boolean to write
	 *
	 * @return the current instance
	 *
	 * @see #write(byte)
	 */
	public ByteBuffer writeBoolean(final boolean b) {
		return write(b ? 1 : 0);
	}
	
	/**
	 * Writes the char {@code c} to the buffer.
	 *
	 * @param c the char to write
	 *
	 * @return the current instance
	 *
	 * @see #writeShort(short)
	 */
	public ByteBuffer writeChar(final char c) {
		return writeShort((short) c);
	}
	
	/**
	 * Writes the {@link String} {@code s} length-prefixed to the buffer.
	 *
	 * @param s the {@link String} to write
	 *
	 * @return the current instance
	 *
	 * @see #charset
	 * @see #writeString(String, Charset)
	 */
	public ByteBuffer writeString(final String s) {
		return writeString(s, charset);
	}
	
	/**
	 * Writes the {@link String} {@code s} length-prefixed to the buffer.
	 *
	 * @param s       the {@link String} to write
	 * @param charset the {@link Charset} to be used to encode the {@link String}
	 *
	 * @return the current instance
	 *
	 * @see #writeInt(int)
	 * @see String#getBytes(Charset)
	 * @see #write(byte[])
	 */
	public ByteBuffer writeString(final String s, final Charset charset) {
		final byte[] bytes = s.getBytes(charset);
		
		writeInt(bytes.length);
		write(bytes);
		
		return this;
	}
	
	public ByteBuffer writeString(final CharSequence cs) {
		return writeString(cs.toString());
	}
	
	/**
	 * Writes the name of the {@linkplain Enum enum value} {@code e} as a string to the buffer.
	 *
	 * @param e the enum value to write
	 *
	 * @return the current instance
	 *
	 * @see Enum#name()
	 * @see #writeNullTerminatedString(String)
	 */
	public <T extends Enum<T>> ByteBuffer writeEnum(final Enum<T> e) {
		return writeNullTerminatedString(e.name());
	}
	
	/**
	 * Writes the unsigned byte {@code ubyte} to the buffer.
	 *
	 * @param ubyte the unsigned byte to write
	 *
	 * @return the current instance
	 *
	 * @throws IllegalArgumentException if {@code ubyte} exceeds the range [0;{@value #MAX_UBYTE}]
	 * @see #write(int)
	 */
	public ByteBuffer writeUnsignedByte(final int ubyte) {
		if(ubyte < 0 || ubyte > MAX_UBYTE) {
			write(0);
			
			throw new IllegalArgumentException("Unsigned byte is out of range");
		}
		
		return write(ubyte);
	}
	
	/**
	 * Writes the unsigned short {@code ushort} to the buffer.
	 *
	 * @param ushort the unsigned short to write
	 *
	 * @return the current instance
	 *
	 * @throws IllegalArgumentException if {@code ushort} exceeds the range [0;{@value #MAX_USHORT}]
	 * @see #writeShort(int)
	 */
	public ByteBuffer writeUnsignedShort(final int ushort) {
		if(ushort < 0 || ushort > MAX_USHORT) {
			writeShort(0);
			
			throw new IllegalArgumentException("Unsigned short is out of range");
		}
		
		return writeShort(ushort);
	}
	
	/**
	 * Writes the unsigned int {@code uint} to the buffer.
	 *
	 * @param uint the unsigned int to write
	 *
	 * @return the current instance
	 *
	 * @throws IllegalArgumentException if {@code uint} exceeds the range [0;{@value #MAX_UINT}]
	 * @see #writeInt(int)
	 */
	public ByteBuffer writeUnsignedInt(final long uint) {
		if(uint < 0 || uint > MAX_UINT) {
			writeInt(0);
			
			throw new IllegalArgumentException("Unsigned integer is out of range");
		}
		
		return writeInt((int) uint);
	}
	
	/**
	 * Writes the int {@code vi} in the variable-length format to the buffer.
	 *
	 * @param vi the int to write
	 *
	 * @return the current instance
	 *
	 * @see VLQHelper.VLQInt#encodeSLE(ByteBuffer, int)
	 */
	public ByteBuffer writeVarInt(final int vi) {
		VLQHelper.VLQInt.encodeSLE(this, vi);
		
		return this;
	}
	
	/**
	 * Writes the long {@code vl} in the variable-length format to the buffer.
	 *
	 * @param vl the long to write
	 *
	 * @return the current instance
	 *
	 * @see VLQHelper.VLQLong#encodeSLE(ByteBuffer, long)
	 */
	public ByteBuffer writeVarLong(final long vl) {
		VLQHelper.VLQLong.encodeSLE(this, vl);
		
		return this;
	}
	
	/**
	 * Writes the {@link BigInteger} {@code vbi} in the variable-length format to the buffer.
	 *
	 * @param vbi the {@link BigInteger} to write
	 *
	 * @return the current instance
	 *
	 * @see VLQHelper.VLQBigInt#encodeSLE(ByteBuffer, BigInteger)
	 */
	public ByteBuffer writeVarBigInt(final BigInteger vbi) {
		VLQHelper.VLQBigInt.encodeSLE(this, vbi);
		
		return this;
	}
	
	/**
	 * Writes the non-negative int {@code vi} in the variable-length format to the buffer.
	 *
	 * @param vi the non-negative int to write
	 *
	 * @return the current instance
	 *
	 * @see VLQHelper.VLQInt#encodeULE(ByteBuffer, int)
	 */
	public ByteBuffer writeUnsignedVarInt(final int vi) {
		VLQHelper.VLQInt.encodeULE(this, vi);
		
		//		byte temp;
		//
		//		do {
		//			temp = (byte) vi;
		//
		//			write(temp);
		//
		//			vi >>>= 8;
		//		}while(vi != 0);
		
		return this;
	}
	
	/**
	 * Writes the non-negative long {@code vl} in the variable-length format to the buffer.
	 *
	 * @param vl the non-negative long to write
	 *
	 * @return the current instance
	 *
	 * @see VLQHelper.VLQLong#encodeULE(ByteBuffer, long)
	 */
	public ByteBuffer writeUnsignedVarLong(final long vl) {
		VLQHelper.VLQLong.encodeULE(this, vl);
		
		return this;
	}
	
	/**
	 * Writes the non-negative {@link BigInteger} {@code vbi} in the variable-length format to the buffer.
	 *
	 * @param vbi the non-negative {@link BigInteger} to write
	 *
	 * @return the current instance
	 *
	 * @see VLQHelper.VLQBigInt#encodeULE(ByteBuffer, BigInteger)
	 */
	public ByteBuffer writeUnsignedVarBigInt(final BigInteger vbi) {
		VLQHelper.VLQBigInt.encodeULE(this, vbi);
		
		return this;
	}
	
	/**
	 * Writes the {@link BigInteger} {@code bi} as a length-prefixed byte array to the buffer.
	 *
	 * @param bi the {@link BigInteger} to write
	 *
	 * @return the current instance
	 *
	 * @see BigInteger#toByteArray()
	 * @see #writeByteArray(byte[])
	 */
	public ByteBuffer writeBigInteger(final BigInteger bi) {
		return writeByteArray(bi.toByteArray());
	}
	
	/**
	 * Writes the {@link BigDecimal} {@code bd} as a scaled {@link BigInteger} to the buffer.
	 *
	 * @param bd the {@link BigDecimal} to write
	 *
	 * @return the current instance
	 *
	 * @see BigDecimal#unscaledValue()
	 * @see #writeBigInteger(BigInteger)
	 * @see BigDecimal#scale()
	 * @see #writeVarInt(int)
	 */
	public ByteBuffer writeBigDecimal(final BigDecimal bd) {
		writeBigInteger(bd.unscaledValue());
		writeVarInt(bd.scale());
		//		writeVarInt(bd.precision());
		
		return this;
	}
	
	/**
	 * Writes the {@link String} {@code s} to the buffer.
	 *
	 * @param s      the {@link String} to write
	 * @param length the exact number of bytes to write
	 *
	 * @return the current instance
	 *
	 * @throws IllegalArgumentException if the number of bytes of the {@link String} differ from {@code length}
	 * @see #charset
	 * @see #writeFixedLengthString(String, Charset, int)
	 */
	public ByteBuffer writeFixedLengthString(final String s, final int length) {
		return writeFixedLengthString(s, charset, length);
	}
	
	/**
	 * Writes the {@link String} {@code s} to the buffer.
	 *
	 * @param s       the {@link String} to write
	 * @param charset the {@link Charset} to be used to encode the {@link String}
	 * @param length  the exact number of bytes to write
	 *
	 * @return the current instance
	 *
	 * @throws IllegalArgumentException if the number of bytes of the {@link String} differ from {@code length}
	 * @see String#getBytes(Charset)
	 * @see #write(byte[])
	 */
	public ByteBuffer writeFixedLengthString(final String s, final Charset charset, final int length) {
		final byte[] bytes = s.getBytes(charset);
		
		if(bytes.length != length) {
			final byte[] errorBytes = new byte[length];
			
			Arrays.fill(errorBytes, (byte) 0);
			
			write(errorBytes);
			
			throw new IllegalArgumentException("Bytes length and given length differ");
		}
		
		return write(bytes);
	}
	
	/**
	 * Writes the {@link String} {@code s} to the buffer.
	 *
	 * @param s the {@link String} to write
	 *
	 * @return the current instance
	 *
	 * @see #charset
	 * @see #writeNullTerminatedString(String, Charset)
	 */
	public ByteBuffer writeNullTerminatedString(final String s) {
		return writeNullTerminatedString(s, charset);
	}
	
	/**
	 * Writes the {@link String} {@code s} to the buffer.
	 *
	 * @param s       the {@link String} to write
	 * @param charset the {@link Charset} to be used to encode the {@link String}
	 *
	 * @return the current instance
	 *
	 * @throws IllegalArgumentException if the {@link String} contains a null-byte
	 * @see String#getBytes(Charset)
	 * @see #write(byte[])
	 */
	public ByteBuffer writeNullTerminatedString(final String s, final Charset charset) {
		final byte[] bytes = s.getBytes(charset);
		
		boolean foundNUL = false;
		for(final byte b : bytes)
			if(b == 0) {
				foundNUL = true;
				
				break;
			}
		
		if(!foundNUL) write(bytes);
		
		write(0);
		
		if(foundNUL) throw new IllegalArgumentException("String representation cannot contain NUL");
		
		return this;
	}
	
	/**
	 * Writes the {@link UUID} {@code u} as two longs to the buffer.
	 *
	 * @param u the {@link UUID} to write
	 *
	 * @return the current instance
	 *
	 * @see #writeLong(long)
	 */
	public ByteBuffer writeUUID(final UUID u) {
		if(byteOrder.isBigEndian()) {
			writeLong(u.getMostSignificantBits());
			writeLong(u.getLeastSignificantBits());
		}else {
			writeLong(u.getLeastSignificantBits());
			writeLong(u.getMostSignificantBits());
		}
		
		return this;
	}
	
	/**
	 * Writes the {@link Instant} {@code i} as one long and one unsigned {@linkplain VLQHelper.VLQInt var-int} to the buffer.
	 *
	 * @param i the {@link Instant} to write
	 *
	 * @return the current instance
	 *
	 * @see Instant#getEpochSecond()
	 * @see #writeLong(long)
	 * @see Instant#getNano()
	 * @see #writeUnsignedVarInt(int)
	 */
	public ByteBuffer writeInstant(final Instant i) {
		if(byteOrder.isBigEndian()) {
			writeLong(i.getEpochSecond());
			writeUnsignedVarInt(i.getNano());
		}else {
			writeUnsignedVarInt(i.getNano());
			writeLong(i.getEpochSecond());
		}
		
		return this;
	}
	
	/**
	 * Writes the {@link LocalDate} {@code ld} as one {@linkplain VLQHelper.VLQInt var-int} and two bytes to the buffer.
	 *
	 * @param ld the {@link LocalDate} to write
	 *
	 * @return the current instance
	 *
	 * @see LocalDate#getYear()
	 * @see #writeVarInt(int)
	 * @see LocalDate#getMonthValue()
	 * @see LocalDate#getDayOfMonth()
	 * @see #write(int)
	 */
	public ByteBuffer writeLocalDate(final LocalDate ld) {
		if(byteOrder.isBigEndian()) {
			writeVarInt(ld.getYear());
			write(ld.getMonthValue());
			write(ld.getDayOfMonth());
		}else {
			write(ld.getDayOfMonth());
			write(ld.getMonthValue());
			writeVarInt(ld.getYear());
		}
		
		return this;
	}
	
	/**
	 * Writes the {@link LocalTime} {@code lt} as three bytes and one unsigned {@linkplain VLQHelper.VLQInt var-int} to the buffer.
	 *
	 * @param lt the {@link LocalTime} to write
	 *
	 * @return the current instance
	 *
	 * @see LocalTime#getHour()
	 * @see LocalTime#getMinute()
	 * @see LocalTime#getSecond()
	 * @see #write(int)
	 * @see LocalTime#getNano()
	 * @see #writeUnsignedVarInt(int)
	 */
	public ByteBuffer writeLocalTime(final LocalTime lt) {
		if(byteOrder.isBigEndian()) {
			write(lt.getHour());
			write(lt.getMinute());
			write(lt.getSecond());
			writeUnsignedVarInt(lt.getNano());
		}else {
			writeUnsignedVarInt(lt.getNano());
			write(lt.getSecond());
			write(lt.getMinute());
			write(lt.getHour());
		}
		
		return this;
	}
	
	/**
	 * Writes the {@link LocalDateTime} {@code ldt} as a {@link LocalDate} and a {@link LocalTime} to the buffer.
	 *
	 * @param ldt the {@link LocalDateTime} to write
	 *
	 * @return the current instance
	 *
	 * @see LocalDateTime#toLocalDate()
	 * @see #writeLocalDate(LocalDate)
	 * @see LocalDateTime#toLocalTime()
	 * @see #writeLocalTime(LocalTime)
	 */
	public ByteBuffer writeLocalDateTime(final LocalDateTime ldt) {
		if(byteOrder.isBigEndian()) {
			writeLocalDate(ldt.toLocalDate());
			writeLocalTime(ldt.toLocalTime());
		}else {
			writeLocalTime(ldt.toLocalTime());
			writeLocalDate(ldt.toLocalDate());
		}
		
		return this;
	}
	
	/**
	 * Writes the {@link ZoneOffset} {@code zo} as an int to the buffer.
	 *
	 * @param zo the {@link ZoneOffset} to write
	 *
	 * @return the current instance
	 *
	 * @see ZoneOffset#getTotalSeconds()
	 * @see #writeInt(int)
	 */
	public ByteBuffer writeZoneOffset(final ZoneOffset zo) {
		return writeInt(zo.getTotalSeconds());
	}
	
	/**
	 * Writes the {@link OffsetDateTime} {@code odt} as a {@link LocalDateTime} and a {@link ZoneOffset} to the buffer.
	 *
	 * @param odt the {@link OffsetDateTime} to write
	 *
	 * @return the current instance
	 *
	 * @see OffsetDateTime#toLocalDateTime()
	 * @see #writeLocalDateTime(LocalDateTime)
	 * @see OffsetDateTime#getOffset()
	 * @see #writeZoneOffset(ZoneOffset)
	 */
	public ByteBuffer writeOffsetDateTime(final OffsetDateTime odt) {
		writeLocalDateTime(odt.toLocalDateTime());
		writeZoneOffset(odt.getOffset());
		
		return this;
	}
	
	/**
	 * Writes the {@link ZoneId} {@code zi} as a null-terminated string to the buffer.
	 *
	 * @param zi the {@link ZoneId} to write
	 *
	 * @return the current instance
	 *
	 * @see ZoneId#getId()
	 * @see #writeNullTerminatedString(String)
	 */
	public ByteBuffer writeZoneId(final ZoneId zi) {
		return writeNullTerminatedString(zi.getId());
	}
	
	/**
	 * Writes the {@link ZonedDateTime} {@code zdt} as a {@link LocalDateTime}, a {@link ZoneId} and a {@link ZoneOffset} to the buffer.
	 *
	 * @param zdt the {@link ZonedDateTime} to write
	 *
	 * @return the current instance
	 *
	 * @see ZonedDateTime#toLocalDateTime()
	 * @see #writeLocalDateTime(LocalDateTime)
	 * @see ZonedDateTime#getZone()
	 * @see #writeZoneId(ZoneId)
	 * @see ZonedDateTime#getOffset()
	 * @see #writeZoneOffset(ZoneOffset)
	 */
	public ByteBuffer writeZonedDateTime(final ZonedDateTime zdt) {
		writeLocalDateTime(zdt.toLocalDateTime());
		writeZoneId(zdt.getZone());
		writeZoneOffset(zdt.getOffset());
		
		return this;
	}
	
	/**
	 * Writes the {@link Duration} {@code d} as one {@linkplain VLQHelper.VLQLong var-long} and one {@linkplain VLQHelper.VLQInt var-int} to the buffer.
	 *
	 * @param d the {@link Duration} to write
	 *
	 * @return the current instance
	 *
	 * @see Duration#getSeconds()
	 * @see #writeVarLong(long)
	 * @see Duration#getNano()
	 * @see #writeVarInt(int)
	 */
	public ByteBuffer writeDuration(final Duration d) {
		if(byteOrder.isBigEndian()) {
			writeVarLong(d.getSeconds());
			writeVarInt(d.getNano());
		}else {
			writeVarInt(d.getNano());
			writeVarLong(d.getSeconds());
		}
		
		return this;
	}
	
	/**
	 * Writes the {@link Period} {@code p} as three {@linkplain VLQHelper.VLQInt var-ints} to the buffer.
	 *
	 * @param p the {@link Period} to write
	 *
	 * @return the current instance
	 *
	 * @see Period#getYears()
	 * @see Period#getMonths()
	 * @see Period#getDays()
	 * @see #writeVarInt(int)
	 */
	public ByteBuffer writePeriod(final Period p) {
		if(byteOrder.isBigEndian()) {
			writeVarInt(p.getYears());
			writeVarInt(p.getMonths());
			writeVarInt(p.getDays());
		}else {
			writeVarInt(p.getDays());
			writeVarInt(p.getMonths());
			writeVarInt(p.getYears());
		}
		
		return this;
	}
	
	/**
	 * Writes the {@link Pattern} {@code p} as a {@link String} and an unsigned {@linkplain VLQHelper.VLQInt var-int} to the buffer.
	 *
	 * @param p the {@link Pattern} to write
	 *
	 * @return the current instance
	 *
	 * @see Pattern#pattern()
	 * @see #writeString(String)
	 * @see Pattern#flags()
	 * @see #writeUnsignedVarInt(int)
	 */
	public ByteBuffer writePattern(final Pattern p) {
		writeString(p.pattern());
		writeUnsignedVarInt(p.flags());
		
		return this;
	}
	
	/**
	 * Writes the {@link IByteBufferable} {@code obj} to the buffer.
	 *
	 * @param obj the {@link IByteBufferable} to write
	 * @param <B> the {@code ByteBuffer} type the current instance will be casted to
	 *
	 * @return the current instance
	 *
	 * @see IByteBufferable#toBuffer(B)
	 */
	@SuppressWarnings("unchecked")
	public <B extends ByteBuffer> ByteBuffer write(final IByteBufferable<B> obj) {
		obj.toBuffer((B) this);
		
		return this;
	}
	
	/**
	 * Writes the byte array {@code bytes} to the buffer.
	 *
	 * @param bytes the byte array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #write(byte[])
	 */
	public ByteBuffer writeByteArray(final byte[] bytes) {
		writeUnsignedVarInt(bytes.length);
		write(bytes);
		
		return this;
	}
	
	/**
	 * Writes the short array {@code shorts} to the buffer.
	 *
	 * @param shorts the short array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #writeShort(short)
	 */
	public ByteBuffer writeShortArray(final short[] shorts) {
		writeUnsignedVarInt(shorts.length);
		
		for(final short s : shorts)
			writeShort(s);
		
		return this;
	}
	
	/**
	 * Writes the int array {@code ints} to the buffer.
	 *
	 * @param ints the int array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #writeInt(int)
	 */
	public ByteBuffer writeIntArray(final int[] ints) {
		writeUnsignedVarInt(ints.length);
		
		for(final int i : ints)
			writeInt(i);
		
		return this;
	}
	
	/**
	 * Writes the long array {@code longs} to the buffer.
	 *
	 * @param longs the long array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #writeLong(long)
	 */
	public ByteBuffer writeLongArray(final long[] longs) {
		writeUnsignedVarInt(longs.length);
		
		for(final long l : longs)
			writeLong(l);
		
		return this;
	}
	
	/**
	 * Writes the float array {@code floats} to the buffer.
	 *
	 * @param floats the float array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #writeFloat(float)
	 */
	public ByteBuffer writeFloatArray(final float[] floats) {
		writeUnsignedVarInt(floats.length);
		
		for(final float f : floats)
			writeFloat(f);
		
		return this;
	}
	
	/**
	 * Writes the double array {@code doubles} to the buffer.
	 *
	 * @param doubles the double array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #writeDouble(double)
	 */
	public ByteBuffer writeDoubleArray(final double[] doubles) {
		writeUnsignedVarInt(doubles.length);
		
		for(final double d : doubles)
			writeDouble(d);
		
		return this;
	}
	
	/**
	 * Writes the boolean array {@code bools} to the buffer.
	 *
	 * @param bools the boolean array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #writeBoolean(boolean)
	 */
	public ByteBuffer writeBooleanArray(final boolean[] bools) {
		writeUnsignedVarInt(bools.length);
		
		for(final boolean b : bools)
			writeBoolean(b);
		
		return this;
	}
	
	/**
	 * Writes the char array {@code chars} to the buffer.
	 *
	 * @param chars the char array to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see #writeChar(char)
	 */
	public ByteBuffer writeCharArray(final char[] chars) {
		writeUnsignedVarInt(chars.length);
		
		for(final char c : chars)
			writeChar(c);
		
		return this;
	}
	
	/**
	 * Writes the {@linkplain T type} array {@code objs} to the buffer.
	 *
	 * @param objs   the {@linkplain T type} array to write
	 * @param writer the {@link BiConsumer} that defines the writing behavior
	 * @param <B>    the {@code ByteBuffer} type the current instance will be casted to
	 * @param <T>    the type of the objects to write
	 *
	 * @return the current instance
	 *
	 * @see #writeUnsignedVarInt(int)
	 * @see BiConsumer#accept(Object, Object)
	 */
	@SuppressWarnings("unchecked")
	public <B extends ByteBuffer, T> ByteBuffer writeArray(final T[] objs, final BiConsumer<B, T> writer) {
		writeUnsignedVarInt(objs.length);
		
		lockReading((B) this, buf -> {
			for(final T obj : objs)
				writer.accept(buf, obj);
		});
		
		return this;
	}
	
	final ByteBuffer write(final Object obj) {
		Objects.requireNonNull(obj);
		
		Class<?> clazz = obj.getClass();
		
		if(clazz.isArray() && !CACHE.containsKey(clazz)) return writeArray(obj);
		
		do
			if(CACHE.containsKey(clazz)) {
				CACHE.get(clazz).accept(this, obj);
				
				return this;
			}
		while((clazz = clazz.getSuperclass()) != null);
		
		throw new TypeNotRegisteredException(obj.getClass());
	}
	
	final ByteBuffer writeArray(final Object[] array) {
		final ByteBuffer buf = copy().clear();
		
		int length = array.length;
		Set<Class<?>> types = null;
		
		for(final Object elem : array)
			try {
				buf.write(elem);
			}catch(final TypeNotRegisteredException tnre) {
				if(types == null) types = new LinkedHashSet<>();
				
				types.add(tnre.type);
				
				length--;
			}
		
		writeUnsignedVarInt(length);
		transferFrom(buf);
		
		if(types != null) throw new TypesNotRegisteredException(types);
		
		return this;
	}
	
	final ByteBuffer writeArray(final Object array) {
		final Iterator<Object> iter = new Iterator<>() {
			private final int length = Array.getLength(array);
			
			private int index = 0;
			
			@Override
			public Object next() {
				return Array.get(array, index++);
			}
			
			@Override
			public boolean hasNext() {
				return index < length;
			}
		};
		final ByteBuffer buf = copy().clear();
		
		int length = Array.getLength(array);
		Set<Class<?>> types = null;
		
		while(iter.hasNext())
			try {
				buf.write(iter.next());
			}catch(final TypeNotRegisteredException tnre) {
				if(types == null) types = new LinkedHashSet<>();
				
				types.add(tnre.type);
				
				length--;
			}
		
		writeUnsignedVarInt(length);
		transferFrom(buf);
		
		if(types != null) throw new TypesNotRegisteredException(types);
		
		return this;
	}
	
	/**
	 * Reads a byte from the buffer.
	 *
	 * @return the byte read
	 */
	public byte read() {
		if(readLock) throw new IllegalStateException("Reading is locked");
		if(readCursor >= bytes.size) throw new IllegalStateException("There are no bytes to read");
		
		return bytes.get(readCursor++);
	}
	
	/**
	 * Reads {@code n} bytes from the buffer. If {@code n == -1} all bytes are read.
	 *
	 * @param n the count of the bytes to read
	 *
	 * @return an array of the bytes read
	 *
	 * @see #read()
	 * @see #readAll()
	 */
	public byte[] read(final int n) {
		if(n == -1) return readAll();
		
		final byte[] bytes = new byte[n];
		
		for(int i = 0; i < n; i++)
			bytes[i] = read();
		
		return bytes;
	}
	
	/**
	 * Reads bytes until {@code toByte} is read.
	 *
	 * @param toByte the byte to read to
	 *
	 * @return an array of the bytes read
	 *
	 * @see #read()
	 */
	public byte[] readTo(final byte toByte) {
		return IntStream.generate(this::read).takeWhile(b -> b != toByte).collect(ByteBuffer::new, ByteBuffer::write, ByteBuffer::transferFrom).toByteArray();
		
		// readCursor--;
	}
	
	public byte[] readTo(final int toByte) {
		return readTo((byte) toByte);
	}
	
	/**
	 * Reads all bytes from the buffer.
	 *
	 * @return an array of the bytes read
	 *
	 * @see #getSize()
	 * @see #read(int)
	 */
	public byte[] readAll() {
		return read(getSize());
	}
	
	/**
	 * Reads two bytes from the buffer and interprets them as a short.
	 *
	 * @return the short read
	 *
	 * @see #readUnsignedByte()
	 */
	@SuppressWarnings("IfStatementWithIdenticalBranches")
	public short readShort() {
		final short s;
		
		if(byteOrder.isBigEndian()) s = (short) (readUnsignedByte() << 8 | readUnsignedByte());
		else s = (short) (readUnsignedByte() | readUnsignedByte() << 8);
		
		return s;
	}
	
	/**
	 * Reads four bytes from the buffer and interprets them as an int.
	 *
	 * @return the int read
	 *
	 * @see #readUnsignedByte()
	 */
	public int readInt() {
		final int i;
		
		if(byteOrder.isBigEndian())
			i = readUnsignedByte() << 24 | readUnsignedByte() << 16 | readUnsignedByte() << 8 | readUnsignedByte();
		else i = readUnsignedByte() | readUnsignedByte() << 8 | readUnsignedByte() << 16 | readUnsignedByte() << 24;
		
		return i;
	}
	
	/**
	 * Reads eight bytes from the buffer and interprets them as a long.
	 *
	 * @return the long read
	 *
	 * @see #readUnsignedByte()
	 */
	public long readLong() {
		final long l;
		
		if(byteOrder.isBigEndian())
			l = (long) readUnsignedByte() << 56 | (long) readUnsignedByte() << 48 | (long) readUnsignedByte() << 40 | (long) readUnsignedByte() << 32 | (long) readUnsignedByte() << 24 | (long) readUnsignedByte() << 16 | (long) readUnsignedByte() << 8 | readUnsignedByte();
		else
			l = readUnsignedByte() | (long) readUnsignedByte() << 8 | (long) readUnsignedByte() << 16 | (long) readUnsignedByte() << 24 | (long) readUnsignedByte() << 32 | (long) readUnsignedByte() << 40 | (long) readUnsignedByte() << 48 | (long) readUnsignedByte() << 56;
		
		return l;
	}
	
	/**
	 * Reads an int from the buffer and interprets it as a float.
	 *
	 * @return the float read
	 *
	 * @see #readInt()
	 */
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}
	
	/**
	 * Reads a long from the buffer and interprets it as a double.
	 *
	 * @return the double read
	 *
	 * @see #readLong()
	 */
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}
	
	/**
	 * Reads a byte from the buffer and interprets it as a boolean.
	 *
	 * @return the boolean read
	 *
	 * @see #read()
	 */
	public boolean readBoolean() {
		return read() == 1;
	}
	
	/**
	 * Reads a short from the buffer and interprets it as a char.
	 *
	 * @return the char read
	 *
	 * @see #readShort()
	 */
	public char readChar() {
		return (char) readShort();
	}
	
	/**
	 * Reads an int {@code n} and {@code n} bytes from the buffer and interprets them as a {@link String}.
	 *
	 * @return the {@link String} read
	 *
	 * @see #charset
	 * @see #readString(Charset)
	 */
	public String readString() {
		return readString(charset);
	}
	
	/**
	 * Reads an int {@code n} and {@code n} bytes from the buffer and interprets them as a {@link String}.
	 *
	 * @return the {@link String} read
	 *
	 * @see #readInt()
	 * @see #readFixedLengthString(int, Charset)
	 */
	public String readString(final Charset charset) {
		return readFixedLengthString(readInt(), charset);
	}
	
	/**
	 * Reads a string from the buffer and interprets it as an {@link Enum} value of the class {@code e}.
	 *
	 * @param e   the class of the {@link Enum}
	 * @param <T> the type of the {@link Enum} to read
	 *
	 * @return the {@link Enum} value read
	 *
	 * @see #readNullTerminatedString()
	 */
	public <T extends Enum<T>> T readEnum(final Class<T> e) {
		return Enum.valueOf(e, readNullTerminatedString());
	}
	
	/**
	 * Reads an unsigned byte from the buffer.
	 *
	 * @return the unsigned byte read
	 *
	 * @see #read()
	 */
	public int readUnsignedByte() {
		return read() & 0xFF;
	}
	
	/**
	 * Reads an unsigned short from the buffer.
	 *
	 * @return the unsigned short read
	 *
	 * @see #readShort()
	 * @see Short#toUnsignedInt(short)
	 */
	public int readUnsignedShort() {
		return Short.toUnsignedInt(readShort());
	}
	
	/**
	 * Reads an unsigned int from the buffer.
	 *
	 * @return the unsigned int read
	 *
	 * @see #readInt()
	 * @see Integer#toUnsignedLong(int)
	 */
	public long readUnsignedInt() {
		return Integer.toUnsignedLong(readInt());
	}
	
	/**
	 * Reads an int in the variable-length format from the buffer.
	 *
	 * @return the int read
	 *
	 * @see VLQHelper.VLQInt#decodeSLE(ByteBuffer)
	 */
	public int readVarInt() {
		return VLQHelper.VLQInt.decodeSLE(this);
	}
	
	/**
	 * Reads a long in the variable-length format from the buffer.
	 *
	 * @return the long read
	 *
	 * @see VLQHelper.VLQLong#decodeSLE(ByteBuffer)
	 */
	public long readVarLong() {
		return VLQHelper.VLQLong.decodeSLE(this);
	}
	
	/**
	 * Reads a {@link BigInteger} in the variable-length format from the buffer.
	 *
	 * @return the {@link BigInteger} read
	 *
	 * @see VLQHelper.VLQBigInt#decodeSLE(ByteBuffer)
	 */
	public BigInteger readVarBigInt() {
		return VLQHelper.VLQBigInt.decodeSLE(this);
	}
	
	/**
	 * Reads a non-negative int in the variable-length format from the buffer.
	 *
	 * @return the non-negative int read
	 *
	 * @see VLQHelper.VLQInt#decodeULE(ByteBuffer)
	 */
	public int readUnsignedVarInt() {
		return VLQHelper.VLQInt.decodeULE(this);
	}
	
	/**
	 * Reads a non-negative long in the variable-length format from the buffer.
	 *
	 * @return the non-negative long read
	 *
	 * @see VLQHelper.VLQLong#decodeULE(ByteBuffer)
	 */
	public long readUnsignedVarLong() {
		return VLQHelper.VLQLong.decodeULE(this);
	}
	
	/**
	 * Reads a non-negative {@link BigInteger} in the variable-length format from the buffer.
	 *
	 * @return the non-negative {@link BigInteger} read
	 *
	 * @see VLQHelper.VLQBigInt#decodeULE(ByteBuffer)
	 */
	public BigInteger readUnsignedVarBigInt() {
		return VLQHelper.VLQBigInt.decodeULE(this);
	}
	
	/**
	 * Reads a length-prefixed byte array from the buffer and interprets it as a {@link BigInteger}.
	 *
	 * @return the {@link BigInteger} read
	 *
	 * @see #readByteArray()
	 */
	public BigInteger readBigInteger() {
		return new BigInteger(readByteArray());
	}
	
	/**
	 * Reads a scaled {@link BigInteger} from the buffer and interprets it as a {@link BigDecimal}.
	 *
	 * @return the {@code BigDecimal} read
	 *
	 * @see #readBigInteger()
	 * @see #readVarInt()
	 */
	public BigDecimal readBigDecimal() {
		return new BigDecimal(readBigInteger(), readVarInt());
	}
	
	/**
	 * Reads {@code length} bytes from the buffer and interprets them as a {@link String}.
	 *
	 * @param length the number of bytes to read
	 *
	 * @return the {@link String} read
	 *
	 * @see #charset
	 * @see #readFixedLengthString(int, Charset)
	 */
	public String readFixedLengthString(final int length) {
		return readFixedLengthString(length, charset);
	}
	
	/**
	 * Reads {@code length} bytes from the buffer and interprets them as a {@link String}.
	 *
	 * @param length the number of bytes to read
	 *
	 * @return the {@link String} read
	 *
	 * @see #read(int)
	 * @see String#String(byte[], Charset)
	 */
	public String readFixedLengthString(final int length, final Charset charset) {
		return new String(read(length), charset);
	}
	
	public String readNullTerminatedString() {
		return readNullTerminatedString(charset);
	}
	
	public String readNullTerminatedString(final Charset charset) {
		return new String(readTo(0), charset);
	}
	
	/**
	 * Reads two longs from the buffer and interprets them as an {@link UUID}.
	 *
	 * @return the {@link UUID} read
	 *
	 * @see #readLong()
	 */
	public UUID readUUID() {
		final long msb, lsb;
		
		if(byteOrder.isBigEndian()) {
			msb = readLong();
			lsb = readLong();
		}else {
			lsb = readLong();
			msb = readLong();
		}
		
		return new UUID(msb, lsb);
	}
	
	/**
	 * Reads one long and one unsigned int from the buffer and interprets them as an {@link Instant}.
	 *
	 * @return the {@link Instant} read
	 *
	 * @see #readLong()
	 * @see #readUnsignedVarInt()
	 * @see Instant#ofEpochSecond(long, long)
	 */
	public Instant readInstant() {
		final long epoch;
		final int nano;
		
		if(byteOrder.isBigEndian()) {
			epoch = readLong();
			nano = readUnsignedVarInt();
		}else {
			nano = readUnsignedVarInt();
			epoch = readLong();
		}
		
		return Instant.ofEpochSecond(epoch, nano);
	}
	
	public LocalDate readLocalDate() {
		final int year;
		final byte month, day;
		
		if(byteOrder.isBigEndian()) {
			year = readVarInt();
			month = read();
			day = read();
		}else {
			day = read();
			month = read();
			year = readVarInt();
		}
		
		return LocalDate.of(year, month, day);
	}
	
	public LocalTime readLocalTime() {
		final byte hour, minute, second;
		final int nano;
		
		if(byteOrder.isBigEndian()) {
			hour = read();
			minute = read();
			second = read();
			nano = readUnsignedVarInt();
		}else {
			nano = readUnsignedVarInt();
			second = read();
			minute = read();
			hour = read();
		}
		
		return LocalTime.of(hour, minute, second, nano);
	}
	
	public LocalDateTime readLocalDateTime() {
		final LocalDate ld;
		final LocalTime lt;
		
		if(byteOrder.isBigEndian()) {
			ld = readLocalDate();
			lt = readLocalTime();
		}else {
			lt = readLocalTime();
			ld = readLocalDate();
		}
		
		return LocalDateTime.of(ld, lt);
	}
	
	public ZoneOffset readZoneOffset() {
		return ZoneOffset.ofTotalSeconds(readInt());
	}
	
	public OffsetDateTime readOffsetDateTime() {
		return OffsetDateTime.of(readLocalDateTime(), readZoneOffset());
	}
	
	public ZoneId readZoneId() {
		return ZoneId.of(readNullTerminatedString());
	}
	
	public ZonedDateTime readZonedDateTime() {
		return ZonedDateTime.ofLocal(readLocalDateTime(), readZoneId(), readZoneOffset());
	}
	
	public Duration readDuration() {
		final long seconds;
		final int nanos;
		
		if(byteOrder.isBigEndian()) {
			seconds = readVarLong();
			nanos = readVarInt();
		}else {
			nanos = readVarInt();
			seconds = readVarLong();
		}
		
		return Duration.ofSeconds(seconds, nanos);
	}
	
	public Period readPeriod() {
		final int years, months, days;
		
		if(byteOrder.isBigEndian()) {
			years = readVarInt();
			months = readVarInt();
			days = readVarInt();
		}else {
			days = readVarInt();
			months = readVarInt();
			years = readVarInt();
		}
		
		return Period.of(years, months, days);
	}
	
	@SuppressWarnings("MagicConstant")
	public Pattern readPattern() {
		return Pattern.compile(readString(), readUnsignedVarInt());
	}
	
	@SuppressWarnings("unchecked")
	public <B extends ByteBuffer, T extends IByteBufferable<B>> T read(final T obj) {
		obj.fromBuffer((B) this);
		
		return obj;
	}
	
	public byte[] readByteArray() {
		return read(readUnsignedVarInt());
	}
	
	public short[] readShortArray() {
		final int length = readUnsignedVarInt();
		final short[] shorts = new short[length];
		
		for(int i = 0; i < length; i++)
			shorts[i] = readShort();
		
		return shorts;
	}
	
	public int[] readIntArray() {
		final int length = readUnsignedVarInt();
		final int[] ints = new int[length];
		
		for(int i = 0; i < length; i++)
			ints[i] = readInt();
		
		return ints;
	}
	
	public long[] readLongArray() {
		final int length = readUnsignedVarInt();
		final long[] longs = new long[length];
		
		for(int i = 0; i < length; i++)
			longs[i] = readLong();
		
		return longs;
	}
	
	public float[] readFloatArray() {
		final int length = readUnsignedVarInt();
		final float[] floats = new float[length];
		
		for(int i = 0; i < length; i++)
			floats[i] = readFloat();
		
		return floats;
	}
	
	public double[] readDoubleArray() {
		final int length = readUnsignedVarInt();
		final double[] doubles = new double[length];
		
		for(int i = 0; i < length; i++)
			doubles[i] = readDouble();
		
		return doubles;
	}
	
	public boolean[] readBooleanArray() {
		final int length = readUnsignedVarInt();
		final boolean[] bools = new boolean[length];
		
		for(int i = 0; i < length; i++)
			bools[i] = readBoolean();
		
		return bools;
	}
	
	public char[] readCharArray() {
		final int length = readUnsignedVarInt();
		final char[] chars = new char[length];
		
		for(int i = 0; i < length; i++)
			chars[i] = readChar();
		
		return chars;
	}
	
	public <B extends ByteBuffer, T> T[] readArray(final Class<T> clazz, final Function<B, T> reader) {
		return readArray(arrayFromClassGenerator(clazz), reader);
	}
	
	public <B extends ByteBuffer, T> T[] readArray(final Class<T> clazz, final BiFunction<B, Integer, T> reader) {
		return readArray(arrayFromClassGenerator(clazz), reader);
	}
	
	@SuppressWarnings("unchecked")
	private <T> IntFunction<T[]> arrayFromClassGenerator(final Class<T> clazz) {
		return length -> (T[]) Array.newInstance(clazz, length);
	}
	
	public <B extends ByteBuffer, T> T[] readArray(final IntFunction<T[]> generator, final Function<B, T> reader) {
		return this.<B, T>readArray(generator, (buf, _index_) -> reader.apply(buf));
	}
	
	@SuppressWarnings("unchecked")
	public <B extends ByteBuffer, T> T[] readArray(final IntFunction<T[]> generator, final BiFunction<B, Integer, T> reader) {
		final int length = readUnsignedVarInt();
		final T[] array = generator.apply(length);
		
		lockWriting((B) this, buf -> {
			for(int i = 0; i < length; i++)
				array[i] = reader.apply(buf, i);
		});
		
		return array;
	}
	
	/**
	 * Skips {@code n} bytes by reading them.
	 *
	 * @param n the number how many bytes will be skipped
	 *
	 * @return the current instance
	 *
	 * @throws IllegalArgumentException if {@code n} is negative
	 * @see #read()
	 */
	public ByteBuffer skip(final int n) {
		if(n < 0) throw new IllegalArgumentException("Cannot skip a negative amount of bytes");
		
		for(int i = 0; i < n; i++)
			read();
		
		return this;
	}
	
	public ByteBuffer mark() {
		if(readLock) throw new IllegalStateException("Reading is locked");
		
		mark = readCursor;
		
		return this;
	}
	
	public ByteBuffer reset() {
		if(readLock) throw new IllegalStateException("Reading is locked");
		
		readCursor = mark;
		
		return this;
	}
	
	/**
	 * Clears this instance and sets both cursors to zero.
	 *
	 * @return the current instance
	 */
	public ByteBuffer clear() {
		if(writeLock || readLock) throw new IllegalStateException("Writing or reading is locked");
		
		bytes.clear();
		
		writeCursor = 0;
		readCursor = 0;
		
		return this;
	}
	
	/**
	 * Reverses all bytes stored.
	 *
	 * @return the current instance
	 */
	public ByteBuffer reverse() {
		if(writeLock || readLock) throw new IllegalStateException("Writing or reading is locked");
		
		bytes.reverse();
		
		return this;
	}
	
	/**
	 * Encodes this instance with {@linkplain Base64 BASE64}.
	 *
	 * @return the current instance
	 *
	 * @see Base64.Encoder#encode(byte[])
	 */
	public ByteBuffer encode() {
		if(writeLock || readLock) throw new IllegalStateException("Writing or reading is locked");
		
		bytes.encode();
		
		return this;
	}
	
	/**
	 * Decodes this instance with {@linkplain Base64 BASE64}.
	 *
	 * @return the current instance
	 *
	 * @see Base64.Decoder#decode(byte[])
	 */
	public ByteBuffer decode() {
		if(writeLock || readLock) throw new IllegalStateException("Writing or reading is locked");
		
		bytes.decode();
		
		return this;
	}
	
	/**
	 * Encrypts this instance with the {@link Encryption} {@code encryption}.
	 *
	 * @param encryption the {@link Encryption} that will be used to encrypt
	 *
	 * @return the current instance
	 *
	 * @see Encryption#encrypt(byte[])
	 */
	public ByteBuffer encrypt(final Encryption encryption) {
		if(writeLock || readLock) throw new IllegalStateException("Writing or reading is locked");
		
		bytes.encrypt(encryption);
		
		return this;
	}
	
	/**
	 * Decrypts this instance with the {@link Encryption} {@code encryption}.
	 *
	 * @param encryption the {@link Encryption} that will be used to decrypt
	 *
	 * @return the current instance
	 *
	 * @see Encryption#decrypt(byte[])
	 */
	public ByteBuffer decrypt(final Encryption encryption) {
		if(writeLock || readLock) throw new IllegalStateException("Writing or reading is locked");
		
		bytes.decrypt(encryption);
		
		return this;
	}
	
	/**
	 * Transfers all bytes of this instance to the {@code ByteBuffer buf}.
	 *
	 * @param buf the instance to transfer to
	 *
	 * @return the current instance
	 *
	 * @see #readAll()
	 */
	public ByteBuffer transferTo(final ByteBuffer buf) {
		buf.write(readAll());
		
		return this;
	}
	
	/**
	 * Transfers all bytes of the {@code ByteBuffer buf} to this instance.
	 *
	 * @param buf the instance to transfer from
	 *
	 * @return the current instance
	 *
	 * @see #readAll()
	 */
	public ByteBuffer transferFrom(final ByteBuffer buf) {
		return write(buf.readAll());
	}
	
	/**
	 * Writes all non-written bytes of this instance to the {@link OutputStream} {@code out}.
	 *
	 * @param out the {@link OutputStream} to which it will write
	 *
	 * @return the current instance
	 *
	 * @throws IOException if an I/O error occurs
	 * @see #readAll()
	 */
	public ByteBuffer writeToOutputStream(final OutputStream out) throws IOException {
		out.write(readAll());
		out.flush();
		
		return this;
	}
	
	/**
	 * Reads all bytes from the {@link InputStream} {@code in} and stores it in the current instance.
	 *
	 * @param in the {@link InputStream} from which it will read
	 *
	 * @return the current instance
	 *
	 * @throws IOException if an I/O error occurs
	 */
	public ByteBuffer readFromInputStream(final InputStream in) throws IOException {
		while(in.available() > 0)
			write(in.read());
		
		return this;
	}
	
	public InputStream createInputStream() {
		return in = new ByteBufferInputStream();
	}
	
	public InputStream getInputStream() {
		return in != null ? in : createInputStream();
	}
	
	public OutputStream createOutputStream() {
		return out = new ByteBufferOutputStream();
	}
	
	public OutputStream createOutputStream(final Runnable onFlush) {
		return out = new ByteBufferOutputStream(onFlush);
	}
	
	public OutputStream getOutputStream() {
		return out != null ? out : createOutputStream();
	}
	
	/**
	 * @return whether this buffer is empty
	 *
	 * @see #getSize()
	 */
	public boolean isEmpty() {
		return getSize() <= 0;
	}
	
	/**
	 * Sets the limit of this instance.
	 *
	 * @param limit the new limit of this instance
	 *
	 * @return the current instance
	 */
	public ByteBuffer setLimit(final int limit) {
		this.limit = limit;
		
		if(bytes.size() > limit) setSize(limit);
		
		return this;
	}
	
	/**
	 * Gets the size of this buffer.
	 *
	 * @return the size of this buffer
	 */
	public int getSize() {
		return bytes.size() - readCursor;
	}
	
	/**
	 * Sets the size of this buffer.
	 *
	 * @param size the new size of this buffer
	 *
	 * @return the current instance
	 */
	public ByteBuffer setSize(final int size) {
		while(getSize() < size)
			bytes.add(0);
		
		if(getSize() > size) {
			if(writeCursor > limit) writeCursor = size;
			if(readCursor > size) readCursor = size;
			
			bytes.removeN(getSize() - size);
		}
		
		return this;
	}
	
	/**
	 * Gets all bytes of this instance.
	 *
	 * @return all bytes
	 */
	public byte[] getAllBytes() {
		return bytes.toArray();
	}
	
	/**
	 * Returns all non-written bytes of this instance in form of a byte array.
	 *
	 * @return the non-written bytes
	 */
	public byte[] toByteArray() {
		return bytes.toArray(readCursor);
	}
	
	@Override
	public String toString() {
		final Stream.Builder<String> sb = Stream.builder();
		
		for(final byte b : toByteArray())
			sb.add(String.valueOf(b & 0xFF));
		
		return sb.build().collect(Collectors.joining(", ", "[", "]"));
	}
	
	public String toHexString() {
		final StringBuilder sb = new StringBuilder();
		
		for(final byte b : toByteArray())
			sb.append(String.format("%02X", b));
		
		return sb.toString();
	}
	
	public String toOctalString() {
		final StringBuilder sb = new StringBuilder();
		
		for(final byte b : toByteArray())
			sb.append(String.format("%03o", b));
		
		return sb.toString();
	}
	
	public ByteBuffer toReversed() {
		return copy().reverse();
	}
	
	/**
	 * Constructs a new instance containing all bytes of this instance encoded with {@linkplain Base64 BASE64}.
	 *
	 * @return the newly constructed instance
	 *
	 * @see #copy()
	 * @see #encode()
	 */
	public ByteBuffer toEncoded() {
		return copy().encode();
	}
	
	/**
	 * Constructs a new instance containing all bytes of this instance decoded with {@linkplain Base64 BASE64}.
	 *
	 * @return the newly constructed instance
	 *
	 * @see #copy()
	 * @see #decode()
	 */
	public ByteBuffer toDecoded() {
		return copy().decode();
	}
	
	public ByteBuffer toEncrypted(final Encryption encryption) {
		return copy().encrypt(encryption);
	}
	
	public ByteBuffer toDecrypted(final Encryption encryption) {
		return copy().decrypt(encryption);
	}
	
	/**
	 * Copies this instance by constructing a new one.
	 *
	 * @return the newly constructed instance
	 *
	 * @see #ByteBuffer(ByteBuffer)
	 */
	public ByteBuffer copy() {
		return new ByteBuffer(this);
	}
	
	public <B extends ByteBuffer> B copy(final ByteBufferGenerator<B> generator) {
		final B buf = generator.generate();
		
		buf.byteOrder = byteOrder;
		buf.charset = charset;
		
		buf.write(toByteArray());
		
		return buf;
	}
	
	/**
	 * Reads all bytes from the {@link InputStream} {@code in} and stores them in a new instance.
	 *
	 * @param in the {@link InputStream} from which the method will read
	 *
	 * @return the new instance
	 *
	 * @throws IOException if an I/O error occurs
	 * @see #empty()
	 * @see #readFromInputStream(InputStream)
	 */
	public static ByteBuffer fromInputStream(final InputStream in) throws IOException {
		return empty().readFromInputStream(in);
	}
	
	public static ByteBuffer fromHexString(final String s) {
		final ByteBuffer buf = empty();
		
		for(int i = 0; i < s.length(); i += 2)
			buf.write(Integer.parseInt(s.substring(i, i + 2), 16));
		
		return buf;
	}
	
	public static ByteBuffer fromOctalString(final String s) {
		final ByteBuffer buf = empty();
		
		for(int i = 0; i < s.length(); i += 3)
			buf.write(Integer.parseInt(s.substring(i, i + 3), 8));
		
		return buf;
	}
	
	public static ByteBuffer fromEncoded(final ByteBuffer buf) {
		return buf.toDecoded();
	}
	
	public static ByteBuffer fromEncrypted(final ByteBuffer buf, final Encryption encryption) {
		return buf.toDecrypted(encryption);
	}
	
	/**
	 * Returns a new empty {@code ByteBuffer}.
	 *
	 * @return the new empty instance
	 *
	 * @see #ByteBuffer()
	 */
	public static ByteBuffer empty() {
		return new ByteBuffer();
	}
	
	public static <B extends ByteBuffer> B empty(final ByteBufferGenerator<B> generator) {
		return generator.generate();
	}
	
	private class ByteBufferInputStream extends InputStream {
		
		@Override
		public int read() {
			return isEmpty() ? -1 : readUnsignedByte();
		}
		
		@Override
		public int available() {
			return getSize();
		}
	}
	
	private class ByteBufferOutputStream extends OutputStream {
		private final Runnable onFlush;
		
		public ByteBufferOutputStream() {
			this(null);
		}
		
		public ByteBufferOutputStream(final Runnable onFlush) {
			this.onFlush = onFlush;
		}
		
		@Override
		public void write(final int b) {
			ByteBuffer.this.write(b);
		}
		
		@Override
		public void flush() {
			if(onFlush != null) onFlush.run();
		}
	}
	
	private static class ByteList {
		private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
		private static final int DEFAULT_SIZE = 16;
		
		private byte[] data = {};
		
		@Getter
		@Accessors(fluent = true)
		private int size = 0;
		
		private void checkBounds() {
			final int minCap = size + 1;
			final int oldCap = data.length;
			
			if(minCap > oldCap) {
				int newCap = oldCap + (oldCap >> 1);
				
				if(newCap < minCap) {
					if(oldCap == 0) newCap = DEFAULT_SIZE;
					else newCap = minCap;
				}else if(newCap > MAX_ARRAY_SIZE) newCap = MAX_ARRAY_SIZE;
				
				data = Arrays.copyOf(data, newCap);
			}
		}
		
		private byte[] shrink() {
			return Arrays.copyOf(data, size);
		}
		
		private void setData(final byte[] data) {
			this.data = data;
			
			size = data.length;
		}
		
		private void changeData(final Function<byte[], byte[]> dataFunc) {
			setData(dataFunc.apply(shrink()));
		}
		
		public byte get(final int index) {
			Objects.checkIndex(index, size);
			
			return data[index];
		}
		
		public void add(final byte b) {
			checkBounds();
			
			data[size++] = b;
		}
		
		public void add(final int b) {
			add((byte) b);
		}
		
		public void removeN(final int n) {
			size -= n;
		}
		
		public void clear() {
			size = 0;
		}
		
		public void reverse() {
			final int mid = size >> 1;
			
			byte tmp;
			for(int i = 0, j = size - 1; i < mid; i++, j--) {
				tmp = data[i];
				data[i] = data[j];
				data[j] = tmp;
			}
		}
		
		public byte[] toArray() {
			return Arrays.copyOf(data, size);
		}
		
		public byte[] toArray(final int from) {
			return toArray(from, size);
		}
		
		public byte[] toArray(final int from, final int to) {
			return Arrays.copyOfRange(data, from, to);
		}
		
		public void encode() {
			changeData(ENCODER::encode);
		}
		
		public void decode() {
			changeData(DECODER::decode);
		}
		
		public void encrypt(final Encryption encryption) {
			changeData(encryption::encrypt);
		}
		
		public void decrypt(final Encryption encryption) {
			changeData(encryption::decrypt);
		}
	}
	
	private static class TypeNotRegisteredException extends IllegalArgumentException {
		private static final long serialVersionUID = -9212880267233459213L;
		
		private final Class<?> type;
		
		public TypeNotRegisteredException(final Class<?> type) {
			super("The object type '" + type.getName() + "' is not registered");
			
			this.type = type;
		}
	}
	
	private static class TypesNotRegisteredException extends IllegalArgumentException {
		private static final long serialVersionUID = -9212880267233459213L;
		
		public TypesNotRegisteredException(final Set<Class<?>> types) {
			super("The object types " + types.stream().map(Class::getName).collect(Collectors.joining(", ", "[", "]")) + " are not registered");
		}
	}
	
	public enum ByteOrder {
		BIG_ENDIAN,
		LITTLE_ENDIAN;
		
		public boolean isBigEndian() {
			return this == BIG_ENDIAN;
		}
		
		public boolean isLittleEndian() {
			return this == LITTLE_ENDIAN;
		}
		
		public boolean isNative() {
			return this == nativeOrder();
		}
		
		public static ByteOrder from(final java.nio.ByteOrder order) {
			return order.equals(java.nio.ByteOrder.BIG_ENDIAN) ? BIG_ENDIAN : LITTLE_ENDIAN;
		}
		
		public static ByteOrder nativeOrder() {
			return from(java.nio.ByteOrder.nativeOrder());
		}
	}
	
	protected static <T> void addToCache(final Class<T> clazz, final Consumer<T> consumer) {
		addToCache(clazz, (buf, value) -> consumer.accept(value));
	}
	
	@SuppressWarnings("unchecked")
	protected static <B extends ByteBuffer, T> void addToCache(final Class<T> clazz, final BiConsumer<B, T> consumer) {
		CACHE.put(clazz, (BiConsumer<ByteBuffer, Object>) consumer);
	}
	
	private static <B extends ByteBuffer> void lockWriting(final B buf, final Consumer<B> consumer) {
		((ByteBuffer) buf).writeLock = true;
		
		consumer.accept(buf);
		
		((ByteBuffer) buf).writeLock = false;
	}
	
	private static <B extends ByteBuffer> void lockReading(final B buf, final Consumer<B> consumer) {
		((ByteBuffer) buf).readLock = true;
		
		consumer.accept(buf);
		
		((ByteBuffer) buf).readLock = false;
	}
}
