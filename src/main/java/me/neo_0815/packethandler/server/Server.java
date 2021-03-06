package me.neo_0815.packethandler.server;

import me.neo_0815.packethandler.PacketMap;
import me.neo_0815.packethandler.PacketSender;
import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.ThreadExecutors;
import me.neo_0815.packethandler.ThreadExecutors.NamedThreadRunnable;
import me.neo_0815.packethandler.ThreadExecutors.RepeatableRunnable;
import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.PacketChangeUUID;
import me.neo_0815.packethandler.packet.system.PacketDisconnect;
import me.neo_0815.packethandler.packet.system.SystemPacketType;
import me.neo_0815.packethandler.registry.AbstractPacketRegistry;
import me.neo_0815.packethandler.registry.IPacketFactory;
import me.neo_0815.packethandler.registry.IPacketType;

import lombok.NonNull;
import lombok.Synchronized;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Consumer;

/**
 * The abstract class Server represents a server on which {@link Client}s can connect to.
 *
 * @author Neo Hornberger
 */
public abstract class Server {
	private final Server INSTANCE = this;
	
	private final ServerSocket socket;
	private final Properties properties;
	
	private final Set<UUID> uuids = new HashSet<>();
	private final Map<UUID, ClientConnection> clients = new HashMap<>();
	private final Map<UUID, ClientGroup> clientGroups = new HashMap<>();
	
	private Future<?> acceptingThread;
	private ScheduledFuture<?> clearingThread;
	private boolean haltAccepting = false, blockConnecting = false;
	
	/**
	 * Constructs a new {@link Server} and binds it to the {@code port}.
	 *
	 * @param port the port the {@link Server} will bound to
	 * @throws IOException if an I/O error occurs when opening the {@link ServerSocket}
	 */
	public Server(final int port, final Properties properties) throws IOException {
		this((InetAddress) null, port, properties);
	}
	
	/**
	 * Constructs a new {@link Server} and binds it to the {@code bindAddress} on {@code port}.
	 *
	 * @param bindAddress the address the {@link Server} will be bound to
	 * @param port        the port the {@link Server} will be bound to
	 * @throws IOException if an I/O error occurs when opening the {@link ServerSocket}
	 */
	public Server(final String bindAddress, final int port, final Properties properties) throws IOException {
		this(InetAddress.getByName(bindAddress), port, properties);
	}
	
	/**
	 * Constructs a new {@link Server} and binds it to the {@code bindAddress} on {@code port}.
	 *
	 * @param bindAddress the address the {@link Server} will be bound to
	 * @param port        the port the {@link Server} will be bound to
	 * @throws IOException if an I/O error occurs when opening the {@link ServerSocket}
	 */
	public Server(final InetAddress bindAddress, final int port, @NonNull final Properties properties) throws IOException {
		socket = new ServerSocket(port, 0, bindAddress);
		
		this.properties = properties;
	}
	
	/**
	 * Starts the accepting thread of this {@link Server}.
	 */
	public void start() {
		acceptingThread = ThreadExecutors.ACCEPTING_THREAD_SERVICE.submit(new AcceptingThread());
		
		if(properties.isClearingEnabled())
			clearingThread = ThreadExecutors.CLEARING_THREAD_SERVICE.scheduleWithFixedDelay(new ClearingThread(), 0L, properties.getClearingInterval(), properties.getClearingIntervalUnit());
	}
	
	public void halt() {
		haltAccepting = !haltAccepting;
	}
	
	public void halt(final boolean blockClients) {
		halt();
		
		blockConnecting = blockClients;
	}
	
	public boolean isHalting() {
		return haltAccepting;
	}
	
	/**
	 * Interrupts the accepting thread of this {@link Server} and disconnects all {@link ClientConnection}s.
	 *
	 * @see #disconnectAll()
	 */
	public void stop() {
		acceptingThread.cancel(true);
		
		if(properties.isClearingEnabled()) clearingThread.cancel(true);
		
		disconnectAll();
	}
	
	/**
	 * Disconnects all {@link ClientConnection}s from the server.
	 *
	 * @see ClientConnection#disconnect()
	 */
	@Synchronized("clients")
	public void disconnectAll() {
		clients.values().forEach(ClientConnection::disconnect);
		clients.clear();
	}
	
	public void startEncryptionForClient(final UUID client) {
		computeOnClientIfPresent(client, ClientConnection::startEncryption);
	}
	
	public void stopEncryptionForClient(final UUID client) {
		computeOnClientIfPresent(client, ClientConnection::stopEncryption);
	}
	
	/**
	 * Disconnects the client associated with the {@link UUID} {@code uuid} from the server.
	 *
	 * @param client the uuid
	 * @see ClientConnection#disconnect()
	 */
	public void disconnectClient(final UUID client) {
		computeOnClientIfPresent(client, ClientConnection::disconnect);
		
		synchronized(clients) {
			clients.remove(client);
		}
	}
	
	@Synchronized("uuids")
	public Set<UUID> getRegisteredUUIDs() {
		return new HashSet<>(uuids);
	}
	
	@Synchronized("uuids")
	public boolean isRegisteredUUID(final UUID uuid) {
		return uuids.contains(uuid);
	}
	
	@Synchronized("uuids")
	private UUID newUUID() {
		UUID uuid;
		do
			uuid = UUID.randomUUID();
		while(!uuids.add(uuid));
		
		return uuid;
	}
	
	protected PacketSender getPacketSender(final UUID uuid) {
		if(!isRegisteredUUID(uuid)) {
			System.err.println(uuid + " is not registered!");
			
			return null;
		}
		
		if(hasClient(uuid)) return getClient(uuid);
		if(hasClientGroup(uuid)) return getClientGroup(uuid);
		
		throw new IllegalStateException();
	}
	
	protected void computeOnSenderIfPresent(final UUID uuid, final Consumer<PacketSender> consumer) {
		final PacketSender ps = getPacketSender(uuid);
		
		if(ps != null) consumer.accept(ps);
	}
	
	@Synchronized("clients")
	public Set<UUID> getClients() {
		return new HashSet<>(clients.keySet());
	}
	
	@Synchronized("clients")
	public boolean hasClient(final UUID client) {
		return clients.containsKey(client);
	}
	
	protected ClientConnection getClient(final UUID client) {
		if(!hasClient(client)) {
			System.err.println(client + " is not a registered client!");
			
			return null;
		}
		
		synchronized(clients) {
			return clients.get(client);
		}
	}
	
	protected void computeOnClientIfPresent(final UUID client, final Consumer<ClientConnection> consumer) {
		final ClientConnection cc = getClient(client);
		
		if(cc != null) consumer.accept(cc);
	}
	
	@Synchronized("clientGroups")
	public Set<UUID> getClientGroups() {
		return new HashSet<>(clientGroups.keySet());
	}
	
	@Synchronized("clientGroups")
	public boolean hasClientGroup(final UUID clientGroup) {
		return clientGroups.containsKey(clientGroup);
	}
	
	protected ClientGroup getClientGroup(final UUID clientGroup) {
		if(!hasClientGroup(clientGroup)) {
			System.err.println(clientGroup + " is not a registered client-group!");
			
			return null;
		}
		
		synchronized(clientGroups) {
			return clientGroups.get(clientGroup);
		}
	}
	
	protected void computeOnClientGroupIfPresent(final UUID clientGroup, final Consumer<ClientGroup> consumer) {
		final ClientGroup cg = getClientGroup(clientGroup);
		
		if(cg != null) consumer.accept(cg);
	}
	
	protected ClientGroup createClientGroup() {
		final UUID uuid = newUUID();
		final ClientGroup group = new ClientGroup(INSTANCE, uuid);
		
		synchronized(clientGroups) {
			clientGroups.put(uuid, group);
		}
		
		return group;
	}
	
	protected void changeClientUUID(@NonNull final UUID client, @NonNull final UUID uuid) {
		if(client.equals(uuid)) return;
		
		computeOnClientIfPresent(client, cc -> {
			if(isRegisteredUUID(uuid)) {
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
	
	protected void onSystemPacketReceived(final UUID client, final PacketBase<?> packet) {
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
	
	public void sendPacket(final UUID uuid, final PacketBase<?> packet, final long id) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(packet, id));
	}
	
	public void sendPacket(final UUID uuid, final long id, final PacketMap map) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(id, map));
	}
	
	public void sendPacket(final UUID uuid, final IPacketFactory packetFactory, final PacketMap map) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(packetFactory, map));
	}
	
	public void sendPacket(final UUID uuid, final IPacketType packetType, final PacketMap map) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(packetType, map));
	}
	
	public void sendPacket(final UUID uuid, final PacketBase<?> packet, final IPacketFactory packetFactory) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(packet, packetFactory));
	}
	
	public void sendPacket(final UUID uuid, final PacketBase<?> packet, final IPacketType packetType) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(packet, packetType));
	}
	
	public void sendPacket(final UUID uuid, final long... ids) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(ids));
	}
	
	public void sendPacket(final UUID uuid, final IPacketFactory... packetFactories) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(packetFactories));
	}
	
	public void sendPacket(final UUID uuid, final IPacketType... packetTypes) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPacket(packetTypes));
	}
	
	public void sendPackets(final UUID uuid, final long[] ids, final PacketMap[] maps) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPackets(ids, maps));
	}
	
	public void sendPackets(final UUID uuid, final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPackets(packetFactories, maps));
	}
	
	public void sendPackets(final UUID uuid, final IPacketType[] packetTypes, final PacketMap[] maps) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendPackets(packetTypes, maps));
	}
	
	public void sendMessage(final UUID uuid, final String message) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendMessage(message));
	}
	
	public void sendMessages(final UUID uuid, final String[] messages) {
		computeOnSenderIfPresent(uuid, sender -> sender.sendMessages(messages));
	}
	
	@Synchronized("clients")
	protected void computeOnAllClients(final Consumer<ClientConnection> consumer) {
		clients.values().forEach(consumer);
	}
	
	public void broadcastPacket(final PacketBase<?> packet, final long id) {
		computeOnAllClients(client -> client.sendPacket(packet, id));
	}
	
	public void broadcastPacket(final long id, final PacketMap map) {
		computeOnAllClients(client -> client.sendPacket(id, map));
	}
	
	public void broadcastPacket(final IPacketFactory packetFactory, final PacketMap map) {
		computeOnAllClients(client -> client.sendPacket(packetFactory, map));
	}
	
	public void broadcastPacket(final IPacketType packetType, final PacketMap map) {
		computeOnAllClients(client -> client.sendPacket(packetType, map));
	}
	
	public void broadcastPacket(final PacketBase<?> packet, final IPacketFactory packetFactory) {
		computeOnAllClients(client -> client.sendPacket(packet, packetFactory));
	}
	
	public void broadcastPacket(final PacketBase<?> packet, final IPacketType packetType) {
		computeOnAllClients(client -> client.sendPacket(packet, packetType));
	}
	
	public void broadcastPacket(final long... ids) {
		computeOnAllClients(client -> client.sendPacket(ids));
	}
	
	public void broadcastPacket(final IPacketFactory... packetFactories) {
		computeOnAllClients(client -> client.sendPacket(packetFactories));
	}
	
	public void broadcastPacket(final IPacketType... packetTypes) {
		computeOnAllClients(client -> client.sendPacket(packetTypes));
	}
	
	public void broadcastPackets(final long[] ids, final PacketMap[] maps) {
		computeOnAllClients(client -> client.sendPackets(ids, maps));
	}
	
	public void broadcastPackets(final IPacketFactory[] packetFactories, final PacketMap[] maps) {
		computeOnAllClients(client -> client.sendPackets(packetFactories, maps));
	}
	
	public void broadcastPackets(final IPacketType[] packetTypes, final PacketMap[] maps) {
		computeOnAllClients(client -> client.sendPackets(packetTypes, maps));
	}
	
	public void broadcastMessage(final String message) {
		computeOnAllClients(client -> client.sendMessage(message));
	}
	
	public void broadcastMessages(final String[] messages) {
		computeOnAllClients(client -> client.sendMessages(messages));
	}
	
	public AbstractPacketRegistry registry(final UUID client) {
		return client != null ? getClient(client).registry() : properties.getPacketRegistry();
	}
	
	@SuppressWarnings("unchecked")
	public <R extends AbstractPacketRegistry> R getPacketRegistry(final UUID client) {
		return client != null ? getClient(client).getPacketRegistry() : (R) properties.getPacketRegistry();
	}
	
	@Override
	public String toString() {
		return "Server[" + socket.getLocalSocketAddress() + "]";
	}
	
	private class AcceptingThread extends NamedThreadRunnable implements RepeatableRunnable {
		
		private AcceptingThread() {
			super(" -- " + INSTANCE);
		}
		
		@Override
		public void run() {
			super.run();
			
			RepeatableRunnable.super.run();
		}
		
		@Override
		public boolean repeat() {
			try {
				final Socket client = socket.accept();
				
				if(haltAccepting) {
					if(blockConnecting) client.close();
				}else {
					final UUID uuid = newUUID();
					final ClientConnection clientCon = new ClientConnection(INSTANCE, client, uuid, properties.copy());
					
					synchronized(clients) {
						clients.put(uuid, clientCon);
					}
					
					if(properties.isSendingConnectionPackets())
						clientCon.sendPacket(SystemPacketType.CONNECT, PacketMap.of("uuid", uuid));
					
					onClientConnected(uuid);
					
					clientCon.start();
				}
			}catch(final IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	private class ClearingThread implements Runnable {
		
		@Override
		public void run() {
			synchronized(clients) {
				final long currentTime = System.currentTimeMillis();
				
				new HashMap<>(clients).forEach((uuid, client) -> {
					final long currentDiff = currentTime - client.lastPacket;
					
					if(client.clearingCount >= properties.getClearingPromptCount()) disconnectClient(uuid);
					else if(currentDiff > properties.getClearingInterval())
						client.sendPacket(SystemPacketType.WAKE, PacketMap.of("count", ++client.clearingCount));
				});
			}
		}
	}
}
