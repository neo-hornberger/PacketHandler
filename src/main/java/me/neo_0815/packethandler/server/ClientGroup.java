package me.neo_0815.packethandler.server;

import me.neo_0815.packethandler.PacketMap;
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

@EqualsAndHashCode
@RequiredArgsConstructor
public class ClientGroup implements Iterable<ClientConnection> {
	@Getter
	private final UUID uuid;
	private final Set<ClientConnection> clients = new HashSet<>();
	
	public boolean add(final ClientConnection client) {
		return clients.add(client);
	}
	
	public boolean remove(final ClientConnection client) {
		return clients.remove(client);
	}
	
	public boolean has(final ClientConnection client) {
		return clients.contains(client);
	}
	
	public void sendPacket(final PacketBase<?> packet, final long id) {
		forEach(client -> client.sendPacket(packet, id));
	}
	
	public void sendPacket(final PacketBase<?> packet, final IPacketFactory packetFactory) {
		forEach(client -> client.sendPacket(packet, packetFactory));
	}
	
	public void sendPacket(final PacketBase<?> packet, final IPacketType packetType) {
		forEach(client -> client.sendPacket(packet, packetType));
	}
	
	public void sendPacket(final long... ids) {
		forEach(client -> client.sendPacket(ids));
	}
	
	public void sendPacket(final IPacketFactory... packetFactories) {
		forEach(client -> client.sendPacket(packetFactories));
	}
	
	public void sendPacket(final IPacketType... packetTypes) {
		forEach(client -> client.sendPacket(packetTypes));
	}
	
	public void sendPacket(final long id, final PacketMap map) {
		forEach(client -> client.sendPacket(id, map));
	}
	
	public void sendPacket(final IPacketFactory packetFactories, final PacketMap map) {
		forEach(client -> client.sendPacket(packetFactories, map));
	}
	
	public void sendPacket(final IPacketType packetType, final PacketMap map) {
		forEach(client -> client.sendPacket(packetType, map));
	}
	
	public void sendPackets(final long[] ids, final PacketMap[] maps) {
		forEach(client -> client.sendPackets(ids, maps));
	}
	
	public void sendPackets(final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		forEach(client -> client.sendPackets(packetFactories, maps));
	}
	
	public void sendPackets(final IPacketType[] packetTypes, final PacketMap[] maps) {
		forEach(client -> client.sendPackets(packetTypes, maps));
	}
	
	public void sendMessage(final String message) {
		forEach(client -> client.sendMessage(message));
	}
	
	public void sendMessages(final String[] messages) {
		forEach(client -> client.sendMessages(messages));
	}
	
	@Override
	public Iterator<ClientConnection> iterator() {
		return clients.iterator();
	}
}
