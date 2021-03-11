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
	
	/**
	 * Checks if clearing and connection packet sending are disabled.<br>
	 * Same as:
	 * <hr/><blockquote><pre>
	 * !isClearingEnabled() &amp;&amp; !isSendingConnectionPackets();
	 * </pre></blockquote><hr/>
	 *
	 * @return whether clearing and connection packet sending are disabled
	 *
	 * @see #isClearingEnabled()
	 * @see #isSendingConnectionPackets()
	 */
	public boolean isRaw() {
		return !clearingEnabled && !sendingConnectionPackets;
	}
	
	/**
	 * Disables clearing and connection packet sending.<br>
	 * Same as:
	 * <hr/><blockquote><pre>
	 * setClearingEnabled(false);
	 * setSendingConnectionPackets(false);
	 * </pre></blockquote><hr/>
	 *
	 * @return the current instance
	 *
	 * @see #setClearingEnabled(boolean)
	 * @see #setSendingConnectionPackets(boolean)
	 */
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
