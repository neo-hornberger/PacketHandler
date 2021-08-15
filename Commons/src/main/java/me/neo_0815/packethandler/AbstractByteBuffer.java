package me.neo_0815.packethandler;

import me.neo_0815.encryption.Encryption;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.schema.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.time.*;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public abstract class AbstractByteBuffer<B extends AbstractByteBuffer<B>> extends ByteBuffer {
	private final B INSTANCE = cast(this);
	
	public AbstractByteBuffer() {
	}
	
	public AbstractByteBuffer(final byte[] bytes) {
		super(bytes);
	}
	
	public AbstractByteBuffer(final ByteBuffer buffer) {
		super(buffer);
	}
	
	@Override
	public B write(final byte b) {
		super.write(b);
		
		return INSTANCE;
	}
	
	@Override
	public B write(final int b) {
		super.write(b);
		
		return INSTANCE;
	}
	
	@Override
	public B write(final byte[] bytes) {
		super.write(bytes);
		
		return INSTANCE;
	}
	
	@Override
	public B write(final byte[] bytes, final int n) {
		super.write(bytes, n);
		
		return INSTANCE;
	}
	
	@Override
	public B writeShort(final short s) {
		super.writeShort(s);
		
		return INSTANCE;
	}
	
	@Override
	public B writeShort(final int s) {
		super.writeShort(s);
		
		return INSTANCE;
	}
	
	@Override
	public B writeInt(final int i) {
		super.writeInt(i);
		
		return INSTANCE;
	}
	
	@Override
	public B writeLong(final long l) {
		super.writeLong(l);
		
		return INSTANCE;
	}
	
	@Override
	public B writeFloat(final float f) {
		super.writeFloat(f);
		
		return INSTANCE;
	}
	
	@Override
	public B writeDouble(final double d) {
		super.writeDouble(d);
		
		return INSTANCE;
	}
	
	@Override
	public B writeBoolean(final boolean b) {
		super.writeBoolean(b);
		
		return INSTANCE;
	}
	
	@Override
	public B writeChar(final char c) {
		super.writeChar(c);
		
		return INSTANCE;
	}
	
	@Override
	public B writeString(final String s) {
		super.writeString(s);
		
		return INSTANCE;
	}
	
	@Override
	public B writeString(final String s, final Charset charset) {
		super.writeString(s, charset);
		
		return INSTANCE;
	}
	
	@Override
	public B writeString(final CharSequence cs) {
		super.writeString(cs);
		
		return INSTANCE;
	}
	
	@Override
	public <T extends Enum<T>> B writeEnum(final Enum<T> e) {
		super.writeEnum(e);
		
		return INSTANCE;
	}
	
	@Override
	public B writeUnsignedByte(final int ubyte) {
		super.writeUnsignedByte(ubyte);
		
		return INSTANCE;
	}
	
	@Override
	public B writeUnsignedShort(final int ushort) {
		super.writeUnsignedShort(ushort);
		
		return INSTANCE;
	}
	
	@Override
	public B writeUnsignedInt(final long uint) {
		super.writeUnsignedInt(uint);
		
		return INSTANCE;
	}
	
	@Override
	public B writeVarInt(final int vi) {
		super.writeVarInt(vi);
		
		return INSTANCE;
	}
	
	@Override
	public B writeVarLong(final long vl) {
		super.writeVarLong(vl);
		
		return INSTANCE;
	}
	
	@Override
	public B writeVarBigInt(final BigInteger vbi) {
		super.writeVarBigInt(vbi);
		
		return INSTANCE;
	}
	
	@Override
	public B writeUnsignedVarInt(final int vi) {
		super.writeUnsignedVarInt(vi);
		
		return INSTANCE;
	}
	
	@Override
	public B writeUnsignedVarLong(final long vl) {
		super.writeUnsignedVarLong(vl);
		
		return INSTANCE;
	}
	
	@Override
	public B writeUnsignedVarBigInt(final BigInteger vbi) {
		super.writeUnsignedVarBigInt(vbi);
		
		return INSTANCE;
	}
	
	@Override
	public B writeBigInteger(final BigInteger bi) {
		super.writeBigInteger(bi);
		
		return INSTANCE;
	}
	
	@Override
	public B writeBigDecimal(final BigDecimal bd) {
		super.writeBigDecimal(bd);
		
		return INSTANCE;
	}
	
	@Override
	public B writeFixedLengthString(final String s, final int length) {
		super.writeFixedLengthString(s, length);
		
		return INSTANCE;
	}
	
	@Override
	public B writeFixedLengthString(final String s, final Charset charset, final int length) {
		super.writeFixedLengthString(s, charset, length);
		
		return INSTANCE;
	}
	
	@Override
	public B writeNullTerminatedString(final String s) {
		super.writeNullTerminatedString(s);
		
		return INSTANCE;
	}
	
	@Override
	public B writeNullTerminatedString(final String s, final Charset charset) {
		super.writeNullTerminatedString(s, charset);
		
		return INSTANCE;
	}
	
	@Override
	public B writeUUID(final UUID u) {
		super.writeUUID(u);
		
		return INSTANCE;
	}
	
	@Override
	public B writeInstant(final Instant i) {
		super.writeInstant(i);
		
		return INSTANCE;
	}
	
	@Override
	public B writeLocalDate(final LocalDate ld) {
		super.writeLocalDate(ld);
		
		return INSTANCE;
	}
	
	@Override
	public B writeLocalTime(final LocalTime lt) {
		super.writeLocalTime(lt);
		
		return INSTANCE;
	}
	
	@Override
	public B writeLocalDateTime(final LocalDateTime ldt) {
		super.writeLocalDateTime(ldt);
		
		return INSTANCE;
	}
	
	@Override
	public B writeZoneOffset(final ZoneOffset zo) {
		super.writeZoneOffset(zo);
		
		return INSTANCE;
	}
	
	@Override
	public B writeOffsetDateTime(final OffsetDateTime odt) {
		super.writeOffsetDateTime(odt);
		
		return INSTANCE;
	}
	
	@Override
	public B writeZoneId(final ZoneId zi) {
		super.writeZoneId(zi);
		
		return INSTANCE;
	}
	
	@Override
	public B writeZonedDateTime(final ZonedDateTime zdt) {
		super.writeZonedDateTime(zdt);
		
		return INSTANCE;
	}
	
	@Override
	public B writeDuration(final Duration d) {
		super.writeDuration(d);
		
		return INSTANCE;
	}
	
	@Override
	public B writePeriod(final Period p) {
		super.writePeriod(p);
		
		return INSTANCE;
	}
	
	@Override
	public B writePattern(final Pattern p) {
		super.writePattern(p);
		
		return INSTANCE;
	}
	
	@Override
	public <BT extends ByteBuffer, T> B write(final Schema<T, BT> schema, final T obj) {
		super.write(obj);
		
		return INSTANCE;
	}
	
	@Override
	public <BT extends ByteBuffer> ByteBuffer writePacket(final PacketBase<BT> packet) {
		super.writePacket(packet);
		
		return INSTANCE;
	}
	
	@Override
	public B writeByteArray(final byte[] bytes) {
		super.writeArray(bytes);
		
		return INSTANCE;
	}
	
	@Override
	public B writeShortArray(final short[] shorts) {
		super.writeArray(shorts);
		
		return INSTANCE;
	}
	
	@Override
	public B writeIntArray(final int[] ints) {
		super.writeArray(ints);
		
		return INSTANCE;
	}
	
	@Override
	public B writeLongArray(final long[] longs) {
		super.writeArray(longs);
		
		return INSTANCE;
	}
	
	@Override
	public B writeFloatArray(final float[] floats) {
		super.writeArray(floats);
		
		return INSTANCE;
	}
	
	@Override
	public B writeDoubleArray(final double[] doubles) {
		super.writeArray(doubles);
		
		return INSTANCE;
	}
	
	@Override
	public B writeBooleanArray(final boolean[] bools) {
		super.writeArray(bools);
		
		return INSTANCE;
	}
	
	@Override
	public B writeCharArray(final char[] chars) {
		super.writeArray(chars);
		
		return INSTANCE;
	}
	
	@Override
	public <BT extends ByteBuffer, T> B writeArray(final T[] objs, final Schema<T, BT> schema) {
		super.writeArray(objs, schema);
		
		return INSTANCE;
	}
	
	@Override
	public <T> B writeArray(final T[] objs, final Consumer<T> writer) {
		super.writeArray(objs, writer);
		
		return INSTANCE;
	}
	
	@Override
	public <BT extends ByteBuffer, T> B writeArray(final T[] objs, final BiConsumer<BT, T> writer) {
		super.writeArray(objs, writer);
		
		return INSTANCE;
	}
	
	@Override
	public B skip(final int n) {
		super.skip(n);
		
		return INSTANCE;
	}
	
	@Override
	public B mark() {
		super.mark();
		
		return INSTANCE;
	}
	
	@Override
	public B reset() {
		super.reset();
		
		return INSTANCE;
	}
	
	@Override
	public B clear() {
		super.clear();
		
		return INSTANCE;
	}
	
	@Override
	public B reverse() {
		super.reverse();
		
		return INSTANCE;
	}
	
	@Override
	public B encode() {
		super.encode();
		
		return INSTANCE;
	}
	
	@Override
	public B decode() {
		super.decode();
		
		return INSTANCE;
	}
	
	@Override
	public B encrypt(final Encryption encryption) {
		super.encrypt(encryption);
		
		return INSTANCE;
	}
	
	@Override
	public B decrypt(final Encryption encryption) {
		super.decrypt(encryption);
		
		return INSTANCE;
	}
	
	@Override
	public B transferTo(final ByteBuffer buf) {
		super.transferTo(buf);
		
		return INSTANCE;
	}
	
	@Override
	public B transferFrom(final ByteBuffer buf) {
		super.transferFrom(buf);
		
		return INSTANCE;
	}
	
	@Override
	public B writeToOutputStream(final OutputStream out) throws IOException {
		super.writeToOutputStream(out);
		
		return INSTANCE;
	}
	
	@Override
	public B readFromInputStream(final InputStream in) throws IOException {
		super.readFromInputStream(in);
		
		return INSTANCE;
	}
	
	@Override
	public B setLimit(final int limit) {
		super.setLimit(limit);
		
		return INSTANCE;
	}
	
	@Override
	public B setSize(final int size) {
		super.setSize(size);
		
		return INSTANCE;
	}
	
	@Override
	public B toReversed() {
		return cast(super.toReversed());
	}
	
	@Override
	public B toEncoded() {
		return cast(super.toEncoded());
	}
	
	@Override
	public B toDecoded() {
		return cast(super.toDecoded());
	}
	
	@Override
	public B toEncrypted(final Encryption encryption) {
		return cast(super.toEncrypted(encryption));
	}
	
	@Override
	public B toDecrypted(final Encryption encryption) {
		return cast(super.toDecrypted(encryption));
	}
	
	@Override
	public abstract B copy();
	
	@SuppressWarnings("unchecked")
	private B cast(final ByteBuffer buf) {
		return (B) buf;
	}
}
