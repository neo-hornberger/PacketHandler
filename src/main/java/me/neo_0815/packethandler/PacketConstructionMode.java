package me.neo_0815.packethandler;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.Accessors;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;

@AllArgsConstructor
public final class PacketConstructionMode {
	public static final int ID = 0;
	public static final int ID_INT = 1;
	public static final int ID_VARINT = 2;
	public static final int ID_VARLONG = 3;
	
	public static final int LENGTH_CONTENT = 100;
	public static final int LENGTH_CONTENT_ID = 101;
	public static final int LENGTH_CONTENT_ID_INT = 102;
	public static final int LENGTH_CONTENT_ID_VARINT = 103;
	public static final int LENGTH_CONTENT_ID_VARLONG = 104;
	
	public static final int LENGTH_VARINT_CONTENT = 200;
	public static final int LENGTH_VARINT_CONTENT_ID = 201;
	public static final int LENGTH_VARINT_CONTENT_ID_INT = 202;
	public static final int LENGTH_VARINT_CONTENT_ID_VARINT = 203;
	public static final int LENGTH_VARINT_CONTENT_ID_VARLONG = 204;
	
	public static final int CONTENT = 300;
	
	public static final PacketConstructionMode DEFAULT = createMode(ID, LENGTH_CONTENT, CONTENT);
	
	private final int[] modes;
	
	public ByteBuffer encodePacket(final ByteBufferGenerator<?> generator, final PacketBase<?> packet, final long id, final AbstractPacketRegistry registry) {
		if(!registry.hasOutgoingPacket(id))
			return encodeUnknownPacket(generator, PacketHelper.toBuffer(generator, packet));
		
		return encodePacket0(generator, packet, id);
	}
	
	public ByteBuffer encodePacket(final ByteBufferGenerator<?> generator, final long id, final PacketMap map, final AbstractPacketRegistry registry) {
		if(!registry.hasOutgoingPacket(id))
			return encodeUnknownPacket(generator, PacketHelper.toBuffer(generator, map));
		
		final PacketBase<?> packet = registry.getOutgoingPacket(id).get();
		
		if(map != null) packet.fromMap(map);
		
		return encodePacket0(generator, packet, id);
	}
	
	private ByteBuffer encodePacket0(final ByteBufferGenerator<?> generator, final PacketBase<?> packet, final long id) {
		final ByteBuffer buf = generator.generate(), content = generator.generate();
		
		if(id == UnknownPacket.ID) buf.write(packet);
		else {
			content.write(packet);
			
			for(final int mode : modes)
				switch(mode) {
					case ID:
						buf.writeLong(id);
						break;
					case ID_INT:
						buf.writeInt((int) id);
						break;
					case ID_VARINT:
						buf.writeUnsignedVarInt((int) id);
						break;
					case ID_VARLONG:
						buf.writeUnsignedVarLong(id);
						break;
					
					case LENGTH_CONTENT:
						buf.writeInt(content.getSize());
						break;
					case LENGTH_CONTENT_ID:
						buf.writeInt(content.getSize() + 8);
						break;
					case LENGTH_CONTENT_ID_INT:
						buf.writeInt(content.getSize() + 4);
						break;
					case LENGTH_CONTENT_ID_VARINT:
					case LENGTH_CONTENT_ID_VARLONG:
						buf.writeInt(content.getSize() + calcVarNumberLength(id));
						break;
					
					case LENGTH_VARINT_CONTENT:
						buf.writeUnsignedVarInt(content.getSize());
						break;
					case LENGTH_VARINT_CONTENT_ID:
						buf.writeUnsignedVarInt(content.getSize() + 8);
						break;
					case LENGTH_VARINT_CONTENT_ID_INT:
						buf.writeUnsignedVarInt(content.getSize() + 4);
						break;
					case LENGTH_VARINT_CONTENT_ID_VARINT:
					case LENGTH_VARINT_CONTENT_ID_VARLONG:
						buf.writeUnsignedVarInt(content.getSize() + calcVarNumberLength(id));
						break;
					
					case CONTENT:
						buf.write(content.toByteArray());
						break;
				}
		}
		
		return buf;
	}
	
	private ByteBuffer encodeUnknownPacket(final ByteBufferGenerator<?> generator, final ByteBuffer buf) {
		final UnknownPacket packet = new UnknownPacket();
		
		packet.fromMap(PacketMap.of("bytes", buf.toByteArray()));
		
		return encodePacket0(generator, packet, UnknownPacket.ID);
	}
	
	public PacketIdPair decodePacket(final ByteBufferGenerator<?> generator, final ByteBuffer buf, final AbstractPacketRegistry registry) {
		final ByteBuffer content = generator.generate();
		
		long id = UnknownPacket.ID;
		boolean subFromLength = false;
		int length = -1;
		
		try {
			for(final int mode : modes)
				switch(mode) {
					case ID:
						id = buf.readLong();
						break;
					case ID_INT:
						id = buf.readInt();
						break;
					case ID_VARINT:
						id = buf.readUnsignedVarInt();
						
						if(subFromLength) {
							length -= calcVarNumberLength(id);
							
							subFromLength = false;
						}
						break;
					case ID_VARLONG:
						id = buf.readUnsignedVarLong();
						
						if(subFromLength) {
							length -= calcVarNumberLength(id);
							
							subFromLength = false;
						}
						break;
					
					case LENGTH_CONTENT:
						length = buf.readInt();
						break;
					case LENGTH_CONTENT_ID:
						length = buf.readInt() - 8;
						break;
					case LENGTH_CONTENT_ID_INT:
						length = buf.readInt() - 4;
						break;
					case LENGTH_CONTENT_ID_VARINT:
					case LENGTH_CONTENT_ID_VARLONG:
						length = buf.readInt();
						
						subFromLength = true;
						break;
					case LENGTH_VARINT_CONTENT:
						length = buf.readUnsignedVarInt();
						break;
					case LENGTH_VARINT_CONTENT_ID:
						length = buf.readUnsignedVarInt() - 8;
						break;
					case LENGTH_VARINT_CONTENT_ID_INT:
						length = buf.readUnsignedVarInt() - 4;
						break;
					case LENGTH_VARINT_CONTENT_ID_VARINT:
					case LENGTH_VARINT_CONTENT_ID_VARLONG:
						length = buf.readUnsignedVarInt();
						
						subFromLength = true;
						break;
					
					case CONTENT:
						content.write(buf.read(length));
						break;
				}
		}catch(final IllegalStateException e) {
			id = UnknownPacket.ID;
		}
		
		final PacketBase<?> packet;
		if(registry.hasIncomingPacket(id)) {
			packet = registry.getIncomingPacket(id).get();
			
			content.read(packet);
		}else {
			packet = new UnknownPacket();
			
			buf.read(packet);
		}
		
		return new PacketIdPair(packet, id);
	}
	
	private static int calcVarNumberLength(long vl) {
		int length = 0;
		
		do {
			vl >>>= 7;
			
			length++;
		}while(vl != 0);
		
		return length;
	}
	
	public static PacketConstructionMode createMode(final int... modes) {
		return new PacketConstructionMode(modes);
	}
	
	@Value
	@Accessors(fluent = true)
	public static class PacketIdPair {
		PacketBase<?> packet;
		long id;
	}
	
	public static class EncodingStrategy {
		private EncodingConsumer consumer = EncodingConsumer.DEFAULT;
		
		private void add(final EncodingConsumer after) {
			consumer = consumer.andThen(after);
		}
		
		public void id() {
			id(IdEncoding.DEFAULT);
		}
		
		public void id(final IdEncoding consumer) {
			add(consumer);
		}
		
		public void length() {
			length(LengthEncoding.DEFAULT);
		}
		
		public void length(final LengthEncoding consumer) {
			add(consumer);
		}
		
		public void content() {
			content(ContentEncoding.DEFAULT);
		}
		
		public void content(final ContentEncoding consumer) {
			add(consumer);
		}
		
		@FunctionalInterface
		private interface EncodingConsumer {
			
			void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id);
			
			default EncodingConsumer andThen(@NonNull final EncodingConsumer after) {
				return (buf, content, packet, id) -> {
					encode(buf, content, packet, id);
					after.encode(buf, content, packet, id);
				};
			}
			
			EncodingConsumer DEFAULT = (buf, content, packet, id) -> {
			};
		}
		
		@FunctionalInterface
		public interface IdEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final long id);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id) {
				encode(buf, id);
			}
			
			IdEncoding DEFAULT = ByteBuffer::writeLong;
		}
		
		@FunctionalInterface
		public interface LengthEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final int length);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id) {
				encode(buf, content.getSize());
			}
			
			LengthEncoding DEFAULT = ByteBuffer::writeInt;
		}
		
		@FunctionalInterface
		public interface ContentEncoding extends EncodingConsumer {
			
			void encode(final ByteBuffer buf, final ByteBuffer content);
			
			@Override
			default void encode(final ByteBuffer buf, final ByteBuffer content, final PacketBase<?> packet, final long id) {
				encode(buf, content);
			}
			
			ContentEncoding DEFAULT = ByteBuffer::transferFrom;
		}
	}
}
