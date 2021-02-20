package me.neo_0815.packethandler;

import me.neo_0815.packethandler.PacketConstructionMode.PacketIdPair;
import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.Packet;
import me.neo_0815.packethandler.server.ClientConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

/**
 * This thread is listening to an {@link InputStream}, reading the transferred
 * bytes,<br>
 * writing these bytes to a {@link ByteBuffer}, executing the matching
 * {@link Packet}.
 *
 * @author Neo Hornberger
 */
public abstract class ListeningThread extends Thread {
	private final InputStream in;
	
	/**
	 * Constructs the {@link ListeningThread} with the {@link Socket} 'socket'.
	 *
	 * @param socket the {@link Socket}s {@link InputStream} will be used to
	 *               construct this {@link ListeningThread}
	 * @throws IOException if an I/O error occurs
	 */
	public ListeningThread(final Socket socket) throws IOException {
		in = socket.getInputStream();
		
		setName("Listening-Thread(" + threadName() + ") -- " + socket.getLocalAddress() + ":" + socket.getLocalPort());
	}
	
	@Override
	public final void run() {
		while(!isInterrupted())
			try {
				if(in.available() <= 0) {
					onSpinWait();
					
					continue;
				}
				
				final ByteBuffer buf = connection().byteBufferGenerator().generate().readFromInputStream(in);
				
				PacketIdPair pip;
				while(!buf.isEmpty()) {
					if(connection().isEncryptionEnabled()) buf.decrypt(connection().encryption());
					
					pip = connection().constructionMode().decodePacket(connection().byteBufferGenerator(), buf, connection().registry());
					
					connection().packetQueueThread().packetQueue.offer(pip);
				}
			}catch(final IOException e) {
				e.printStackTrace();
			}
	}
	
	@Override
	public void interrupt() {
		super.interrupt();
		
		try {
			in.close();
		}catch(final IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * The {@link Connection} instance associated with this
	 * {@link ListeningThread}.
	 *
	 * @return the {@link Connection} instance
	 */
	protected abstract Connection connection();
	
	private String threadName() {
		return connection() instanceof Client ? "Client" : connection() instanceof ClientConnection ? "Server" : "None";
	}
}
