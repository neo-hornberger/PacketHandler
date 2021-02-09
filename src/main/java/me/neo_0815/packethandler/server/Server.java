package me.neo_0815.packethandler.server;

import lombok.Synchronized;
import me.neo_0815.packethandler.PacketHelper;
import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.PacketChangeUUID;
import me.neo_0815.packethandler.packet.system.PacketDisconnect;
import me.neo_0815.packethandler.packet.system.SystemPacketType;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.function.Consumer;

/**
 * The abstract class Server represents a server on which {@link Client}s can
 * connect to.
 *
 * @author Neo Hornberger
 */
public abstract class Server {
	private final Server INSTANCE = this;
	
	private final ServerSocket socket;
	private final Properties properties;
	private final Thread acceptingThread;
	
	private final Map<UUID, ClientConnection> clients = new HashMap<>();
	private final Map<UUID, ClientGroup> clientGroups = new HashMap<>();
	
	private Thread clearingThread;
	private boolean haltAccepting = false, blockConnecting = false;
	
	/**
	 * Constructs a new {@link Server} and bind it to the port 'port'.
	 *
	 * @param port the port the {@link Server} will bound to
	 * @throws IOException if an I/O error occurs when opening the {@link ServerSocket}
	 */
	public Server(final int port, final Properties properties) throws IOException {
		this((InetAddress) null, port, properties);
	}
	
	public Server(final String bindAddress, final int port, final Properties properties) throws IOException {
		this(InetAddress.getByName(bindAddress), port, properties);
	}
	
	public Server(final InetAddress bindAddress, final int port, final Properties properties) throws IOException {
		socket = new ServerSocket(port, 0, bindAddress);
		
		this.properties = properties;
		
		acceptingThread = new Thread("Accepting-Thread -- " + socket.getLocalPort()) {
			
			@Override
			public void run() {
				while(!isInterrupted())
					try {
						final Socket client = socket.accept();
						
						if(haltAccepting) {
							if(blockConnecting) client.close();
						}else {
							UUID uuid;
							do
								uuid = UUID.randomUUID();
							while(hasClient(uuid));
							
							final ClientConnection clientCon = new ClientConnection(INSTANCE, client, uuid, properties.copy());
							
							clients.put(uuid, clientCon);
							
							if(properties.isSendingConnectionPackets())
								clientCon.sendPacket(SystemPacketType.CONNECT, PacketMap.of("uuid", uuid));
							
							onClientConnected(uuid);
							
							clientCon.start();
						}
					}catch(final IOException e) {
						e.printStackTrace();
					}
			}
		};
		
		if(properties.isClearingEnabled()) clearingThread = new Thread("ClearingThread -- " + socket.getLocalPort()) {
			private final LinkedList<UUID> toRemove = new LinkedList<>();
			
			@Override
			public void run() {
				while(!isInterrupted()) {
					toRemove.clear();
					
					final long currentTime = System.currentTimeMillis();
					
					clients.forEach((uuid, client) -> {
						final long currentDiff = currentTime - client.lastPacket;
						
						if(currentDiff > 50_000) disconnectClient(uuid);
						else if(currentDiff > 10_000) client.sendPacket(SystemPacketType.WAKE);
						
						if(client.isStopped()) toRemove.add(uuid);
					});
					
					toRemove.forEach(clients::remove);
					
					try {
						Thread.sleep(10_000); // TODO make user-controllable
					}catch(final InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		};
	}
	
	/**
	 * Starts the accepting thread of this {@link Server}.
	 *
	 * @see Thread#start()
	 */
	public final void start() {
		acceptingThread.start();
		
		if(properties.isClearingEnabled()) clearingThread.start();
	}
	
	public final void halt() {
		haltAccepting = !haltAccepting;
	}
	
	public final void halt(final boolean blockClients) {
		halt();
		
		blockConnecting = blockClients;
	}
	
	public final boolean isHalting() {
		return haltAccepting;
	}
	
	/**
	 * Interrupts the accepting thread of this {@link Server} and disconnects all {@link ClientConnection}s.
	 *
	 * @see Thread#interrupt()
	 * @see #disconnectAll()
	 */
	public final void stop() {
		acceptingThread.interrupt();
		
		if(properties.isClearingEnabled()) clearingThread.interrupt();
		
		disconnectAll();
	}
	
	/**
	 * Disconnects all {@link ClientConnection}s from the server.
	 *
	 * @see ClientConnection#disconnect()
	 * @see Map#clear()
	 */
	@Synchronized("clients")
	public final void disconnectAll() {
		clients.values().forEach(ClientConnection::disconnect);
		clients.clear();
	}
	
	public final void startEncryptionForClient(final UUID client) {
		computeOnClientIfPresent(client, ClientConnection::startEncryption);
	}
	
	public final void stopEncryptionForClient(final UUID client) {
		computeOnClientIfPresent(client, ClientConnection::stopEncryption);
	}
	
	public final void disconnectClient(final UUID client) {
		computeOnClientIfPresent(client, ClientConnection::disconnect);
		
		synchronized(clients) {
			clients.remove(client);
		}
	}
	
	@Synchronized("clients")
	public final Set<UUID> getClients() {
		return Collections.unmodifiableSet(clients.keySet());
	}
	
	@Synchronized("clients")
	public final boolean hasClient(final UUID client) {
		return clients.containsKey(client);
	}
	
	@Synchronized("clients")
	protected final ClientConnection getClient(final UUID client) {
		if(!hasClient(client)) {
			System.err.println(client + " is not registered!");
			
			return null;
		}else return clients.get(client);
	}
	
	protected final void computeOnClientIfPresent(final UUID client, final Consumer<ClientConnection> consumer) {
		if(!hasClient(client)) {
			System.err.println(client + " is not registered!");
			
			return;
		}
		
		synchronized(clients) {
			consumer.accept(clients.get(client));
		}
	}
	
	@Synchronized("clients")
	public final Set<UUID> getClientGroups() {
		return Collections.unmodifiableSet(clientGroups.keySet());
	}
	
	@Synchronized("clients")
	public final boolean hasClientGroup(final UUID clientGroup) {
		return clientGroups.containsKey(clientGroup);
	}
	
	@Synchronized("clients")
	protected final ClientGroup getClientGroup(final UUID clientGroup) {
		if(!hasClientGroup(clientGroup)) {
			System.err.println(clientGroup + " is not registered!");
			
			return null;
		}else return clientGroups.get(clientGroup);
	}
	
	protected final void changeClientUUID(final UUID client, final UUID uuid) {
		if(client == null || client.equals(uuid)) return;
		
		computeOnClientIfPresent(client, cc -> {
			if(hasClient(uuid)) {
				System.err.println(uuid + " is already registered!");
				
				cc.changeUUID(client);
			}else {
				clients.put(uuid, cc);
				clients.remove(client);
				
				cc.changeUUID(uuid);
			}
		});
	}
	
	protected abstract void onPacketReceived(final UUID client, final PacketBase<?> packet, final long id);
	
	protected final void onSystemPacketReceived(final UUID client, final PacketBase<?> packet) {
		if(packet instanceof PacketDisconnect) {
			synchronized(clients) {
				clients.get(client).stop();
				clients.remove(client);
			}
			
			onClientDisconnected(client);
		}else if(packet instanceof PacketChangeUUID) {
			changeClientUUID(client, ((PacketChangeUUID) packet).uuid);
			
			onClientUUIDChanged(((PacketChangeUUID) packet).uuid, client);
		}
	}
	
	protected void onUnknownPacketReceived(final UUID client, final UnknownPacket packet) {
	}
	
	protected void onMessageReceived(final UUID client, final String message) {
	}
	
	protected void onClientConnected(final UUID client) {
	}
	
	protected void onClientDisconnected(final UUID client) {
	}
	
	protected void onClientUUIDChanged(final UUID client, final UUID oldUUID) {
	}
	
	public final void sendPacket(final UUID client, final PacketBase<?> packet, final long id) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(packet, id));
	}
	
	public final void sendPacket(final UUID client, final PacketBase<?> packet, final IPacketFactory packetFactory) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(packet, packetFactory));
	}
	
	public final void sendPacket(final UUID client, final PacketBase<?> packet, final IPacketType packetType) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(packet, packetType));
	}
	
	public final void sendPacket(final UUID client, final long... ids) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(ids));
	}
	
	public final void sendPacket(final UUID client, final IPacketFactory... packetFactories) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(packetFactories));
	}
	
	public final void sendPacket(final UUID client, final IPacketType... packetTypes) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(packetTypes));
	}
	
	public final void sendPacket(final UUID client, final long id, final PacketMap map) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(id, map));
	}
	
	public final void sendPacket(final UUID client, final IPacketFactory packetFactory, final PacketMap map) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(packetFactory, map));
	}
	
	public final void sendPacket(final UUID client, final IPacketType packetType, final PacketMap map) {
		computeOnClientIfPresent(client, cc -> cc.sendPacket(packetType, map));
	}
	
	public final void sendPackets(final UUID client, final long[] ids, final PacketMap[] maps) {
		computeOnClientIfPresent(client, cc -> cc.sendPackets(ids, maps));
	}
	
	public final void sendPackets(final UUID client, final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		computeOnClientIfPresent(client, cc -> cc.sendPackets(packetFactories, maps));
	}
	
	public final void sendPackets(final UUID client, final IPacketType[] packetTypes, final PacketMap[] maps) {
		computeOnClientIfPresent(client, cc -> cc.sendPackets(packetTypes, maps));
	}
	
	public final void sendMessage(final UUID client, final String message) {
		computeOnClientIfPresent(client, cc -> cc.sendMessage(message));
	}
	
	public final void sendMessages(final UUID client, final String[] messages) {
		computeOnClientIfPresent(client, cc -> cc.sendMessages(messages));
	}
	
	@Synchronized("clients")
	public final void broadcastPacket(final PacketBase<?> packet, final long id) {
		clients.values().forEach(client -> client.sendPacket(packet, id));
	}
	
	@Synchronized("clients")
	public final void broadcastPacket(final PacketBase<?> packet, final IPacketFactory packetFactory) {
		clients.values().forEach(client -> client.sendPacket(packet, packetFactory));
	}
	
	@Synchronized("clients")
	public final void broadcastPacket(final PacketBase<?> packet, final IPacketType packetType) {
		clients.values().forEach(client -> client.sendPacket(packet, packetType));
	}
	
	public final void broadcastPacket(final long... ids) {
		broadcastPackets(ids, PacketHelper.createMaps(ids.length));
	}
	
	public final void broadcastPacket(final IPacketFactory... packetFactories) {
		broadcastPackets(packetFactories, PacketHelper.createMaps(packetFactories.length));
	}
	
	public final void broadcastPacket(final IPacketType... packetTypes) {
		broadcastPackets(packetTypes, PacketHelper.createMaps(packetTypes.length));
	}
	
	public final void broadcastPacket(final long id, final PacketMap map) {
		broadcastPackets(new long[] { id }, new PacketMap[] { map });
	}
	
	public final void broadcastPacket(final IPacketFactory packetFactory, final PacketMap map) {
		broadcastPackets(new IPacketFactory[] { packetFactory }, new PacketMap[] { map });
	}
	
	public final void broadcastPacket(final IPacketType packetType, final PacketMap map) {
		broadcastPackets(new IPacketType[] { packetType }, new PacketMap[] { map });
	}
	
	@Synchronized("clients")
	public final void broadcastPackets(final long[] ids, final PacketMap[] maps) {
		clients.values().forEach(client -> client.sendPackets(ids, maps));
	}
	
	@Synchronized("clients")
	public final void broadcastPackets(final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		clients.values().forEach(client -> client.sendPackets(packetFactories, maps));
	}
	
	@Synchronized("clients")
	public final void broadcastPackets(final IPacketType[] packetTypes, final PacketMap[] maps) {
		clients.values().forEach(client -> client.sendPackets(packetTypes, maps));
	}
	
	public final void broadcastMessage(final String message) {
		broadcastPacket(SystemPacketType.MESSAGE, PacketMap.of("message", message));
	}
	
	public final void broadcastMessages(final String[] messages) {
		final IPacketType[] packetTypes = new IPacketType[messages.length];
		final PacketMap[] maps = new PacketMap[messages.length];
		
		Arrays.fill(packetTypes, SystemPacketType.MESSAGE);
		
		for(int i = 0; i < messages.length; i++)
			maps[i] = PacketMap.of("message", messages[i]);
		
		broadcastPackets(packetTypes, maps);
	}
	
	public final AbstractPacketRegistry registry(final UUID client) {
		return client != null ? getClient(client).registry() : properties.getPacketRegistry();
	}
	
	public final <R extends AbstractPacketRegistry> R getPacketRegistry(final UUID client) {
		return client != null ? getClient(client).getPacketRegistry() : (R) properties.getPacketRegistry();
	}
}
