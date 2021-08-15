package me.neo_0815.packethandler;

import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.system.SystemPacketType;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;

import java.util.Arrays;

public abstract class PacketSender {
	
	private ByteBuffer constructPacket(final PacketBase<?> packet, final long id) {
		return constructionMode().encodePacket(byteBufferGenerator(), packet, id, registry());
	}
	
	private ByteBuffer constructPacket(final long id, final PacketMap map) {
		return constructionMode().encodePacket(byteBufferGenerator(), id, map, registry());
	}
	
	private ByteBuffer constructPacket(final IPacketFactory packetFactory, final PacketMap map) {
		return constructPacket(registry().getOutgoingPacketId(packetFactory), map);
	}
	
	private ByteBuffer constructPacket(final IPacketType packetType, final PacketMap map) {
		return constructPacket(packetType.id(), map);
	}
	
	protected abstract void sendData(final ByteBuffer buf);
	
	public void sendPacket(final PacketBase<?> packet, final long id) {
		sendData(constructPacket(packet, id));
	}
	
	public void sendPacket(final long id, final PacketMap map) {
		sendData(constructPacket(id, map));
	}
	
	public void sendPacket(final IPacketFactory packetFactory, final PacketMap map) {
		sendData(constructPacket(packetFactory, map));
	}
	
	public void sendPacket(final IPacketType packetType, final PacketMap map) {
		sendData(constructPacket(packetType, map));
	}
	
	public void sendPacket(final PacketBase<?> packet, final IPacketFactory packetFactory) {
		sendPacket(packet, registry().getOutgoingPacketId(packetFactory));
	}
	
	public void sendPacket(final PacketBase<?> packet, final IPacketType packetType) {
		sendPacket(packet, packetType.id());
	}
	
	public void sendPacket(final long... ids) {
		sendPackets(ids, PacketHelper.createMaps(ids.length));
	}
	
	public void sendPacket(final IPacketFactory... packetFactories) {
		sendPackets(packetFactories, PacketHelper.createMaps(packetFactories.length));
	}
	
	public void sendPacket(final IPacketType... packetTypes) {
		sendPackets(packetTypes, PacketHelper.createMaps(packetTypes.length));
	}
	
	public void sendPackets(final long[] ids, final PacketMap[] maps) {
		final ByteBuffer buf = byteBufferGenerator().generate();
		
		for(int i = 0; i < ids.length; i++)
			constructPacket(ids[i], maps[i]).transferTo(buf);
		
		sendData(buf);
	}
	
	public void sendPackets(final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		final ByteBuffer buf = byteBufferGenerator().generate();
		
		for(int i = 0; i < packetFactories.length; i++)
			constructPacket(packetFactories[i], maps[i]).transferTo(buf);
		
		sendData(buf);
	}
	
	public void sendPackets(final IPacketType[] packetTypes, final PacketMap[] maps) {
		final ByteBuffer buf = byteBufferGenerator().generate();
		
		for(int i = 0; i < packetTypes.length; i++)
			constructPacket(packetTypes[i], maps[i]).transferTo(buf);
		
		sendData(buf);
	}
	
	public void sendMessage(final String message) {
		sendPacket(SystemPacketType.MESSAGE, PacketMap.of("message", message));
	}
	
	public void sendMessages(final String[] messages) {
		final IPacketType[] packetTypes = new IPacketType[messages.length];
		final PacketMap[] maps = new PacketMap[messages.length];
		
		Arrays.fill(packetTypes, SystemPacketType.MESSAGE);
		
		for(int i = 0; i < messages.length; i++)
			maps[i] = PacketMap.of("message", messages[i]);
		
		sendPackets(packetTypes, maps);
	}
	
	protected abstract Properties properties();
	
	private AbstractPacketRegistry registry() {
		return properties().getPacketRegistry();
	}
	
	private PacketConstructionMode constructionMode() {
		return properties().getPacketConstructionMode();
	}
	
	private ByteBufferGenerator<?> byteBufferGenerator() {
		return properties().getByteBufferGenerator();
	}
}
