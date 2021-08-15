package me.neo_0815.packethandler.bridge;

import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.registry.IPacketType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.function.Supplier;

@AllArgsConstructor
@FieldDefaults(makeFinal = true)
public final class BridgedPacket implements IPacketType {
	PacketBase<?> packet;
	@Getter
	@Accessors(fluent = true)
	long id;
	
	@Override
	public Supplier<? extends PacketBase<?>> packet() {
		return () -> packet;
	}
}
