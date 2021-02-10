package me.neo_0815.packethandler.client;

import lombok.Getter;
import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.system.*;
import me.neo_0815.packethandler.server.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * The abstract class Client represents a client that can communicate with a
 * {@link Server} instance.
 *
 * @author Neo Hornberger
 */
public abstract class Client extends Connection {
	private final Socket socket;
	
	@Getter
	private UUID uuid;
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at
	 * 'host':'port'. (localhost:8080)
	 *
	 * @param host the host name, or null for the loopback address
	 * @param port the port number
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the socket
	 */
	public Client(final String host, final int port, final Properties properties) throws UnknownHostException, IOException {
		this(null, host, port, properties);
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at
	 * 'address':'port'. (localhost:8080)
	 *
	 * @param address the address, or null for the loopback address
	 * @param port    the port number
	 * @throws IOException if an I/O error occurs when creating the socket
	 */
	public Client(final InetAddress address, final int port, final Properties properties) throws IOException {
		this(null, address, port, properties);
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at
	 * 'host':'port'. (localhost:8080)
	 *
	 * @param uuid
	 * @param host the host name, or null for the loopback address
	 * @param port the port number
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the socket
	 */
	public Client(final UUID uuid, final String host, final int port, final Properties properties) throws UnknownHostException, IOException {
		super(properties);
		
		this.uuid = uuid;
		
		socket = new Socket(host, port);
		
		init();
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at
	 * 'address':'port'. (localhost:8080)
	 *
	 * @param uuid
	 * @param address the address, or null for the loopback address
	 * @param port    the port number
	 * @throws IOException if an I/O error occurs when creating the socket
	 */
	public Client(final UUID uuid, final InetAddress address, final int port, final Properties properties) throws IOException {
		super(properties);
		
		this.uuid = uuid;
		
		socket = new Socket(address, port);
		
		init();
	}
	
	private void init() throws IOException {
		setOut(socket.getOutputStream());
		initThreads(socket);
	}
	
	public final void changeUUID(final UUID uuid) {
		sendPacket(SystemPacketType.CHANGE_UUID, PacketMap.of("uuid", uuid));
	}
	
	@Override
	protected final void onSystemPacketReceived(final PacketBase<?> packet) {
		if(packet instanceof PacketDisconnect) {
			stop();
			
			onDisconnected();
		}else if(packet instanceof PacketConnect) {
			onUUIDChanged(uuid, uuid = ((PacketConnect) packet).uuid);
			
			onConnected();
		}else if(packet instanceof PacketChangeUUID) onUUIDChanged(uuid, uuid = ((PacketChangeUUID) packet).uuid);
		else if(packet instanceof PacketWake) sendPacket(SystemPacketType.WAKE);
	}
	
	protected void onConnected() {
	}
	
	protected void onDisconnected() {
	}
	
	protected void onUUIDChanged(final UUID oldUUID, final UUID newUUID) {
	}
}
