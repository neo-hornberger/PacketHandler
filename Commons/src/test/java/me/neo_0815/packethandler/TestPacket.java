package me.neo_0815.packethandler;

import me.neo_0815.packethandler.packet.Packet;

public class TestPacket extends Packet {
	public String name = "";
	public TestObject object = new TestObject();
	
	@Override
	public void fromBuffer(final ByteBuffer buf) {
		name = buf.readString();
		object = buf.read(TestSchema.SCHEMA);
	}
	
	@Override
	public void toBuffer(final ByteBuffer buf) {
		buf.writeString(name);
		buf.write(TestSchema.SCHEMA, object);
	}
	
	@Override
	public void fromMap(final PacketMap map) {
		name = map.getOrDefault("name", "");
		object = map.getOrDefault("object", TestObject::new);
	}
	
	@Override
	public void intoMap(final PacketMap map) {
		map.put("name", name);
		map.put("object", object);
	}
}
