package me.neo_0815.packethandler;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketClassFactory;

@AllArgsConstructor
public enum TestPacketType implements IPacketClassFactory {
	PACKET(TestPacket.class),
	JSON(TestJsonPacket.class),
	EMPTY(TestEmptyPacket.class);
	
	@Getter
	@Accessors(fluent = true)
	private final Class<? extends PacketBase<?>> packetClass;
}
