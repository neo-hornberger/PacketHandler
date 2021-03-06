package me.neo_0815.packethandler;

import me.neo_0815.packethandler.PacketConstructionMode.PacketIdPair;
import me.neo_0815.packethandler.packet.Packet;
import me.neo_0815.packethandler.packet.UnknownPacket;
import me.neo_0815.packethandler.packet.system.PacketPrimitiveMessage;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class ThreadExecutors {
	public final ExecutorService ACCEPTING_THREAD_SERVICE = Executors.newCachedThreadPool(factory("AcceptingThread"));
	public final ScheduledExecutorService CLEARING_THREAD_SERVICE = Executors.newScheduledThreadPool(0, factory("ClearingThread"));
	public final ExecutorService LISTENING_THREAD_SERVICE = Executors.newCachedThreadPool(factory("ListeningThread"));
	public final ExecutorService PACKET_QUEUE_THREAD_SERVICE = Executors.newCachedThreadPool(factory("PacketQueueThread"));
	
	@RequiredArgsConstructor
	@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
	private static class NamedThreadFactory implements ThreadFactory {
		private static final ThreadFactory DEFAULT_FACTORY = Executors.defaultThreadFactory();
		
		String prefix, postfix;
		AtomicInteger threadNumber = new AtomicInteger(1);
		
		@Override
		public Thread newThread(final Runnable r) {
			final Thread t = DEFAULT_FACTORY.newThread(r);
			
			t.setName(prefix + threadNumber.getAndIncrement() + postfix);
			
			return t;
		}
		
	}
	
	private ThreadFactory factory(final String name) {
		return new NamedThreadFactory(name + "{", "}");
	}
	
	@AllArgsConstructor
	public static class NamedThreadRunnable implements Runnable {
		private final String name;
		
		@Override
		public void run() {
			Thread.currentThread().setName(Thread.currentThread().getName() + name);
		}
	}
	
	public interface RepeatableRunnable extends Runnable {
		
		@Override
		default void run() {
			while(!Thread.interrupted())
				if(!repeat()) break;
		}
		
		boolean repeat();
	}
	
	public static abstract class ConnectionRunnable extends NamedThreadRunnable implements RepeatableRunnable {
		protected final Connection connection;
		
		public ConnectionRunnable(final Connection connection) {
			super(" -- " + connection);
			
			this.connection = connection;
		}
		
		@Override
		public void run() {
			super.run();
			
			RepeatableRunnable.super.run();
		}
	}
	
	/**
	 * This thread is listening to an {@link InputStream}, reading the transferred
	 * bytes,<br>
	 * writing these bytes to a {@link ByteBuffer}, executing the matching
	 * {@link Packet}.
	 *
	 * @author Neo Hornberger
	 */
	public static class ListeningThread extends ConnectionRunnable {
		private final InputStream in;
		
		public ListeningThread(final Connection connection, final InputStream in) {
			super(connection);
			
			this.in = in;
		}
		
		
		@Override
		public boolean repeat() {
			try {
				if(in.available() <= 0) {
					Thread.onSpinWait();
					
					return true;
				}
				
				final ByteBuffer buf = connection.byteBufferGenerator().generate().readFromInputStream(in);
				
				PacketIdPair pip;
				while(!buf.isEmpty()) {
					if(connection.isEncryptionEnabled()) buf.decrypt(connection.encryption());
					
					pip = connection.constructionMode().decodePacket(connection.byteBufferGenerator(), buf, connection.registry());
					
					connection.getPacketQueue().offer(pip);
				}
			}catch(final IOException e) {
				e.printStackTrace();
			}
			
			return true;
		}
	}
	
	public static class PacketQueueThread extends ConnectionRunnable {
		
		public PacketQueueThread(final Connection connection) {
			super(connection);
		}
		
		@Override
		public boolean repeat() {
			final PacketIdPair pip;
			
			try {
				pip = connection.getPacketQueue().take();
			}catch(final InterruptedException e) {
				return false;
			}
			
			if(pip.id() >= 0) connection.onPacketReceived(pip.packet(), pip.id());
			else {
				if(pip.id() == UnknownPacket.ID)
					connection.onUnknownPacketReceived((UnknownPacket) pip.packet());
				else if(pip.packet() instanceof PacketPrimitiveMessage)
					connection.onMessageReceived(((PacketPrimitiveMessage) pip.packet()).message);
				else connection.onSystemPacketReceived(pip.packet());
			}
			
			return true;
		}
	}
}
