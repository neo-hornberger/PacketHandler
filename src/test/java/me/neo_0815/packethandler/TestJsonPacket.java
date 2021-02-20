package me.neo_0815.packethandler;

import me.neo_0815.json.values.JsonObject;
import me.neo_0815.packethandler.packet.JsonPacket;

public class TestJsonPacket extends JsonPacket {
	public String name;
	public int age;
	
	@Override
	public void fromJSON(final JsonObject json) {
		name = json.getJsonString("name").getValue();
		age = json.getJsonNumber("age").intValue();
	}
	
	@Override
	public void toJSON(final JsonObject json) {
		json.put("name", name).put("age", age);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		name = map.getOrDefault("name", "");
		age = map.getOrDefault("age", -1);
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("name", name);
		map.put("age", age);
	}
}
