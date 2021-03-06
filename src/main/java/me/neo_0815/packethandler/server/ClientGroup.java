package me.neo_0815.packethandler.server;

import me.neo_0815.packethandler.ByteBuffer;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.PacketSender;
import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class ClientGroup extends PacketSender implements Iterable<ClientConnection> {
	
	@EqualsAndHashCode.Exclude
	private final Server server;
	
	@Getter
	private final UUID uuid;
	private final Set<ClientConnection> clients = new HashSet<>();
	
	public boolean add(final UUID client) {
		return add(findClient(client));
	}
	
	public boolean add(final ClientConnection client) {
		return clients.add(client);
	}
	
	public boolean remove(final UUID client) {
		return remove(findClient(client));
	}
	
	public boolean remove(final ClientConnection client) {
		return clients.remove(client);
	}
	
	public boolean has(final UUID client) {
		return has(findClient(client));
	}
	
	public boolean has(final ClientConnection client) {
		return clients.contains(client);
	}
	
	public void clear() {
		clients.clear();
	}
	
	public int count() {
		return clients.size();
	}
	
	@Override
	protected final void sendData(final ByteBuffer buf) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void sendPacket(final PacketBase<?> packet, final long id) {
		forEach(client -> client.sendPacket(packet, id));
	}
	
	@Override
	public void sendPacket(final long id, final PacketMap map) {
		forEach(client -> client.sendPacket(id, map));
	}
	
	@Override
	public void sendPacket(final IPacketFactory packetFactories, final PacketMap map) {
		forEach(client -> client.sendPacket(packetFactories, map));
	}
	
	@Override
	public void sendPacket(final IPacketType packetType, final PacketMap map) {
		forEach(client -> client.sendPacket(packetType, map));
	}
	
	@Override
	public void sendPacket(final PacketBase<?> packet, final IPacketFactory packetFactory) {
		forEach(client -> client.sendPacket(packet, packetFactory));
	}
	
	@Override
	public void sendPacket(final PacketBase<?> packet, final IPacketType packetType) {
		forEach(client -> client.sendPacket(packet, packetType));
	}
	
	@Override
	public void sendPacket(final long... ids) {
		forEach(client -> client.sendPacket(ids));
	}
	
	@Override
	public void sendPacket(final IPacketFactory... packetFactories) {
		forEach(client -> client.sendPacket(packetFactories));
	}
	
	@Override
	public void sendPacket(final IPacketType... packetTypes) {
		forEach(client -> client.sendPacket(packetTypes));
	}
	
	@Override
	public void sendPackets(final long[] ids, final PacketMap[] maps) {
		forEach(client -> client.sendPackets(ids, maps));
	}
	
	@Override
	public void sendPackets(final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		forEach(client -> client.sendPackets(packetFactories, maps));
	}
	
	@Override
	public void sendPackets(final IPacketType[] packetTypes, final PacketMap[] maps) {
		forEach(client -> client.sendPackets(packetTypes, maps));
	}
	
	@Override
	public void sendMessage(final String message) {
		forEach(client -> client.sendMessage(message));
	}
	
	@Override
	public void sendMessages(final String[] messages) {
		forEach(client -> client.sendMessages(messages));
	}
	
	@Override
	protected final Properties properties() {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Iterator<ClientConnection> iterator() {
		return clients.iterator();
	}
	
	private ClientConnection findClient(final UUID client) {
		return server.getClient(client);
	}
}
