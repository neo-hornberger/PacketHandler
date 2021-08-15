package me.neo_0815.packethandler;

import me.neo_0815.packethandler.RandomValueSource.ValueFilter;

import java.util.Objects;
import java.util.stream.Stream;

public class Random extends java.util.Random {
	private static final long serialVersionUID = 3514237081213715815L;
	
	public Stream<Byte> bytes() {
		return ints(Byte.MIN_VALUE, Byte.MAX_VALUE + 1).mapToObj(i -> (byte) i);
	}
	
	public Stream<Byte> bytes(final ValueRange range) {
		range.checkByteValue();
		
		return ints(range.getIntMinimum(), range.getIntMaximum() + 1).mapToObj(i -> (byte) i);
	}
	
	public Stream<Short> shorts() {
		return ints(Short.MIN_VALUE, Short.MAX_VALUE + 1).mapToObj(i -> (short) i);
	}
	
	public Stream<Short> shorts(final ValueRange range) {
		range.checkShortValue();
		
		return ints(range.getIntMinimum(), range.getIntMaximum() + 1).mapToObj(i -> (short) i);
	}
	
	public Stream<Integer> ints(final ValueRange range) {
		range.checkIntValue();
		
		if(ValueRange.INT.equals(range)) return ints().boxed();
		if(range.getIntMaximum() == Integer.MAX_VALUE) return ints().filter(i -> range.getIntMinimum() <= i).boxed();
		
		return ints(range.getIntMinimum(), range.getIntMaximum() + 1).boxed();
	}
	
	public Stream<Long> longs(final ValueRange range) {
		if(ValueRange.LONG.equals(range)) return longs().boxed();
		if(range.getMaximum() == Long.MAX_VALUE) return longs().filter(l -> range.getMinimum() <= l).boxed();
		
		return longs(range.getMinimum(), range.getMaximum() + 1).boxed();
	}
	
	public Stream<Float> floats(final ValueFilter filter) {
		return doubles(Float.MIN_VALUE, Float.MAX_VALUE)
				.filter(i -> filter == ValueFilter.POSITIVE ? i > 0 : (filter != ValueFilter.NEGATIVE || i < 0))
				.mapToObj(d -> (float) d);
	}
	
	public Stream<Double> doubles(final ValueFilter filter) {
		return doubles().filter(i -> filter == ValueFilter.POSITIVE ? i > 0 : (filter != ValueFilter.NEGATIVE || i < 0)).boxed();
	}
	
	public Stream<Boolean> booleans() {
		return ints(0, 2).mapToObj(i -> i == 1);
	}
	
	public Stream<Character> chars() {
		return ints(0, 0xFFFF + 1).mapToObj(i -> (char) i);
	}
	
	public Stream<Character> chars(final ValueRange range) {
		range.checkCharValue();
		
		return ints(range.getIntMinimum(), range.getIntMaximum() + 1).mapToObj(i -> (char) i);
	}
	
	public static class ValueRange {
		public static final ValueRange BYTE = new ValueRange(Byte.MIN_VALUE, Byte.MAX_VALUE),
				SHORT = new ValueRange(Short.MIN_VALUE, Short.MAX_VALUE),
				INT = new ValueRange(Integer.MIN_VALUE, Integer.MAX_VALUE),
				LONG = new ValueRange(Long.MIN_VALUE, Long.MAX_VALUE),
				CHAR = new ValueRange(0, 0xFFFF),
				UBYTE = new ValueRange(0, 0xFF),
				UINT = new ValueRange(0, 0xFFFFFFFFL);
		
		private final long min, max;
		
		public ValueRange(final long min, final long max) {
			this.min = min;
			this.max = max;
		}
		
		public boolean isByteValue() {
			return min >= Byte.MIN_VALUE && max <= Byte.MAX_VALUE;
		}
		
		public boolean isShortValue() {
			return min >= Short.MIN_VALUE && max <= Short.MAX_VALUE;
		}
		
		public boolean isIntValue() {
			return min >= Integer.MIN_VALUE && max <= Integer.MAX_VALUE;
		}
		
		public boolean isCharValue() {
			return min >= 0 && max <= 0xFFFF;
		}
		
		public boolean isUByteValue() {
			return min >= 0 && max <= 0xFF;
		}
		
		public boolean isUIntValue() {
			return min >= 0 && max <= 0xFFFFFFFFL;
		}
		
		public boolean isValidValue(final long value) {
			return min <= value && value <= max;
		}
		
		public boolean isValidByteValue(final long value) {
			return isByteValue() && isValidValue(value);
		}
		
		public boolean isValidShortValue(final long value) {
			return isShortValue() && isValidValue(value);
		}
		
		public boolean isValidIntValue(final long value) {
			return isIntValue() && isValidValue(value);
		}
		
		public boolean isValidCharValue(final long value) {
			return isCharValue() && isValidValue(value);
		}
		
		public boolean isValidUByteValue(final long value) {
			return isUByteValue() && isValidValue(value);
		}
		
		public boolean isValidUIntValue(final long value) {
			return isUIntValue() && isValidValue(value);
		}
		
		public void checkByteValue() {
			if(!isByteValue()) throw new IllegalStateException();
		}
		
		public void checkShortValue() {
			if(!isShortValue()) throw new IllegalStateException();
		}
		
		public void checkIntValue() {
			if(!isIntValue()) throw new IllegalStateException();
		}
		
		public void checkCharValue() {
			if(!isCharValue()) throw new IllegalStateException();
		}
		
		public void checkUByteValue() {
			if(!isUByteValue()) throw new IllegalStateException();
		}
		
		public void checkUIntValue() {
			if(!isUIntValue()) throw new IllegalStateException();
		}
		
		public int getIntMinimum() {
			return Math.toIntExact(min);
		}
		
		public int getIntMaximum() {
			return Math.toIntExact(max);
		}
		
		public long getMinimum() {
			return min;
		}
		
		public long getMaximum() {
			return max;
		}
		
		@Override
		public boolean equals(final Object o) {
			if(this == o) return true;
			if(o == null || getClass() != o.getClass()) return false;
			
			final ValueRange that = (ValueRange) o;
			return min == that.min && max == that.max;
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(min, max);
		}
		
		@Override
		public String toString() {
			return "[" + min + ", " + max + "]";
		}
		
		public static ValueRange of(final java.time.temporal.ValueRange range) {
			return new ValueRange(range.getMinimum(), range.getMaximum());
		}
	}
}
