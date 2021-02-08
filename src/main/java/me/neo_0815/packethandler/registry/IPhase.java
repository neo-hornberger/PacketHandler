package me.neo_0815.packethandler.registry;

public interface IPhase<P extends IPhase<P>> {
	
	long id();
	
	P phase(final long id);
	
	default P next() {
		return phase(id() + 1);
	}
	
	default P previous() {
		return phase(id() - 1);
	}
}
