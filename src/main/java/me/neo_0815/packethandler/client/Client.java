package me.neo_0815.packethandler.client;

import me.neo_0815.packethandler.Connection;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.system.*;
import me.neo_0815.packethandler.server.Server;

import lombok.Getter;
import lombok.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * The abstract class Client represents a client that can communicate with a {@link Server} instance.
 *
 * @author Neo Hornberger
 */
public abstract class Client extends Connection {
	
	@Getter
	private UUID uuid;
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at 'localhost:{@code port}'. (localhost:8080)
	 *
	 * @param port       the port number
	 * @param properties the properties
	 *
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the {@link Socket}
	 */
	public Client(final int port, final Properties properties) throws UnknownHostException, IOException {
		this((String) null, port, properties);
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at '{@code host}:{@code port}'. (localhost:8080)
	 *
	 * @param host       the host name, or null for the loopback address
	 * @param port       the port number
	 * @param properties the properties
	 *
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the {@link Socket}
	 */
	public Client(final String host, final int port, final Properties properties) throws UnknownHostException, IOException {
		this(null, host, port, properties);
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at '{@code address}:{@code port}'. (localhost:8080)
	 *
	 * @param address    the address
	 * @param port       the port number
	 * @param properties the properties
	 *
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the {@link Socket}
	 */
	public Client(@NonNull final InetAddress address, final int port, final Properties properties) throws UnknownHostException, IOException {
		this(null, address, port, properties);
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at 'localhost:{@code port}'. (localhost:8080)
	 *
	 * @param uuid       the uuid
	 * @param port       the port number
	 * @param properties the properties
	 *
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the {@link Socket}
	 */
	public Client(final UUID uuid, final int port, final Properties properties) throws UnknownHostException, IOException {
		this(uuid, (String) null, port, properties);
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at '{@code host}:{@code port}'. (localhost:8080)
	 *
	 * @param uuid       the uuid
	 * @param host       the host name, or null for the loopback address
	 * @param port       the port number
	 * @param properties the properties
	 *
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the {@link Socket}
	 */
	public Client(final UUID uuid, final String host, final int port, final Properties properties) throws UnknownHostException, IOException {
		this(new Socket(host, port), properties, uuid);
	}
	
	/**
	 * Constructs a new {@link Client} and connects it to the server at '{@code address}:{@code port}'. (localhost:8080)
	 *
	 * @param uuid       the uuid
	 * @param address    the address
	 * @param port       the port number
	 * @param properties the properties
	 *
	 * @throws UnknownHostException if the IP address of the host could not be determined
	 * @throws IOException          if an I/O error occurs when creating the {@link Socket}
	 */
	public Client(final UUID uuid, @NonNull final InetAddress address, final int port, final Properties properties) throws UnknownHostException, IOException {
		this(new Socket(address, port), properties, uuid);
	}
	
	private Client(final Socket socket, @NonNull final Properties properties, final UUID uuid) throws IOException {
		super(socket, properties);
		
		this.uuid = uuid;
	}
	
	public void changeUUID(@NonNull final UUID uuid) {
		sendPacket(SystemPacketType.CHANGE_UUID, PacketMap.of("uuid", uuid));
	}
	
	@Override
	protected void onSystemPacketReceived(final PacketBase<?> packet) {
		if(packet instanceof PacketDisconnect) {
			stop();
			
			onDisconnected();
		}else if(packet instanceof PacketConnect) {
			onUUIDChanged(uuid, uuid = ((PacketConnect) packet).uuid);
			
			onConnected();
		}else if(packet instanceof PacketChangeUUID) onUUIDChanged(uuid, uuid = ((PacketChangeUUID) packet).uuid);
		else if(packet instanceof PacketWake && properties().isClearingEnabled()) sendPacket(SystemPacketType.WAKE);
	}
	
	protected void onConnected() {
	}
	
	protected void onDisconnected() {
	}
	
	protected void onUUIDChanged(final UUID oldUUID, final UUID newUUID) {
	}
	
	@Override
	public String toString() {
		return "Client" + super.toString();
	}
}
