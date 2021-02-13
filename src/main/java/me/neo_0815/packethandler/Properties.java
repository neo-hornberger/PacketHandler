package me.neo_0815.packethandler;

import me.neo_0815.encryption.Encryption;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.DefaultPacketRegistry;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class Properties {
	private boolean clearingEnabled = true, sendingConnectionPackets = true, encryptionEnabled = false;
	private AbstractPacketRegistry packetRegistry = new DefaultPacketRegistry();
	private PacketConstructionMode packetConstructionMode = PacketConstructionMode.DEFAULT;
	private ByteBufferGenerator<?> byteBufferGenerator = ByteBufferGenerator.DEFAULT_GENERATOR;
	private Encryption encryption;
	
	public boolean isRaw() {
		return !clearingEnabled && !sendingConnectionPackets;
	}
	
	public Properties makeRaw() {
		clearingEnabled = false;
		sendingConnectionPackets = false;
		
		return this;
	}
	
	public Properties copy() {
		final Properties copy = new Properties();
		
		copy.clearingEnabled = clearingEnabled;
		copy.sendingConnectionPackets = sendingConnectionPackets;
		copy.encryptionEnabled = encryptionEnabled;
		
		copy.packetRegistry = packetRegistry.copy();
		copy.packetConstructionMode = packetConstructionMode;
		copy.byteBufferGenerator = byteBufferGenerator;
		copy.encryption = encryption;
		
		return copy;
	}
}
