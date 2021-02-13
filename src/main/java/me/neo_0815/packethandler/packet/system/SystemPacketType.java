package me.neo_0815.packethandler.packet.system;

import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketClassType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public enum SystemPacketType implements IPacketClassType {
	CONNECT(-1, PacketConnect.class),
	DISCONNECT(-2, PacketDisconnect.class),
	WAKE(-3, PacketWake.class),
	CHANGE_UUID(-4, PacketChangeUUID.class),
	
	MESSAGE(-100, PacketPrimitiveMessage.class);
	
	
	private final long id;
	private final Class<? extends PacketBase<?>> packetClass;
}
