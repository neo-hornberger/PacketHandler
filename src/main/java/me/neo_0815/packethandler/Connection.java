package me.neo_0815.packethandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import me.neo_0815.encryption.Encryption;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.SystemPacketType;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

/**
 * The class Connection represents the connection between a client and a server.
 *
 * @author Neo Hornberger
 */
public abstract class Connection {
	private final Connection INSTANCE = this;
	private final Properties properties;
	
	private OutputStream out;
	private ListeningThread listeningThread;
	
	@Getter(AccessLevel.PACKAGE)
	@Accessors(fluent = true)
	private PacketEvalQueueThread packetEvalThread;
	
	private boolean stopped = false;
	
	protected Connection(final Properties properties) {
		this.properties = properties;
	}
	
	public Connection(final OutputStream out, final Properties properties) {
		this(properties);
		
		this.out = out;
	}
	
	protected final void setOut(final OutputStream out) {
		if(this.out != null) throw new IllegalStateException("OutputStream has already been set");
		
		this.out = out;
	}
	
	protected abstract void onPacketReceived(final PacketBase<?> packet, final long id);
	
	protected void onSystemPacketReceived(final PacketBase<?> packet) {
	}
	
	protected void onUnknownPacketReceived(final UnknownPacket packet) {
	}
	
	protected void onMessageReceived(final String message) {
	}
	
	private ByteBuffer constructPacket(final long id, final PacketMap map) {
		return constructionMode().encodePacket(byteBufferGenerator(), id, map, registry());
	}
	
	private ByteBuffer constructPacket(final IPacketFactory packetFactory, final PacketMap map) {
		return constructPacket(registry().getOutgoingPacketId(packetFactory), map);
	}
	
	private ByteBuffer constructPacket(final IPacketType packetType, final PacketMap map) {
		return constructPacket(packetType.id(), map);
	}
	
	private void sendData(final ByteBuffer buf) {
		try {
			if(isEncryptionEnabled()) buf.encrypt(encryption());
			
			buf.writeToOutputStream(out);
		}catch(final IOException e) {
			e.printStackTrace();
		}
	}
	
	public final void sendPacket(final PacketBase<?> packet, final long id) {
		final ByteBuffer buf = byteBufferGenerator().generate();
		
		constructionMode().encodePacket(byteBufferGenerator(), packet, id, registry()).transferTo(buf);
		
		sendData(buf);
	}
	
	public final void sendPacket(final PacketBase<?> packet, final IPacketFactory packetFactory) {
		sendPacket(packet, registry().getOutgoingPacketId(packetFactory));
	}
	
	public final void sendPacket(final PacketBase<?> packet, final IPacketType packetType) {
		sendPacket(packet, packetType.id());
	}
	
	public final void sendPacket(final long... ids) {
		sendPackets(ids, PacketHelper.createMaps(ids.length));
	}
	
	public final void sendPacket(final IPacketFactory... packetFactories) {
		sendPackets(packetFactories, PacketHelper.createMaps(packetFactories.length));
	}
	
	public final void sendPacket(final IPacketType... packetTypes) {
		sendPackets(packetTypes, PacketHelper.createMaps(packetTypes.length));
	}
	
	public final void sendPacket(final long id, final PacketMap map) {
		sendPackets(new long[] { id }, new PacketMap[] { map });
	}
	
	public final void sendPacket(final IPacketFactory packetFactories, final PacketMap map) {
		sendPackets(new IPacketFactory[] { packetFactories }, new PacketMap[] { map });
	}
	
	public final void sendPacket(final IPacketType packetType, final PacketMap map) {
		sendPackets(new IPacketType[] { packetType }, new PacketMap[] { map });
	}
	
	public final void sendPackets(final long[] ids, final PacketMap[] maps) {
		final ByteBuffer buf = byteBufferGenerator().generate();
		
		for(int i = 0; i < ids.length; i++)
			constructPacket(ids[i], maps[i]).transferTo(buf);
		
		sendData(buf);
	}
	
	public final void sendPackets(final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		final ByteBuffer buf = byteBufferGenerator().generate();
		
		for(int i = 0; i < packetFactories.length; i++)
			constructPacket(packetFactories[i], maps[i]).transferTo(buf);
		
		sendData(buf);
	}
	
	public final void sendPackets(final IPacketType[] packetTypes, final PacketMap[] maps) {
		final ByteBuffer buf = byteBufferGenerator().generate();
		
		for(int i = 0; i < packetTypes.length; i++)
			constructPacket(packetTypes[i], maps[i]).transferTo(buf);
		
		sendData(buf);
	}
	
	public final void sendMessage(final String message) {
		sendPacket(SystemPacketType.MESSAGE, PacketMap.of("message", message));
	}
	
	public final void sendMessages(final String[] messages) {
		final IPacketType[] packetTypes = new IPacketType[messages.length];
		final PacketMap[] maps = new PacketMap[messages.length];
		
		Arrays.fill(packetTypes, SystemPacketType.MESSAGE);
		
		for(int i = 0; i < messages.length; i++)
			maps[i] = PacketMap.of("message", messages[i]);
		
		sendPackets(packetTypes, maps);
	}
	
	/**
	 * Initializes the {@link ListeningThread} and the {@link PacketEvalQueueThread} with the {@link Socket} 'socket'.
	 *
	 * @param socket the {@link Socket} which will be used to construct the
	 *               {@link ListeningThread} and the {@link PacketEvalQueueThread}
	 * @throws IOException if an I/O error occurs
	 */
	protected final void initThreads(final Socket socket) throws IOException {
		if(listeningThread != null || packetEvalThread != null) return;
		
		listeningThread = new ListeningThread(socket) {
			
			@Override
			protected Connection connection() {
				return INSTANCE;
			}
		};
		packetEvalThread = new PacketEvalQueueThread(socket) {
			
			@Override
			protected Connection connection() {
				return INSTANCE;
			}
		};
	}
	
	/**
	 * Starts the {@link ListeningThread} and the {@link PacketEvalQueueThread}.
	 *
	 * @see Thread#start()
	 */
	public final void start() {
		if(listeningThread != null && packetEvalThread != null) {
			listeningThread.start();
			packetEvalThread.start();
		}
	}
	
	/**
	 * Interrupts the {@link ListeningThread} and the {@link PacketEvalQueueThread}.
	 *
	 * @see Thread#interrupt()
	 */
	public final void stop() {
		if(listeningThread != null && packetEvalThread != null) {
			listeningThread.interrupt();
			packetEvalThread.interrupt();
			
			try {
				out.close();
			}catch(final IOException e) {
				e.printStackTrace();
			}
			
			stopped = true;
		}
	}
	
	public final void startEncryption() {
		properties.setEncryptionEnabled(true);
	}
	
	public final void stopEncryption() {
		properties.setEncryptionEnabled(false);
	}
	
	/**
	 * @see #stop()
	 */
	public final void disconnect() {
		if(properties.isSendingConnectionPackets()) sendPacket(SystemPacketType.DISCONNECT);
		
		stop();
	}
	
	public final boolean isStopped() {
		return stopped;
	}
	
	public final AbstractPacketRegistry registry() {
		return properties.getPacketRegistry();
	}
	
	@SuppressWarnings("unchecked")
	public final <R extends AbstractPacketRegistry> R getPacketRegistry() {
		return (R) registry();
	}
	
	public final PacketConstructionMode constructionMode() {
		return properties.getPacketConstructionMode();
	}
	
	public final ByteBufferGenerator<?> byteBufferGenerator() {
		return properties.getByteBufferGenerator();
	}
	
	public final boolean isEncryptionEnabled() {
		return properties.isEncryptionEnabled();
	}
	
	public final Encryption encryption() {
		return properties.getEncryption();
	}
}
