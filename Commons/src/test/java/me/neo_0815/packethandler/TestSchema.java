package me.neo_0815.packethandler;

import me.neo_0815.packethandler.schema.Schema;

public class TestSchema implements Schema<TestObject, ByteBuffer> {
	public static final TestSchema SCHEMA = new TestSchema();
	
	private TestSchema() {
	}
	
	@Override
	public void fromBuffer(final TestObject obj, final ByteBuffer buf) {
		obj.string = buf.readString();
	}
	
	@Override
	public void toBuffer(final TestObject obj, final ByteBuffer buf) {
		buf.writeString(obj.string);
	}
	
	@Override
	public TestObject newInstance() {
		return new TestObject();
	}
}
