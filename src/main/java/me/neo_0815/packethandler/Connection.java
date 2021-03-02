package me.neo_0815.packethandler;

import me.neo_0815.encryption.Encryption;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.SystemPacketType;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * The class Connection represents the connection between a client and a server.
 *
 * @author Neo Hornberger
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Connection extends PacketSender {
	private final Connection INSTANCE = this;
	private final Properties properties;
	
	private OutputStream out;
	private ListeningThread listeningThread;
	
	@Getter(AccessLevel.PACKAGE)
	@Accessors(fluent = true)
	private PacketQueueThread packetQueueThread;
	
	private boolean stopped = false;
	
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
	
	@Override
	protected final void sendData(final ByteBuffer buf) {
		try {
			if(isEncryptionEnabled()) buf.encrypt(encryption());
			
			buf.writeToOutputStream(out);
		}catch(final IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Initializes the {@link ListeningThread} and the {@link PacketQueueThread} with the {@link Socket} 'socket'.
	 *
	 * @param socket the {@link Socket} which will be used to construct the
	 *               {@link ListeningThread} and the {@link PacketQueueThread}
	 * @throws IOException if an I/O error occurs
	 */
	protected final void initThreads(final Socket socket) throws IOException {
		if(listeningThread != null || packetQueueThread != null) return;
		
		listeningThread = new ListeningThread(socket) {
			
			@Override
			protected Connection connection() {
				return INSTANCE;
			}
		};
		packetQueueThread = new PacketQueueThread(socket) {
			
			@Override
			protected Connection connection() {
				return INSTANCE;
			}
		};
	}
	
	/**
	 * Starts the {@link ListeningThread} and the {@link PacketQueueThread}.
	 *
	 * @see Thread#start()
	 */
	public final void start() {
		if(listeningThread != null && packetQueueThread != null) {
			listeningThread.start();
			packetQueueThread.start();
		}
	}
	
	/**
	 * Interrupts the {@link ListeningThread} and the {@link PacketQueueThread}.
	 *
	 * @see Thread#interrupt()
	 */
	public final void stop() {
		if(listeningThread != null && packetQueueThread != null) {
			listeningThread.interrupt();
			packetQueueThread.interrupt();
			
			try {
				out.close();
			}catch(final IOException e) {
				e.printStackTrace();
			}
			
			stopped = true;
		}
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
	
	public final void startEncryption() {
		properties.setEncryptionEnabled(true);
	}
	
	public final void stopEncryption() {
		properties.setEncryptionEnabled(false);
	}
	
	@Override
	protected final Properties properties() {
		return properties;
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
	
	/*
	 *
	 * PacketSender methods overridden and finalized
	 *
	 */
	
	@Override
	public final void sendPacket(final PacketBase<?> packet, final long id) {
		super.sendPacket(packet, id);
	}
	
	@Override
	public final void sendPacket(final long id, final PacketMap map) {
		super.sendPacket(id, map);
	}
	
	@Override
	public final void sendPacket(final IPacketFactory packetFactory, final PacketMap map) {
		super.sendPacket(packetFactory, map);
	}
	
	@Override
	public final void sendPacket(final IPacketType packetType, final PacketMap map) {
		super.sendPacket(packetType, map);
	}
	
	@Override
	public final void sendPacket(final PacketBase<?> packet, final IPacketFactory packetFactory) {
		super.sendPacket(packet, packetFactory);
	}
	
	@Override
	public final void sendPacket(final PacketBase<?> packet, final IPacketType packetType) {
		super.sendPacket(packet, packetType);
	}
	
	@Override
	public final void sendPacket(final long... ids) {
		super.sendPacket(ids);
	}
	
	@Override
	public final void sendPacket(final IPacketFactory... packetFactories) {
		super.sendPacket(packetFactories);
	}
	
	@Override
	public final void sendPacket(final IPacketType... packetTypes) {
		super.sendPacket(packetTypes);
	}
	
	@Override
	public final void sendPackets(final long[] ids, final PacketMap[] maps) {
		super.sendPackets(ids, maps);
	}
	
	@Override
	public final void sendPackets(final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		super.sendPackets(packetFactories, maps);
	}
	
	@Override
	public final void sendPackets(final IPacketType[] packetTypes, final PacketMap[] maps) {
		super.sendPackets(packetTypes, maps);
	}
	
	@Override
	public final void sendMessage(final String message) {
		super.sendMessage(message);
	}
	
	@Override
	public final void sendMessages(final String[] messages) {
		super.sendMessages(messages);
	}
}
