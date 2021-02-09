package me.neo_0815.packethandler;

import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;

import java.util.LinkedList;
import java.util.List;

public final class PacketConstructionMode {
	public static final PacketConstructionMode DEFAULT = new PacketConstructionMode(EncodingStrategy.DEFAULT, DecodingStrategy.DEFAULT);
	
	private final EncodingStrategy.EncodingConsumer encoding;
	private final List<DecodingStrategy.DecodingFunction<?>> decoding;
	
	public PacketConstructionMode(final EncodingStrategy encoding, final DecodingStrategy decoding) {
		this.encoding = encoding.consumer;
		this.decoding = decoding.functions;
	}
	
	public ByteBuffer encodePacket(final ByteBufferGenerator<?> generator, final PacketBase<?> packet, final long id, final AbstractPacketRegistry registry) {
		if(!registry.hasOutgoingPacket(id) && !(packet instanceof UnknownPacket))
			return encodeUnknownPacket(generator, PacketHelper.toBuffer(generator, packet), id);
		
		return encodePacket0(generator, packet, id);
	}
	
	public ByteBuffer encodePacket(final ByteBufferGenerator<?> generator, final long id, final PacketMap map, final AbstractPacketRegistry registry) {
		if(!registry.hasOutgoingPacket(id))
			return encodeUnknownPacket(generator, PacketHelper.toBuffer(generator, map), id);
		
		final PacketBase<?> packet = registry.getOutgoingPacket(id).get();
		
		if(map != null) packet.fromMap(map);
		
		return encodePacket0(generator, packet, id);
	}
	
	private ByteBuffer encodePacket0(final ByteBufferGenerator<?> generator, final PacketBase<?> packet, final long id) {
		final ByteBuffer buf = generator.generate(), content = generator.generate();
		int length = -1;
		
		content.write(packet);
		
		if(packet instanceof UnknownPacket) length = ((UnknownPacket) packet).length;
		if(length < 0) length = content.getSize();
		
		encoding.encode(buf, content, packet, id, length);
		
		return buf;
	}
	
	private ByteBuffer encodeUnknownPacket(final ByteBufferGenerator<?> generator, final ByteBuffer buf, final long id) {
		final UnknownPacket packet = new UnknownPacket();
		
		packet.fromMap(PacketMap.of("bytes", buf.toByteArray()));
		
		return encodePacket0(generator, packet, id);
	}
	
	public PacketIdPair decodePacket(final ByteBufferGenerator<?> generator, final ByteBuffer buf, final AbstractPacketRegistry registry) {
		final ByteBuffer content = generator.generate();
		
		long id = UnknownPacket.ID;
		boolean unknownPacket = true;
		int length = -1;
		
		try {
			for(final DecodingStrategy.DecodingFunction<?> function : decoding) {
				buf.mark();
				
				switch(function.type()) {
					case ID:
						id = (long) function.decode(buf, content, id, length);
						unknownPacket = false;
						break;
					case LENGTH:
						length = (int) function.decode(buf, content, id, length);
						break;
					case CONTENT:
						function.decode(buf, content, id, length);
						break;
				}
			}
		}catch(final IllegalStateException e) {
			unknownPacket = true;
		}
		
		final PacketBase<?> packet;
		
		if(!unknownPacket && registry.hasIncomingPacket(id)) packet = registry.getIncomingPacket(id).get();
		else {
			packet = new UnknownPacket(id, length);
			
			if(unknownPacket) {
				buf.reset();
				buf.transferTo(content);
			}
		}
		
		content.read(packet);
		
		return new PacketIdPair(packet, id);
	}
	
	public static int calcVarNumberLength(long vl) {
		int length = 0;
		
		do {
			vl >>>= 7;
			
			length++;
		}while(vl != 0);
		
		return length;
	}
	
	@Value
	@Accessors(fluent = true)
	public static class PacketIdPair {
		PacketBase<?> packet;
		long id;
	}
	
	public static class EncodingStrategy {
		private static final EncodingStrategy DEFAULT = new EncodingStrategy().id().length().content();
		
		private EncodingConsumer consumer = EncodingConsumer.DEFAULT;
		
		private EncodingStrategy add(final EncodingConsumer after) {
			consumer = consumer.andThen(after);
			
			return this;
		}
		
		public EncodingStrategy id() {
			return id(IdEncoding.DEFAULT);
		}
		
		public EncodingStrategy id(final IdEncoding consumer) {
			return add(consumer);
		}
		
		public EncodingStrategy length() {
			return length(LengthEncoding.DEFAULT);
		}
		
		public EncodingStrategy length(final LengthEncoding consumer) {
			return add(consumer);
		}
		
		public EncodingStrategy length(final LengthIdEncoding consumer) {
			return add(consumer);
		}
		
		public EncodingStrategy content() {
			return content(ContentEncoding.DEFAULT);
		}
		
		public EncodingStrategy content(final ContentEncoding consumer) {
			return add(consumer);
		}
		
		public EncodingStrategy content(final ContentIdEncoding consumer) {
			return add(consumer);
		}
		
		@FunctionalInterface
		private interface EncodingConsumer {
			
			void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id, final int length);
			
			default EncodingConsumer andThen(@NonNull final EncodingConsumer after) {
				return (buf, content, packet, id, length) -> {
					encode(buf, content, packet, id, length);
					after.encode(buf, content, packet, id, length);
				};
			}
			
			EncodingConsumer DEFAULT = (buf, content, packet, id, length) -> {
			};
		}
		
		@FunctionalInterface
		public interface IdEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final long id);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id, final int length) {
				encode(buf, id);
			}
			
			IdEncoding DEFAULT = ByteBuffer::writeLong;
		}
		
		@FunctionalInterface
		public interface LengthEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final int length);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id, final int length) {
				encode(buf, length);
			}
			
			LengthEncoding DEFAULT = ByteBuffer::writeInt;
		}
		
		@FunctionalInterface
		public interface LengthIdEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final int length, final long id);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id, final int length) {
				encode(buf, length, id);
			}
			
			LengthIdEncoding ID_INT = (buf, length, id) -> buf.writeInt(length + 4);
			LengthIdEncoding ID_LONG = (buf, length, id) -> buf.writeInt(length + 8);
			LengthIdEncoding ID_VARNUM = (buf, length, id) -> buf.writeInt(length + calcVarNumberLength(id));
		}
		
		@FunctionalInterface
		public interface ContentEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final ByteBuffer content);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id, final int length) {
				encode(buf, content);
			}
			
			ContentEncoding DEFAULT = ByteBuffer::transferFrom;
		}
		
		@FunctionalInterface
		public interface ContentIdEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final ByteBuffer content, final long id);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id, final int length) {
				encode(buf, content, id);
			}
			
			ContentIdEncoding DEFAULT = (buf, content, id) -> buf.transferFrom(content);
		}
	}
	
	public static class DecodingStrategy {
		public static final DecodingStrategy DEFAULT = new DecodingStrategy().id().length().content();
		
		private final List<DecodingFunction<?>> functions = new LinkedList<>();
		
		private DecodingStrategy add(final DecodingFunction<?> after) {
			functions.add(after);
			
			return this;
		}
		
		public DecodingStrategy id() {
			return id(IdDecoding.DEFAULT);
		}
		
		public DecodingStrategy id(final IdDecoding consumer) {
			return add(consumer);
		}
		
		public DecodingStrategy length() {
			return length(LengthDecoding.DEFAULT);
		}
		
		public DecodingStrategy length(final LengthDecoding consumer) {
			return add(consumer);
		}
		
		public DecodingStrategy length(final LengthIdDecoding consumer) {
			return add(consumer);
		}
		
		public DecodingStrategy modifyLength(final ModifyLengthDecoding consumer) {
			return add(consumer);
		}
		
		public DecodingStrategy modifyLength(final ModifyLengthIdDecoding consumer) {
			return add(consumer);
		}
		
		public DecodingStrategy content() {
			return content(ContentDecoding.DEFAULT);
		}
		
		public DecodingStrategy content(final ContentDecoding consumer) {
			return add(consumer);
		}
		
		public DecodingStrategy content(final ContentIdDecoding consumer) {
			return add(consumer);
		}
		
		private interface DecodingFunction<T> {
			
			T decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length);
			
			DecodingFunctionType type();
		}
		
		private enum DecodingFunctionType {
			ID,
			LENGTH,
			CONTENT
		}
		
		@FunctionalInterface
		public interface IdDecoding extends DecodingFunction<Long> {
			
			long decode(final ByteBuffer buf);
			
			@Override
			default Long decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length) {
				return decode(buf);
			}
			
			@Override
			default DecodingFunctionType type() {
				return DecodingFunctionType.ID;
			}
			
			IdDecoding DEFAULT = ByteBuffer::readLong;
		}
		
		@FunctionalInterface
		public interface LengthDecoding extends DecodingFunction<Integer> {
			
			int decode(final ByteBuffer buf);
			
			@Override
			default Integer decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length) {
				return decode(buf);
			}
			
			@Override
			default DecodingFunctionType type() {
				return DecodingFunctionType.LENGTH;
			}
			
			LengthDecoding DEFAULT = ByteBuffer::readInt;
		}
		
		@FunctionalInterface
		public interface LengthIdDecoding extends DecodingFunction<Integer> {
			
			int decode(final ByteBuffer buf, final long id);
			
			@Override
			default Integer decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length) {
				return decode(buf, id);
			}
			
			@Override
			default DecodingFunctionType type() {
				return DecodingFunctionType.LENGTH;
			}
			
			LengthIdDecoding ID_INT = (buf, id) -> buf.readInt() - 4;
			LengthIdDecoding ID_LONG = (buf, id) -> buf.readInt() - 8;
		}
		
		@FunctionalInterface
		public interface ModifyLengthDecoding extends DecodingFunction<Integer> {
			
			int decode(final int length);
			
			@Override
			default Integer decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length) {
				return decode(length);
			}
			
			@Override
			default DecodingFunctionType type() {
				return DecodingFunctionType.LENGTH;
			}
		}
		
		@FunctionalInterface
		public interface ModifyLengthIdDecoding extends DecodingFunction<Integer> {
			
			int decode(final int length, final long id);
			
			@Override
			default Integer decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length) {
				return decode(length, id);
			}
			
			@Override
			default DecodingFunctionType type() {
				return DecodingFunctionType.LENGTH;
			}
			
			ModifyLengthIdDecoding ID_VARNUM = (length, id) -> length - calcVarNumberLength(id);
		}
		
		@FunctionalInterface
		public interface ContentDecoding extends DecodingFunction<Void> {
			
			void decode(final ByteBuffer buf, final ByteBuffer content, final int length);
			
			@Override
			default Void decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length) {
				decode(buf, content, length);
				
				return null;
			}
			
			@Override
			default DecodingFunctionType type() {
				return DecodingFunctionType.CONTENT;
			}
			
			ContentDecoding DEFAULT = (buf, content, length) -> content.write(buf.read(length));
		}
		
		@FunctionalInterface
		public interface ContentIdDecoding extends DecodingFunction<Void> {
			
			void decode(final ByteBuffer buf, final ByteBuffer content, final int length, final long id);
			
			@Override
			default Void decode(final ByteBuffer buf, final ByteBuffer content, final long id, final int length) {
				decode(buf, content, length, id);
				
				return null;
			}
			
			@Override
			default DecodingFunctionType type() {
				return DecodingFunctionType.CONTENT;
			}
			
			ContentIdDecoding DEFAULT = (buf, content, length, id) -> content.write(buf.read(length));
		}
	}
}
