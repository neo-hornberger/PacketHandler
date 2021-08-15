package me.neo_0815.packethandler;

import me.neo_0815.packethandler.Random.ValueRange;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(RandomValueProvider.class)
public @interface RandomValueSource {
	
	ValueType type();
	
	long count();
	
	ValueFilter filter() default ValueFilter.NONE;
	
	enum ValueType {
		BYTE(new ValueRange(1, Byte.MAX_VALUE), new ValueRange(Byte.MIN_VALUE, -1), ValueRange.BYTE),
		SHORT(new ValueRange(1, Short.MAX_VALUE), new ValueRange(Short.MIN_VALUE, -1), ValueRange.SHORT),
		INT(new ValueRange(1, Integer.MAX_VALUE), new ValueRange(Integer.MIN_VALUE, -1), ValueRange.INT),
		LONG(new ValueRange(1, Long.MAX_VALUE), new ValueRange(Long.MIN_VALUE, -1), ValueRange.LONG),
		
		FLOAT,
		DOUBLE,
		
		BOOLEAN,
		
		CHAR,
		
		UBYTE(new ValueRange(1, 0xFF), null, ValueRange.UBYTE),
		USHORT(new ValueRange(1, 0xFFFF), null, ValueRange.CHAR),
		UINT(new ValueRange(1, 0xFFFFFFFFL), null, ValueRange.UINT);
		
		private final ValueRange positiveRange, negativeRange, normalRange;
		
		ValueType() {
			positiveRange = null;
			negativeRange = null;
			normalRange = null;
		}
		
		ValueType(final ValueRange positiveRange, final ValueRange negativeRange, final ValueRange normalRange) {
			this.positiveRange = positiveRange;
			this.negativeRange = negativeRange;
			this.normalRange = normalRange;
		}
		
		public ValueRange getRange(final ValueFilter filter) {
			switch(filter) {
				case POSITIVE:
					return positiveRange;
				case NEGATIVE:
					return negativeRange;
			}
			
			return normalRange;
		}
	}
	
	enum ValueFilter {
		POSITIVE,
		NEGATIVE,
		
		NONE
	}
}
