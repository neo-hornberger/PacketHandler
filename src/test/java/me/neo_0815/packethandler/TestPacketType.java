package me.neo_0815.packethandler;

import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketClassFactory;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@AllArgsConstructor
public enum TestPacketType implements IPacketClassFactory {
	PACKET(TestPacket.class),
	JSON(TestJsonPacket.class),
	EMPTY(TestEmptyPacket.class),
	ARRAY(TestArrayPacket.class);
	
	@Getter
	@Accessors(fluent = true)
	private final Class<? extends PacketBase<?>> packetClass;
}
