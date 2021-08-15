package me.neo_0815.packethandler;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamUtils {
	
	public static <T> Stream<T> generate(final Supplier<T> s, final long maxSize) {
		return Stream.generate(s).limit(maxSize);
	}
	
	public static <T1, T2, R> Stream<R> zip(final Stream<T1> a, final Stream<T2> b, final BiFunction<T1, T2, R> zipper) {
		final boolean parallel = a.isParallel() || b.isParallel();
		
		final Iterator<T1> aIterator = a.iterator();
		final Iterator<T2> bIterator = b.iterator();
		
		final Iterator<R> resultIterator = new Iterator<>() {
			
			@Override
			public boolean hasNext() {
				return aIterator.hasNext() && bIterator.hasNext();
			}
			
			@Override
			public R next() {
				return zipper.apply(aIterator.next(), bIterator.next());
			}
		};
		final Iterable<R> resultIterable = () -> resultIterator;
		
		return StreamSupport.stream(resultIterable.spliterator(), parallel);
	}
	
	public static <T1, T2, T3, R> Stream<R> zip(final Stream<T1> a, final Stream<T2> b, final Stream<T3> c, final TriFunction<T1, T2, T3, R> zipper) {
		final boolean parallel = a.isParallel() || b.isParallel() || c.isParallel();
		
		final Iterator<T1> aIterator = a.iterator();
		final Iterator<T2> bIterator = b.iterator();
		final Iterator<T3> cIterator = c.iterator();
		
		final Iterator<R> resultIterator = new Iterator<>() {
			
			@Override
			public boolean hasNext() {
				return aIterator.hasNext() && bIterator.hasNext() && cIterator.hasNext();
			}
			
			@Override
			public R next() {
				return zipper.apply(aIterator.next(), bIterator.next(), cIterator.next());
			}
		};
		final Iterable<R> resultIterable = () -> resultIterator;
		
		return StreamSupport.stream(resultIterable.spliterator(), parallel);
	}
	
	
	@FunctionalInterface
	public interface TriFunction<T, U, V, R> {
		
		R apply(T t, U u, V v);
		
		default <W> TriFunction<T, U, V, W> andThen(final Function<? super R, ? extends W> after) {
			Objects.requireNonNull(after);
			
			return (T t, U u, V v) -> after.apply(apply(t, u, v));
		}
	}
}
