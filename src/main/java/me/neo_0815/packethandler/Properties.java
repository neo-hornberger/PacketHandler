package me.neo_0815.packethandler;

import me.neo_0815.encryption.Encryption;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.DefaultPacketRegistry;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.concurrent.TimeUnit;

@Data
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Properties {
	boolean clearingEnabled = true, sendingConnectionPackets = true, encryptionEnabled = false;
	@NonNull AbstractPacketRegistry packetRegistry = new DefaultPacketRegistry();
	@NonNull PacketConstructionMode packetConstructionMode = PacketConstructionMode.DEFAULT;
	@NonNull ByteBufferGenerator<?> byteBufferGenerator = ByteBufferGenerator.DEFAULT_GENERATOR;
	@NonNull Encryption encryption = null;
	long clearingInterval = 10_000L;
	@NonNull TimeUnit clearingIntervalUnit = TimeUnit.MILLISECONDS;
	int clearingPromptCount = 5;
	
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
		
		copy.clearingInterval = clearingInterval;
		copy.clearingIntervalUnit = clearingIntervalUnit;
		copy.clearingPromptCount = clearingPromptCount;
		
		return copy;
	}
}
