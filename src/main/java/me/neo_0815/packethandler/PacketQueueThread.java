package me.neo_0815.packethandler;

import me.neo_0815.packethandler.PacketConstructionMode.PacketIdPair;
import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.PacketPrimitiveMessage;
import me.neo_0815.packethandler.server.ClientConnection;

import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class PacketQueueThread extends Thread {
	final BlockingQueue<PacketIdPair> packetQueue = new LinkedBlockingQueue<>();
	
	public PacketQueueThread(final Socket socket) {
		setName("PacketQueue-Thread(" + threadName() + ") -- " + socket.getLocalAddress() + ":" + socket.getLocalPort());
	}
	
	@Override
	public void run() {
		while(!isInterrupted())
			try {
				final PacketIdPair pip = packetQueue.take();
				
				if(pip.id() >= 0) connection().onPacketReceived(pip.packet(), pip.id());
				else {
					if(pip.id() == UnknownPacket.ID)
						connection().onUnknownPacketReceived((UnknownPacket) pip.packet());
					else if(pip.packet() instanceof PacketPrimitiveMessage)
						connection().onMessageReceived(((PacketPrimitiveMessage) pip.packet()).message);
					else connection().onSystemPacketReceived(pip.packet());
				}
			}catch(final InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * The {@link Connection} instance associated with this
	 * {@link PacketQueueThread}.
	 *
	 * @return the {@link Connection} instance
	 */
	protected abstract Connection connection();
	
	private String threadName() {
		return connection() instanceof Client ? "Client" : connection() instanceof ClientConnection ? "Server" : "None";
	}
}
