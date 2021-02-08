package me.neo_0815.packethandler.executor;

import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;
import me.neo_0815.packethandler.server.Server;

import java.util.HashMap;
import java.util.UUID;

public abstract class PacketExecutor extends AbstractPacketExecutor {
	private final HashMap<Long, ExecutorFunction<PacketBase<?>>> map = new HashMap<>();
	
	public PacketExecutor(final Connection connection) {
		super(connection);
		
		init();
	}
	
	public PacketExecutor(final Server server) {
		super(server);
		
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
		if(!map.containsKey(id)) return false;
		
		map.get(id).accept(client, packet, id);
		
		return true;
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final long id, final ExecutorFunction<T> function) {
		map.put(id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType packetType, final ExecutorFunction<T> function) {
		register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory packetFactory, final ExecutorFunction<T> function) {
		register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final long id, final ExecutorClientBiFunction<T> function) {
		register(id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType packetType, final ExecutorClientBiFunction<T> function) {
		register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory packetFactory, final ExecutorClientBiFunction<T> function) {
		register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final long id, final ExecutorClientFunction<T> function) {
		register(id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType packetType, final ExecutorClientFunction<T> function) {
		register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory packetFactory, final ExecutorClientFunction<T> function) {
		register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	@SuppressWarnings("unchecked")
	protected final <T extends PacketBase<?>> void register(final long id, final ExecutorServerFunction<T> function) {
		register(id, (ExecutorFunction<PacketBase<?>>) function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType packetType, final ExecutorServerFunction<T> function) {
		register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory packetFactory, final ExecutorServerFunction<T> function) {
		register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final long[] ids, final ExecutorFunction<T> function) {
		for(final long id : ids)
			register(id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType[] packetTypes, final ExecutorFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory[] packetFactories, final ExecutorFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final long[] ids, final ExecutorClientBiFunction<T> function) {
		for(final long id : ids)
			register(id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType[] packetTypes, final ExecutorClientBiFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory[] packetFactories, final ExecutorClientBiFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final long[] ids, final ExecutorClientFunction<T> function) {
		for(final long id : ids)
			register(id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType[] packetTypes, final ExecutorClientFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory[] packetFactories, final ExecutorClientFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final long[] ids, final ExecutorServerFunction<T> function) {
		for(final long id : ids)
			register(id, function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketType[] packetTypes, final ExecutorServerFunction<T> function) {
		for(final IPacketType packetType : packetTypes)
			register(packetType.id(), function);
	}
	
	protected final <T extends PacketBase<?>> void register(final IPacketFactory[] packetFactories, final ExecutorServerFunction<T> function) {
		for(final IPacketFactory packetFactory : packetFactories)
			register(packetRegistry().getIncomingPacketId(packetFactory), function);
	}
}
