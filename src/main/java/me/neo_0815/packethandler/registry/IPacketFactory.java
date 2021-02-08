package me.neo_0815.packethandler.registry;

import java.util.function.Supplier;

import me.neo_0815.packethandler.packet.PacketBase;

public interface IPacketFactory {
	
	Supplier<? extends PacketBase<?>> packet();
}
