package me.neo_0815.packethandler.registry;

import me.neo_0815.packethandler.PacketHelper;
import me.neo_0815.packethandler.packet.PacketBase;

import java.util.function.Supplier;

public interface IPacketClassFactory extends IPacketFactory {
	
	Class<? extends PacketBase<?>> packetClass();
	
	@Override
	default Supplier<? extends PacketBase<?>> packet() {
		return () -> PacketHelper.construct(packetClass());
	}
}
