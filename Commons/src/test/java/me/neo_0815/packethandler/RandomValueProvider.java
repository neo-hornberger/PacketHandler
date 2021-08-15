package me.neo_0815.packethandler;

import me.neo_0815.packethandler.Random.ValueRange;
import me.neo_0815.packethandler.RandomValueSource.ValueFilter;
import me.neo_0815.packethandler.RandomValueSource.ValueType;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;

import java.util.stream.Stream;

class RandomValueProvider implements ArgumentsProvider, AnnotationConsumer<RandomValueSource> {
	private static final Random RAND = new Random();
	
	private ValueType type;
	private long count;
	private ValueFilter filter;
	
	@Override
	public void accept(final RandomValueSource customSource) {
		type = customSource.type();
		count = customSource.count();
		filter = customSource.filter();
	}
	
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
		final ValueRange range = type.getRange(filter);
		
		Stream<?> stream = Stream.of();
		
		switch(type) {
			case BYTE:
				stream = RAND.bytes(range);
				break;
			case SHORT:
				stream = RAND.shorts(range);
				break;
			case INT:
				stream = RAND.ints(range);
				break;
			case LONG:
				stream = RAND.longs(range);
				break;
			case FLOAT:
				stream = RAND.floats(filter);
				break;
			case DOUBLE:
				stream = RAND.doubles(filter);
				break;
			case BOOLEAN:
				stream = RAND.booleans();
				break;
			case CHAR:
				stream = RAND.chars();
				break;
		}
		
		return stream.limit(count).map(Arguments::of);
	}
}
