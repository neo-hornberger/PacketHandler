package me.neo_0815.packethandler;

import me.neo_0815.packethandler.registry.IPhase;

public enum TestPhase implements IPhase<TestPhase> {
	;
	
	@Override
	public long id() {
		return 0;
	}
	
	@Override
	public TestPhase phase(final long id) {
		return null;
	}
}
