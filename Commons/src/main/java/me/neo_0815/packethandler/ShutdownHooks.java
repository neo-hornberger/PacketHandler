package me.neo_0815.packethandler;

import me.neo_0815.packethandler.client.Client;
import me.neo_0815.packethandler.server.Server;

public class ShutdownHooks {
	private static final ThreadGroup GROUP = new ThreadGroup("PacketHandler_ShutdownHooks");
	
	public static void registerServer(final Server server) {
		register(server::stop, server.toString());
	}
	
	public static void registerClient(final Client client) {
		register(client::stop, client.toString());
	}
	
	public static void register(final Runnable runnable, final String name) {
		Runtime.getRuntime().addShutdownHook(new Thread(GROUP, runnable, "PHSH -- " + name));
	}
}
