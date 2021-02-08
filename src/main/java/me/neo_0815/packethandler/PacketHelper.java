package me.neo_0815.packethandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import me.neo_0815.json.values.JsonObject;
import me.neo_0815.packethandler.packet.PacketBase;
import me.neo_0815.packethandler.packet.UnknownPacket;

public class PacketHelper {
	
	/**
	 * @deprecated PacketMap#of()
	 */
	@Deprecated
	public static PacketMap createMap() {
		return PacketMap.of();
	}
	
	/**
	 * @deprecated PacketMap#of(String, Object)
	 */
	@Deprecated
	public static PacketMap createMap(final String key, final Object value) {
		return PacketMap.of(key, value);
	}
	
	public static PacketMap createMap(final String key, final Object value, Object... objs) {
		final PacketMap map = PacketMap.of(key, value);
		
		if(objs.length % 2 != 0) objs = Arrays.copyOf(objs, objs.length + 1);
		
		for(int i = 0; i < objs.length; i += 2)
			map.put((String) objs[i], objs[i + 1]);
		
		return map;
	}
	
	public static PacketMap[] createMaps(final int count) {
		final PacketMap[] maps = new PacketMap[count];
		
		Arrays.fill(maps, PacketMap.of());
		
		return maps;
	}
	
	public static PacketMap[] createMaps(final JsonObject[] jsons) {
		final PacketMap[] maps = new PacketMap[jsons.length];
		
		for(int i = 0; i < jsons.length; i++)
			maps[i] = new PacketMap(jsons[i].toMap());
		
		return maps;
	}
	
	public static PacketBase<?> construct(final Class<? extends PacketBase<?>> packet) {
		try {
			return packet.getDeclaredConstructor().newInstance();
		}catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
		return new UnknownPacket();
	}
	
	public static ByteBuffer toBuffer(final ByteBufferGenerator<?> generator, final PacketBase<?> packet) {
		final ByteBuffer buf = generator.generate();
		
		buf.write(packet);
		
		return buf;
	}
	
	public static ByteBuffer toBuffer(final ByteBufferGenerator<?> generator, final PacketMap map) {
		final ByteBuffer buf = generator.generate();
		
		map.forEach((key, value) -> buf.writeNullTerminatedString(key).write(value));
		
		return buf;
	}
}
