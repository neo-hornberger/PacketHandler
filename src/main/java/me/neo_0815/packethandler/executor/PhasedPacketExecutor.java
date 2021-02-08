package me.neo_0815.packethandler.executor;

import lombok.Value;
import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;
import me.neo_0815.packethandler.registry.IPhase;
import me.neo_0815.packethandler.registry.PhasedPacketRegistry;
import me.neo_0815.packethandler.server.Server;

import java.util.HashMap;
import java.util.UUID;

public abstract class PhasedPacketExecutor<P extends IPhase<P>> extends AbstractPacketExecutor {
	private final HashMap<Pair, ExecutorFunction<PacketBase<?>>> map = new HashMap<>();
	
	public PhasedPacketExecutor(final Connection connection) {
		super(connection);
		
		init();
	}
	
	public PhasedPacketExecutor(final Server server) {
		super(server);
		
		init();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected final PhasedPacketRegistry<P> packetRegistry() {
		return (PhasedPacketRegistry<P>) super.packetRegistry();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected final PhasedPacketRegistry<P> packetRegistry(final UUID client) {
		return (PhasedPacketRegistry<P>) super.packetRegistry(client);
	}
	
	@Override
	public final boolean execute(final UUID client, final PacketBase<?> packet, final long id) {
		return execute(client, packet, packetRegistry(client).currentPhase(), id);
	}
	
	public final boolean execute(final UUID client, final PacketBase<?> packet, final P phase, final long id) {
		final Pair pair = new Pair(phase, id);
		
		if(!map.containsKey(pair)) return false;
		
		map.get(new Pair(phase, id)).accept(client, packet, id);
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P phase, final long id, final ExecutorFunction<T> function) {
		map.put(new Pair(phase, id), (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType packetType, final ExecutorFunction<T> function) {
		register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory packetFactory, final ExecutorFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		packetRegistry().setPhase(oldPhase);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P phase, final long id, final ExecutorClientBiFunction<T> function) {
		register(phase, id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType packetType, final ExecutorClientBiFunction<T> function) {
		register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory packetFactory, final ExecutorClientBiFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		packetRegistry().setPhase(oldPhase);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P phase, final long id, final ExecutorClientFunction<T> function) {
		register(phase, id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType packetType, final ExecutorClientFunction<T> function) {
		register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory packetFactory, final ExecutorClientFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		packetRegistry().setPhase(oldPhase);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P phase, final long id, final ExecutorServerFunction<T> function) {
		register(phase, id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType packetType, final ExecutorServerFunction<T> function) {
		register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory packetFactory, final ExecutorServerFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		packetRegistry().setPhase(oldPhase);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final long[] ids, final ExecutorFunction<T> function) {
		for(final long id : ids)
			register(phase, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType[] packetTypes, final ExecutorFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory[] packetFactories, final ExecutorFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		
		for(final IPacketFactory packetFactory : packetFactories)
			register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		
		packetRegistry().setPhase(oldPhase);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final long[] ids, final ExecutorClientBiFunction<T> function) {
		for(final long id : ids)
			register(phase, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType[] packetTypes, final ExecutorClientBiFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory[] packetFactories, final ExecutorClientBiFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		
		for(final IPacketFactory packetFactory : packetFactories)
			register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		
		packetRegistry().setPhase(oldPhase);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final long[] ids, final ExecutorClientFunction<T> function) {
		for(final long id : ids)
			register(phase, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType[] packetTypes, final ExecutorClientFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory[] packetFactories, final ExecutorClientFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		
		for(final IPacketFactory packetFactory : packetFactories)
			register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		
		packetRegistry().setPhase(oldPhase);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final long[] ids, final ExecutorServerFunction<T> function) {
		for(final long id : ids)
			register(phase, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketType[] packetTypes, final ExecutorServerFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(phase, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P phase, final IPacketFactory[] packetFactories, final ExecutorServerFunction<T> function) {
		final P oldPhase = packetRegistry().currentPhase();
		
		packetRegistry().setPhase(phase);
		
		for(final IPacketFactory packetFactory : packetFactories)
			register(phase, packetRegistry().getIncomingPacketId(packetFactory), function);
		
		packetRegistry().setPhase(oldPhase);
	}
	
	@Value
	private static class Pair {
		IPhase<?> phase;
		long id;
	}
}
