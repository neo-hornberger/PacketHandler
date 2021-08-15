package me.neo_0815.packethandler.protocol;

import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.executor.ExecutorClientBiFunction;
import me.neo_0815.packethandler.executor.ExecutorClientFunction;
import me.neo_0815.packethandler.executor.ExecutorFunction;
import me.neo_0815.packethandler.executor.ExecutorServerFunction;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;
import me.neo_0815.packethandler.server.Server;
import me.neo_0815.packethandler.utils.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public abstract class Protocol<P extends IProtocol<P>> extends AbstractProtocol<P> {
	private final Map<P, Map<Long, Pair<ExecutorFunction<PacketBase<?>>, P>>> map = new HashMap<>();
	
	private P current;
	
	public Protocol(final Connection connection, final P start) {
		super(connection);
		
		current = start;
		
		init();
	}
	
	public Protocol(final Server server, final P start) {
		super(server);
		
		current = start;
		
		init();
	}
	
	@Override
	protected final AbstractPacketRegistry packetRegistry() {
		return super.packetRegistry();
	}
	
	@Override
	protected final AbstractPacketRegistry packetRegistry(final UUID client) {
		return super.packetRegistry(client);
	}
	
	@Override
	public final boolean execute(final UUID client, final PacketBase<?> packet, final long id) {
		final Map<Long, Pair<ExecutorFunction<PacketBase<?>>, P>> vertices = map.computeIfAbsent(current, _p_ -> new HashMap<>());
		
		if(!vertices.containsKey(id)) {
			onStateChangeError(current, client, packet, id);
			
			return false;
		}
		
		final Pair<ExecutorFunction<PacketBase<?>>, P> pair = vertices.get(id);
		
		pair.getFirst().accept(client, packet, id);
		current = pair.getSecond();
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long id, final ExecutorFunction<T> function) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);
		Objects.requireNonNull(function);
		
		map.computeIfAbsent(from, _p_ -> new HashMap<>()).put(id, new Pair<>((ExecutorFunction<PacketBase<?>>) function, to));
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType packetType, final ExecutorFunction<T> function) {
		register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory packetFactory, final ExecutorFunction<T> function) {
		register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long id, final ExecutorClientBiFunction<T> function) {
		register(from, to, id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType packetType, final ExecutorClientBiFunction<T> function) {
		register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory packetFactory, final ExecutorClientBiFunction<T> function) {
		register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long id, final ExecutorClientFunction<T> function) {
		register(from, to, id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType packetType, final ExecutorClientFunction<T> function) {
		register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory packetFactory, final ExecutorClientFunction<T> function) {
		register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long id, final ExecutorServerFunction<T> function) {
		register(from, to, id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType packetType, final ExecutorServerFunction<T> function) {
		register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory packetFactory, final ExecutorServerFunction<T> function) {
		register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long[] ids, final ExecutorFunction<T> function) {
		for(final long id : ids)
			register(from, to, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType[] packetTypes, final ExecutorFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory[] packetFactories, final ExecutorFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long[] ids, final ExecutorClientBiFunction<T> function) {
		for(final long id : ids)
			register(from, to, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType[] packetTypes, final ExecutorClientBiFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory[] packetFactories, final ExecutorClientBiFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long[] ids, final ExecutorClientFunction<T> function) {
		for(final long id : ids)
			register(from, to, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType[] packetTypes, final ExecutorClientFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory[] packetFactories, final ExecutorClientFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final long[] ids, final ExecutorServerFunction<T> function) {
		for(final long id : ids)
			register(from, to, id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketType[] packetTypes, final ExecutorServerFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(from, to, packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final P from, final P to, final IPacketFactory[] packetFactories, final ExecutorServerFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(from, to, packetRegistry().getIncomingPacketId(packetFactory), function);
	}
}
