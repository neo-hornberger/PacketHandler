package me.neo_0815.packethandler.registry;

import me.neo_0815.packethandler.packet.Packet;
import me.neo_0815.packethandler.packet.PacketBase;

import lombok.NonNull;

import java.util.Map;
import java.util.function.Supplier;

public final class DefaultPacketRegistry extends AbstractPacketRegistry {
	
	@Override
	protected void registerPacket(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id, final boolean incoming) {
		throwException();
	}
	
	@Override
	protected boolean hasPacket(final long id, final boolean incoming) {
		throwException();
		
		return false;
	}
	
	@Override
	protected Supplier<? extends Packet> getPacket(final long id, final boolean incoming) {
		throwException();
		
		return null;
	}
	
	@Override
	protected Map<Long, Supplier<? extends PacketBase<?>>> incoming() {
		throwException();
		
		return null;
	}
	
	@Override
	protected Map<Long, Supplier<? extends PacketBase<?>>> outgoing() {
		throwException();
		
		return null;
	}
	
	@Override
	protected Map<IPacketFactory, Long> factoryCacheIncoming() {
		throwException();
		
		return null;
	}
	
	@Override
	protected Map<IPacketFactory, Long> factoryCacheOutgoing() {
		throwException();
		
		return null;
	}
	
	@Override
	public DefaultPacketRegistry copy() {
		throwException();
		
		return new DefaultPacketRegistry();
	}
	
	@Override
	public DefaultPacketRegistry copySwapped() {
		throwException();
		
		return new DefaultPacketRegistry();
	}
	
	private void throwException() {
		throw new IllegalStateException("PacketRegistry is not set. Set a PacketRegistry with 'setPacketRegistry(AbstractPacketRegistry)'");
	}
}
