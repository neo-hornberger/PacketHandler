package me.neo_0815.packethandler.registry;

import me.neo_0815.packethandler.packet.PacketBase;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class PhasedPacketRegistry<P extends IPhase<P>> extends AbstractPacketRegistry {
	protected final HashMap<P, HashMap<Long, Supplier<? extends PacketBase<?>>>> incoming = new HashMap<>(), outgoing = new HashMap<>();
	protected final HashMap<P, HashMap<IPacketFactory, Long>> factoryCacheIncoming = new HashMap<>(), factoryCacheOutgoing = new HashMap<>();
	
	protected final P startPhase;
	
	@Getter
	@Accessors(fluent = true)
	protected P currentPhase;
	
	public PhasedPacketRegistry(@NonNull final P startPhase) {
		this.startPhase = startPhase;
		
		setPhase(startPhase);
	}
	
	public void setPhase(@NonNull final P phase) {
		if(currentPhase == phase) return;
		
		if(!incoming.containsKey(phase)) incoming.put(phase, new HashMap<>());
		if(!outgoing.containsKey(phase)) outgoing.put(phase, new HashMap<>());
		if(!factoryCacheIncoming.containsKey(phase)) factoryCacheIncoming.put(phase, new HashMap<>());
		if(!factoryCacheOutgoing.containsKey(phase)) factoryCacheOutgoing.put(phase, new HashMap<>());
		
		currentPhase = phase;
	}
	
	public void setPhase(final long id) {
		setPhase(currentPhase.phase(id));
	}
	
	public void nextPhase() {
		setPhase(currentPhase.next());
	}
	
	public void previousPhase() {
		setPhase(currentPhase.previous());
	}
	
	public void startPhase() {
		setPhase(startPhase);
	}
	
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
	protected Map<Long, Supplier<? extends PacketBase<?>>> incoming() {
		return incoming.get(currentPhase);
	}
	
	@Override
	protected Map<Long, Supplier<? extends PacketBase<?>>> outgoing() {
		return outgoing.get(currentPhase);
	}
	
	@Override
	protected Map<IPacketFactory, Long> factoryCacheIncoming() {
		return factoryCacheIncoming.get(currentPhase);
	}
	
	@Override
	protected Map<IPacketFactory, Long> factoryCacheOutgoing() {
		return factoryCacheOutgoing.get(currentPhase);
	}
	
	@Override
	public PhasedPacketRegistry<P> copy() {
		final PhasedPacketRegistry<P> reg = new PhasedPacketRegistry<>(startPhase);
		
		incoming.forEach((key, value) -> reg.incoming.put(key, copyMap(value)));
		outgoing.forEach((key, value) -> reg.outgoing.put(key, copyMap(value)));
		
		factoryCacheIncoming.forEach((key, value) -> reg.factoryCacheIncoming.put(key, copyMap(value)));
		factoryCacheOutgoing.forEach((key, value) -> reg.factoryCacheOutgoing.put(key, copyMap(value)));
		
		reg.setPhase(currentPhase);
		
		return reg;
	}
	
	@Override
	public PhasedPacketRegistry<P> copySwapped() {
		final PhasedPacketRegistry<P> reg = new PhasedPacketRegistry<>(startPhase);
		
		incoming.forEach((key, value) -> reg.outgoing.put(key, copyMap(value)));
		outgoing.forEach((key, value) -> reg.incoming.put(key, copyMap(value)));
		
		factoryCacheIncoming.forEach((key, value) -> reg.factoryCacheOutgoing.put(key, copyMap(value)));
		factoryCacheOutgoing.forEach((key, value) -> reg.factoryCacheIncoming.put(key, copyMap(value)));
		
		reg.setPhase(currentPhase);
		
		return reg;
	}
	
	private <K, V> HashMap<K, V> copyMap(final HashMap<K, V> map) {
		return new HashMap<>(map);
	}
}
