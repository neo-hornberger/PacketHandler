package me.neo_0815.packethandler;

import me.neo_0815.encryption.Encryption;
import me.neo_0815.packethandler.PacketConstructionMode.PacketIdPair;
import me.neo_0815.packethandler.ThreadExecutors.ListeningThread;
import me.neo_0815.packethandler.ThreadExecutors.PacketQueueThread;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.SystemPacketType;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The class Connection represents the connection between a client and a server.
 *
 * @author Neo Hornberger
 */
public abstract class Connection extends PacketSender {
	private final Socket socket;
	private final Properties properties;
	
	private OutputStream out;
	private Future<?> listeningThread;
	
	@Getter(AccessLevel.PACKAGE)
	private final BlockingQueue<PacketIdPair> packetQueue = new LinkedBlockingQueue<>();
	private Future<?> packetQueueThread;
	
	private boolean stopped = false;
	
	public Connection(@NonNull final Socket socket, @NonNull final Properties properties) throws IOException {
		this.socket = socket;
		this.properties = properties;
		
		try {
			out = socket.getOutputStream();
		}catch(final SocketException se) {
			out = null;
		}
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
	 * Starts the {@link ListeningThread} and the {@link PacketQueueThread}.
	 */
	public void start() throws IOException {
		if(listeningThread == null && packetQueueThread == null) {
			listeningThread = ThreadExecutors.LISTENING_THREAD_SERVICE.submit(new ListeningThread(this, socket.getInputStream()));
			packetQueueThread = ThreadExecutors.PACKET_QUEUE_THREAD_SERVICE.submit(new PacketQueueThread(this));
		}
	}
	
	/**
	 * Interrupts the {@link ListeningThread} and the {@link PacketQueueThread}.
	 */
	public void stop() {
		if(listeningThread != null && packetQueueThread != null) {
			listeningThread.cancel(true);
			packetQueueThread.cancel(true);
			
			try {
				socket.close();
			}catch(final IOException e) {
				e.printStackTrace();
			}
			
			stopped = true;
		}
	}
	
	/**
	 * Disconnects and stops the connection.
	 *
	 * @see #stop()
	 */
	public void disconnect() {
		if(properties.isSendingConnectionPackets()) sendPacket(SystemPacketType.DISCONNECT);
		
		stop();
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
	public void startEncryption() {
		properties.setEncryptionEnabled(true);
	}
	
	public void stopEncryption() {
		properties.setEncryptionEnabled(false);
	}
	
	@Override
	protected Properties properties() {
		return properties;
	}
	
	public AbstractPacketRegistry registry() {
		return properties.getPacketRegistry();
	}
	
	@SuppressWarnings("unchecked")
	public <R extends AbstractPacketRegistry> R getPacketRegistry() {
		return (R) registry();
	}
	
	public PacketConstructionMode constructionMode() {
		return properties.getPacketConstructionMode();
	}
	
	public ByteBufferGenerator<?> byteBufferGenerator() {
		return properties.getByteBufferGenerator();
	}
	
	public boolean isEncryptionEnabled() {
		return properties.isEncryptionEnabled();
	}
	
	public Encryption encryption() {
		return properties.getEncryption();
	}
	
	@Override
	public String toString() {
		return "[" + socket.getLocalSocketAddress() + " -> " + socket.getRemoteSocketAddress() + "]";
	}
}
