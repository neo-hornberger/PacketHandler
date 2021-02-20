package me.neo_0815.packethandler.registry;

import me.neo_0815.packethandler.packet.PacketBase;

import java.util.function.Supplier;

public interface IPacketFactory {
	
	Supplier<? extends PacketBase<?>> packet();
}
