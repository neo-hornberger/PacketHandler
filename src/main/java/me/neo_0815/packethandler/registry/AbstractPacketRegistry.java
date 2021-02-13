package me.neo_0815.packethandler.registry;

import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.system.SystemPacketType;

import lombok.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractPacketRegistry {
	public static final boolean INCOMING = true, OUTGOING = false;
	
	protected abstract void registerPacket(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id, final boolean incoming);
	
	protected final void registerPacket(@NonNull final IPacketFactory packetFactory, final long id, final boolean incoming) {
		cacheFactory(packetFactory, id, incoming);
		
		registerPacket(packetFactory.packet(), id, incoming);
	}
	
	protected final void registerPacket(@NonNull final IPacketType packetType, final boolean incoming) {
		registerPacket(packetType.packet(), packetType.id(), incoming);
	}
	
	protected final void registerIncomingPacket0(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id) {
		registerPacket(packet, id, INCOMING);
	}
	
	protected final void registerOutgoingPacket0(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id) {
		registerPacket(packet, id, OUTGOING);
	}
	
	protected final void registerIncomingPacket0(@NonNull final IPacketFactory packetFactory, final long id) {
		registerPacket(packetFactory, id, INCOMING);
	}
	
	protected final void registerOutgoingPacket0(@NonNull final IPacketFactory packetFactory, final long id) {
		registerPacket(packetFactory, id, OUTGOING);
	}
	
	protected final void registerIncomingPacket0(@NonNull final IPacketType packetType) {
		registerPacket(packetType, INCOMING);
	}
	
	protected final void registerOutgoingPacket0(@NonNull final IPacketType packetType) {
		registerPacket(packetType, OUTGOING);
	}
	
	protected final void registerIncomingPackets0(@NonNull final IPacketFactory[] packetFactories, final long[] ids) {
		for(int i = 0; i < packetFactories.length; i++)
			registerPacket(packetFactories[i], ids[i], INCOMING);
	}
	
	protected final void registerOutgoingPackets0(@NonNull final IPacketFactory[] packetFactories, final long[] ids) {
		for(int i = 0; i < packetFactories.length; i++)
			registerPacket(packetFactories[i], ids[i], OUTGOING);
	}
	
	protected final void registerIncomingPackets0(@NonNull final IPacketType[] packetTypes) {
		for(final IPacketType packetType : packetTypes)
			registerPacket(packetType, INCOMING);
	}
	
	protected final void registerOutgoingPackets0(@NonNull final IPacketType[] packetTypes) {
		for(final IPacketType packetType : packetTypes)
			registerPacket(packetType, OUTGOING);
	}
	
	public final void registerIncomingPacket(@NonNull final Supplier<? extends PacketBase<?>> packet) {
		registerIncomingPacket(packet, calcNextFreeId(INCOMING));
	}
	
	public final void registerOutgoingPacket(@NonNull final Supplier<? extends PacketBase<?>> packet) {
		registerOutgoingPacket(packet, calcNextFreeId(OUTGOING));
	}
	
	public final void registerIncomingPacket(@NonNull final IPacketFactory packetFactory) {
		registerIncomingPacket(packetFactory, calcNextFreeId(INCOMING));
	}
	
	public final void registerOutgoingPacket(@NonNull final IPacketFactory packetFactory) {
		registerOutgoingPacket(packetFactory, calcNextFreeId(OUTGOING));
	}
	
	public final void registerIncomingPacket(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id) {
		check(id);
		
		registerIncomingPacket0(packet, id);
	}
	
	public final void registerOutgoingPacket(@NonNull final Supplier<? extends PacketBase<?>> packet, final long id) {
		check(id);
		
		registerOutgoingPacket0(packet, id);
	}
	
	public final void registerIncomingPacket(@NonNull final IPacketFactory packetFactory, final long id) {
		check(id);
		
		registerIncomingPacket0(packetFactory, id);
	}
	
	public final void registerOutgoingPacket(@NonNull final IPacketFactory packetFactory, final long id) {
		check(id);
		
		registerOutgoingPacket0(packetFactory, id);
	}
	
	public final void registerIncomingPacket(@NonNull final IPacketType packetType) {
		check(packetType.packet(), packetType.id());
		
		registerIncomingPacket0(packetType);
	}
	
	public final void registerOutgoingPacket(@NonNull final IPacketType packetType) {
		check(packetType.packet(), packetType.id());
		
		registerOutgoingPacket0(packetType);
	}
	
	public final void registerIncomingPackets(@NonNull final IPacketFactory[] packetFactories) {
		for(final IPacketFactory packetFactory : packetFactories)
			registerIncomingPacket(packetFactory, calcNextFreeId(INCOMING));
	}
	
	public final void registerOutgoingPackets(@NonNull final IPacketFactory[] packetFactories) {
		for(final IPacketFactory packetFactory : packetFactories)
			registerOutgoingPacket(packetFactory, calcNextFreeId(OUTGOING));
	}
	
	public final void registerIncomingPackets(@NonNull final IPacketFactory[] packetFactories, final long[] ids) {
		for(int i = 0; i < packetFactories.length; i++)
			registerIncomingPacket(packetFactories[i], ids[i]);
	}
	
	public final void registerOutgoingPackets(@NonNull final IPacketFactory[] packetFactories, final long[] ids) {
		for(int i = 0; i < packetFactories.length; i++)
			registerOutgoingPacket(packetFactories[i], ids[i]);
	}
	
	public final void registerIncomingPackets(@NonNull final IPacketType[] packetTypes) {
		for(final IPacketType packetType : packetTypes)
			registerIncomingPacket(packetType);
	}
	
	public final void registerOutgoingPackets(@NonNull final IPacketType[] packetTypes) {
		for(final IPacketType packetType : packetTypes)
			registerOutgoingPacket(packetType);
	}
	
	protected abstract boolean hasPacket(final long id, final boolean incoming);
	
	protected final boolean hasPacket(@NonNull final IPacketFactory packetFactory, final boolean incoming) {
		return hasPacket(cachedFactoryId(packetFactory, incoming), incoming);
	}
	
	protected final boolean hasPacket(@NonNull final IPacketType packetType, final boolean incoming) {
		return hasPacket(packetType.id(), incoming);
	}
	
	public final boolean hasIncomingPacket(final long id) {
		return hasPacket(id, INCOMING);
	}
	
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public final boolean hasOutgoingPacket(final long id) {
		return hasPacket(id, OUTGOING);
	}
	
	public final boolean hasIncomingPacket(@NonNull final IPacketFactory packetFactory) {
		return hasPacket(packetFactory, INCOMING);
	}
	
	public final boolean hasOutgoingPacket(@NonNull final IPacketFactory packetFactory) {
		return hasPacket(packetFactory, OUTGOING);
	}
	
	public final boolean hasIncomingPacket(@NonNull final IPacketType packetType) {
		return hasPacket(packetType, INCOMING);
	}
	
	public final boolean hasOutgoingPacket(@NonNull final IPacketType packetType) {
		return hasPacket(packetType, OUTGOING);
	}
	
	protected abstract Supplier<? extends PacketBase<?>> getPacket(final long id, final boolean incoming);
	
	protected final Supplier<? extends PacketBase<?>> getPacket(@NonNull final IPacketFactory packetFactory, final boolean incoming) {
		return getPacket(cachedFactoryId(packetFactory, incoming), incoming);
	}
	
	protected final Supplier<? extends PacketBase<?>> getPacket(@NonNull final IPacketType packetType, final boolean incoming) {
		return getPacket(packetType.id(), incoming);
	}
	
	public final Supplier<? extends PacketBase<?>> getIncomingPacket(final long id) {
		return getPacket(id, INCOMING);
	}
	
	public final Supplier<? extends PacketBase<?>> getOutgoingPacket(final long id) {
		return getPacket(id, OUTGOING);
	}
	
	public final Supplier<? extends PacketBase<?>> getIncomingPacket(@NonNull final IPacketFactory packetFactory) {
		return getPacket(packetFactory, INCOMING);
	}
	
	public final Supplier<? extends PacketBase<?>> getOutgoingPacket(@NonNull final IPacketFactory packetFactory) {
		return getPacket(packetFactory, OUTGOING);
	}
	
	public final Supplier<? extends PacketBase<?>> getIncomingPacket(@NonNull final IPacketType packetType) {
		return getPacket(packetType, INCOMING);
	}
	
	public final Supplier<? extends PacketBase<?>> getOutgoingPacket(@NonNull final IPacketType packetType) {
		return getPacket(packetType, OUTGOING);
	}
	
	@SuppressWarnings("unchecked")
	public final Supplier<? extends PacketBase<?>>[] getIncomingPackets(final long[] ids) {
		final Supplier<? extends PacketBase<?>>[] packets = new Supplier[ids.length];
		
		for(int i = 0; i < ids.length; i++)
			packets[i] = getIncomingPacket(ids[i]);
		
		return packets;
	}
	
	@SuppressWarnings("unchecked")
	public final Supplier<? extends PacketBase<?>>[] getOutgoingPackets(final long[] ids) {
		System.out.println(Arrays.stream(ids).<Supplier<? extends PacketBase<?>>>mapToObj(this::getOutgoingPacket).toArray(i -> new Supplier[ids.length]).length);
		
		final Supplier<? extends PacketBase<?>>[] packets = new Supplier[ids.length];
		
		for(int i = 0; i < ids.length; i++)
			packets[i] = getOutgoingPacket(ids[i]);
		
		return packets;
	}
	
	@SuppressWarnings("unchecked")
	public final Supplier<? extends PacketBase<?>>[] getIncomingPackets(@NonNull final IPacketFactory[] packetFactories) {
		final Supplier<? extends PacketBase<?>>[] packets = new Supplier[packetFactories.length];
		
		for(int i = 0; i < packetFactories.length; i++)
			packets[i] = getIncomingPacket(packetFactories[i]);
		
		return packets;
	}
	
	@SuppressWarnings("unchecked")
	public final Supplier<? extends PacketBase<?>>[] getOutgoingPackets(@NonNull final IPacketFactory[] packetFactories) {
		System.out.println(Arrays.stream(packetFactories).<Supplier<? extends PacketBase<?>>>map(this::getOutgoingPacket).toArray(i -> new Supplier[packetFactories.length]).length);
		
		final Supplier<? extends PacketBase<?>>[] packets = new Supplier[packetFactories.length];
		
		for(int i = 0; i < packetFactories.length; i++)
			packets[i] = getOutgoingPacket(packetFactories[i]);
		
		return packets;
	}
	
	@SuppressWarnings("unchecked")
	public final Supplier<? extends PacketBase<?>>[] getIncomingPackets(@NonNull final IPacketType[] packetTypes) {
		final Supplier<? extends PacketBase<?>>[] packets = new Supplier[packetTypes.length];
		
		for(int i = 0; i < packetTypes.length; i++)
			packets[i] = getIncomingPacket(packetTypes[i]);
		
		return packets;
	}
	
	@SuppressWarnings("unchecked")
	public final Supplier<? extends PacketBase<?>>[] getOutgoingPackets(@NonNull final IPacketType[] packetTypes) {
		final Supplier<? extends PacketBase<?>>[] packets = new Supplier[packetTypes.length];
		
		for(int i = 0; i < packetTypes.length; i++)
			packets[i] = getOutgoingPacket(packetTypes[i]);
		
		return packets;
	}
	
	public final void registerIncomingSystemPackets() {
		registerIncomingPackets0(SystemPacketType.values());
	}
	
	public final void registerOutgoingSystemPackets() {
		registerOutgoingPackets0(SystemPacketType.values());
	}
	
	protected abstract Map<Long, Supplier<? extends PacketBase<?>>> incoming();
	
	protected abstract Map<Long, Supplier<? extends PacketBase<?>>> outgoing();
	
	protected final Map<Long, Supplier<? extends PacketBase<?>>> packets(final boolean incoming) {
		return incoming ? incoming() : outgoing();
	}
	
	public final Collection<Supplier<? extends PacketBase<?>>> getIncomingPackets() {
		return incoming().values();
	}
	
	public final Collection<Supplier<? extends PacketBase<?>>> getOutgoingPackets() {
		return outgoing().values();
	}
	
	protected abstract Map<IPacketFactory, Long> factoryCacheIncoming();
	
	protected abstract Map<IPacketFactory, Long> factoryCacheOutgoing();
	
	protected final Map<IPacketFactory, Long> factoryCache(final boolean incoming) {
		return incoming ? factoryCacheIncoming() : factoryCacheOutgoing();
	}
	
	protected final void cacheFactory(@NonNull final IPacketFactory packetFactory, final long id, final boolean incoming) {
		factoryCache(incoming).put(packetFactory, id);
	}
	
	protected final long cachedFactoryId(@NonNull final IPacketFactory packetFactory, final boolean incoming) {
		return factoryCache(incoming).get(packetFactory);
	}
	
	public final long getIncomingPacketId(@NonNull final IPacketFactory packetFactory) {
		return cachedFactoryId(packetFactory, INCOMING);
	}
	
	public final long getOutgoingPacketId(@NonNull final IPacketFactory packetFactory) {
		return cachedFactoryId(packetFactory, OUTGOING);
	}
	
	public abstract AbstractPacketRegistry copy();
	
	public abstract AbstractPacketRegistry copySwapped();
	
	protected final void check(final long id) {
		if(id < 0) throw new IllegalArgumentException("id('" + id + "') is not greater than or equal to 0");
	}
	
	protected final void check(final Object nonNull, final long id) {
		Objects.requireNonNull(nonNull);
		
		check(id);
	}
	
	protected final long calcNextFreeId(final boolean incoming) {
		return packets(incoming).keySet().stream().reduce((long) -1, (mem, id) -> mem + 1 == id ? id : mem) + 1;
	}
	
	protected final void alreadyRegisteredPacket(final long id) {
		throw new IllegalArgumentException("Packet with id('" + id + "') is already registered");
	}
	
	protected final void notRegisteredPacket(final long id) {
		throw new IllegalArgumentException("There is no packet with id('" + id + "') registered");
	}
}
