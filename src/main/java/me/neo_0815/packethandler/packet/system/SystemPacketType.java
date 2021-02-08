package me.neo_0815.packethandler.packet.system;

import lombok.Getter;
import lombok.experimental.Accessors;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketClassType;

@Getter
@Accessors(fluent = true)
public enum SystemPacketType implements IPacketClassType {
	CONNECT(-1, PacketConnect.class),
	DISCONNECT(-2, PacketDisconnect.class),
	WAKE(-3, PacketWake.class),
	CHANGE_UUID(-4, PacketChangeUUID.class),
	
	MESSAGE(-100, PacketPrimitiveMessage.class);
	
	
	private final long id;
	private final Class<? extends PacketBase<?>> packetClass;
	
	SystemPacketType(final long id, final Class<? extends PacketBase<?>> clazz) {
		this.id = id;
		this.packetClass = clazz;
	}
}
