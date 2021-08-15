package me.neo_0815.packethandler.bridge;

import me.neo_0815.packethandler.Properties;
import me.neo_0815.packethandler.bridge.IHostPortPairCreator.HostPortPair;
import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.server.Server;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public abstract class Bridge {
	BridgeServer server;
	IHostPortPairCreator<UUID> creator;
	HashMap<UUID, BridgeClient> clients = new HashMap<>();
	
	public Bridge(final int serverPort, final Properties serverProps, final HostPortPair hpp, final Properties clientProps) throws IOException {
		this(serverPort, serverProps, _uuid_ -> hpp, clientProps);
	}
	
	public Bridge(final int serverPort, final Properties serverProps, final IHostPortPairCreator<UUID> creator, final Properties clientProps) throws IOException {
		server = new BridgeServer(serverPort, serverProps, clientProps);
		
		this.creator = creator;
	}
	
	public final void start() {
		server.start();
	}
	
	public final void stop() {
		server.stop();
		
		synchronized(clients) {
			clients.clear();
		}
	}
	
	@Synchronized("clients")
	protected final BridgeClient getClient(final UUID client) {
		if(!clients.containsKey(client)) {
			System.err.println(client + " is not registered!");
			
			return null;
		}
		
		return clients.get(client);
	}
	
	protected void onClientConnected(final UUID client) {
	}
	
	protected void onClientDisconnected(final UUID client) {
	}
	
	protected abstract BridgedPacket onPacketReceived(final Side sender, final UUID client, final PacketBase<?> packet, final long id);
	
	protected BridgedPacket onUnknownPacketReceived(final Side sender, final UUID client, final UnknownPacket packet) {
		return createPacketType(packet, UnknownPacket.ID);
	}
	
	protected String onMessageReceived(final Side sender, final UUID client, final String message) {
		return message;
	}
	
	protected final BridgedPacket createPacketType(final PacketBase<?> packet, final long id) {
		return new BridgedPacket(packet, id);
	}
	
	private void disconnectClient(final UUID client) {
		synchronized(clients) {
			clients.remove(client);
		}
		
		onClientDisconnected(client);
	}
	
	public enum Side {
		SERVER,
		CLIENT
	}
	
	private final class BridgeServer extends Server {
		private final Properties clientProperties;
		
		public BridgeServer(final int port, final Properties properties, final Properties clientProperties) throws IOException {
			super(port, properties);
			
			this.clientProperties = clientProperties;
		}
		
		@Override
		protected void onClientConnected(final UUID client) {
			try {
				final BridgeClient clientObj = new BridgeClient(client, creator.create(client), clientProperties);
				clientObj.setUUID(client);
				
				clients.put(client, clientObj);
				
				Bridge.this.onClientConnected(client);
				
				clientObj.start();
			}catch(final IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		protected void onClientDisconnected(final UUID client) {
			Bridge.this.getClient(client).disconnect();
			
			Bridge.this.disconnectClient(client);
		}
		
		@Override
		protected void onPacketReceived(final UUID client, final PacketBase<?> packet, final long id) {
			final BridgedPacket p = Bridge.this.onPacketReceived(Side.CLIENT, client, packet, id);
			
			if(p != null) Bridge.this.getClient(client).sendPacket(p.packet, p);
		}
		
		@Override
		protected void onUnknownPacketReceived(final UUID client, final UnknownPacket packet) {
			final BridgedPacket p = Bridge.this.onUnknownPacketReceived(Side.CLIENT, client, packet);
			
			if(p != null) Bridge.this.getClient(client).sendPacket(p.packet, p);
		}
		
		@Override
		protected void onMessageReceived(final UUID client, final String message) {
			final String s = Bridge.this.onMessageReceived(Side.CLIENT, client, message);
			
			if(s != null) Bridge.this.getClient(client).sendMessage(message);
		}
		
		@Override
		protected void onClientUUIDChanged(final UUID client, final UUID old) {
			Bridge.this.getClient(old).changeUUID(client);
		}
		
		private void changeClient(final UUID client, final UUID uuid) {
			changeClientUUID(client, uuid);
		}
	}
	
	private final class BridgeClient extends Client {
		
		public BridgeClient(final UUID uuid, final HostPortPair hpp, final Properties properties) throws IOException {
			super(uuid, hpp.getHost(), hpp.getPort(), properties);
		}
		
		@Override
		protected void onPacketReceived(final PacketBase<?> packet, final long id) {
			final BridgedPacket p = Bridge.this.onPacketReceived(Side.SERVER, getUuid(), packet, id);
			
			if(p != null) server.sendPacket(getUuid(), p.packet, p);
		}
		
		@Override
		protected void onUnknownPacketReceived(final UnknownPacket packet) {
			final BridgedPacket p = Bridge.this.onUnknownPacketReceived(Side.SERVER, getUuid(), packet);
			
			if(p != null) server.sendPacket(getUuid(), p.packet, p);
		}
		
		@Override
		protected void onMessageReceived(final String message) {
			final String m = Bridge.this.onMessageReceived(Side.SERVER, getUuid(), message);
			
			if(m != null) server.sendMessage(getUuid(), m);
		}
		
		@Override
		protected void onDisconnected() {
			server.disconnectClient(getUuid());
			
			disconnectClient(getUuid());
		}
		
		@Override
		protected void onUUIDChanged(final UUID oldUUID, final UUID newUUID) {
			server.changeClient(oldUUID, newUUID);
		}
		
		private void setUUID(final UUID uuid) {
			changeUUID(uuid);
		}
	}
}
