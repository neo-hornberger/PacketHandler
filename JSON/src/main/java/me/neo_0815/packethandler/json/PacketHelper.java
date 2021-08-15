package me.neo_0815.packethandler.json;

import me.neo_0815.json.values.JsonObject;
import me.neo_0815.packethandler.PacketMap;

public class PacketHelper {
	
	public static PacketMap[] createMaps(final JsonObject[] jsons) {
		final PacketMap[] maps = new PacketMap[jsons.length];
		
		for(int i = 0; i < jsons.length; i++)
			maps[i] = new PacketMap(jsons[i].toMap());
		
		return maps;
	}
}
