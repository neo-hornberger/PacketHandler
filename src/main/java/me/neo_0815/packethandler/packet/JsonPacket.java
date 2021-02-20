package me.neo_0815.packethandler.packet;

import me.neo_0815.json.JSON;
import me.neo_0815.json.JsonSerializable;
import me.neo_0815.json.values.JsonObject;
import me.neo_0815.packethandler.ByteBuffer;

public abstract class JsonPacket extends Packet implements JsonSerializable {
	
	@Override
	public final JsonObject toJSON() {
		return JsonSerializable.super.toJSON();
	}
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		fromJSON((JsonObject) JSON.parseJSON(buf.readString()));
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeString(toJSON().toMinifiedString());
	}
}
