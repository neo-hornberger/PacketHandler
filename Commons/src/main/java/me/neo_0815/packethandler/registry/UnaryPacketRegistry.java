package me.neo_0815.packethandler.registry;

import me.neo_0815.packethandler.packet.PacketBase;

import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public final class UnaryPacketRegistry<T extends AbstractPacketRegistry> extends AbstractPacketRegistry {
	private final T packetRegistry;
	
	public UnaryPacketRegistry(@NonNull final T packetRegistry) {
		this.packetRegistry = packetRegistry;
	}
	
	@Override
	protected void registerPacket(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id, final boolean incoming) {
		packetRegistry.registerPacket(packet, id, INCOMING);
	}
	
	public void registerPacket(@NonNull final Supplier<? extends PacketBase<?>> packet) {
		registerIncomingPacket(packet);
	}
	
	public void registerPacket(@NonNull final IPacketFactory packetFactory) {
		registerIncomingPacket(packetFactory);
	}
	
	public void registerPacket(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id) {
		registerIncomingPacket(packet, id);
	}
	
	public void registerPacket(@NonNull final IPacketFactory packetFactory, final long id) {
		registerIncomingPacket(packetFactory, id);
	}
	
	public void registerPacket(@NonNull final IPacketType packetType) {
		registerIncomingPacket(packetType);
	}
	
	public void registerPackets(@NonNull final IPacketFactory[] packetFactories) {
		registerIncomingPackets(packetFactories);
	}
	
	public void registerPackets(@NonNull final IPacketFactory[] packetFactories, final long[] ids) {
		registerIncomingPackets(packetFactories, ids);
	}
	
	public void registerPackets(@NonNull final IPacketType[] packetTypes) {
		registerIncomingPackets(packetTypes);
	}
	
	@Override
	protected boolean hasPacket(final long id, final boolean incoming) {
		return packetRegistry.hasPacket(id, INCOMING);
	}
	
	public boolean hasPacket(final long id) {
		return hasIncomingPacket(id);
	}
	
	public boolean hasPacket(@NonNull final IPacketFactory packetFactory) {
		return hasIncomingPacket(packetFactory);
	}
	
	public boolean hasPacket(@NonNull final IPacketType packetType) {
		return hasIncomingPacket(packetType);
	}
	
	@Override
	protected Supplier<? extends PacketBase<?>> getPacket(final long id, final boolean incoming) {
		return packetRegistry.getPacket(id, INCOMING);
	}
	
	public Supplier<? extends PacketBase<?>> getPacket(final long id) {
		return getIncomingPacket(id);
	}
	
	public Supplier<? extends PacketBase<?>> getPacket(@NonNull final IPacketFactory packetFactory) {
		return getIncomingPacket(packetFactory);
	}
	
	public Supplier<? extends PacketBase<?>> getPacket(@NonNull final IPacketType packetType) {
		return getIncomingPacket(packetType);
	}
	
	public Supplier<? extends PacketBase<?>>[] getPackets(final long[] ids) {
		return getIncomingPackets(ids);
	}
	
	public Supplier<? extends PacketBase<?>>[] getPackets(@NonNull final IPacketFactory[] packetFactories) {
		return getIncomingPackets(packetFactories);
	}
	
	public Supplier<? extends PacketBase<?>>[] getPackets(@NonNull final IPacketType[] packetTypes) {
		return getIncomingPackets(packetTypes);
	}
	
	public void registerSystemPackets() {
		registerIncomingSystemPackets();
	}
	
	@Override
	protected Map<Long, Supplier<? extends PacketBase<?>>> incoming() {
		return packetRegistry.incoming();
	}
	
	@Override
	protected Map<Long, Supplier<? extends PacketBase<?>>> outgoing() {
		return incoming();
	}
	
	public Collection<Supplier<? extends PacketBase<?>>> getPackets() {
		return getIncomingPackets();
	}
	
	@Override
	protected Map<IPacketFactory, Long> factoryCacheIncoming() {
		return packetRegistry.factoryCacheIncoming();
	}
	
	@Override
	protected Map<IPacketFactory, Long> factoryCacheOutgoing() {
		return factoryCacheIncoming();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public UnaryPacketRegistry<T> copy() {
		return new UnaryPacketRegistry<>((T) packetRegistry.copy());
	}
	
	@Override
	public UnaryPacketRegistry<T> copySwapped() {
		return copy();
	}
	
	public T getSuperPacketRegistry() {
		return packetRegistry;
	}
}
