package me.neo_0815.packethandler;

public enum TestEnum implements TestEnumInterface {
	ONE,
	TWO,
	THREE,
	FOUR,
	FIVE;
	
	@Override
	public int id() {
		return ordinal();
	}
}
