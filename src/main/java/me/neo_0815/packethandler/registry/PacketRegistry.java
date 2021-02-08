package me.neo_0815.packethandler.registry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import me.neo_0815.packethandler.packet.Packet;
import me.neo_0815.packethandler.packet.PacketBase;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * The class PacketRegistry makes it easy to deal with {@link Packet}s.
 *
 * @author Neo Hornberger
 */
@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true)
public final class PacketRegistry extends AbstractPacketRegistry {
	protected final HashMap<Long, Supplier<? extends PacketBase<?>>> incoming = new HashMap<>(), outgoing = new HashMap<>();
	protected final HashMap<IPacketFactory, Long> factoryCacheIncoming = new HashMap<>(), factoryCacheOutgoing = new HashMap<>();
	
	@Override
	protected void registerPacket(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id, final boolean incoming) {
		if(hasPacket(id, incoming)) alreadyRegisteredPacket(id);
		
		packets(incoming).put(id, packet);
	}
	
	@Override
	protected boolean hasPacket(final long id, final boolean incoming) {
		return packets(incoming).containsKey(id);
	}
	
	@Override
	protected Supplier<? extends PacketBase<?>> getPacket(final long id, final boolean incoming) {
		if(!hasPacket(id, incoming)) notRegisteredPacket(id);
		
		return packets(incoming).get(id);
	}
	
	@Override
	public PacketRegistry copy() {
		final PacketRegistry reg = new PacketRegistry();
		
		reg.incoming.putAll(incoming);
		reg.outgoing.putAll(outgoing);
		
		reg.factoryCacheIncoming.putAll(factoryCacheIncoming);
		reg.factoryCacheOutgoing.putAll(factoryCacheOutgoing);
		
		return reg;
	}
	
	@Override
	public PacketRegistry copySwapped() {
		final PacketRegistry reg = new PacketRegistry();
		
		reg.incoming.putAll(outgoing);
		reg.outgoing.putAll(incoming);
		
		reg.factoryCacheIncoming.putAll(factoryCacheOutgoing);
		reg.factoryCacheOutgoing.putAll(factoryCacheIncoming);
		
		return reg;
	}
}
