package me.neo_0815.packethandler;

import lombok.EqualsAndHashCode;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@EqualsAndHashCode
public class PacketMap implements Map<String, Object> {
	private final Map<String, Object> map;
	
	protected PacketMap() {
		map = new HashMap<>();
	}
	
	public PacketMap(final Map<String, ?> map) {
		this.map = new HashMap<>(map);
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	public boolean containsKey(final Object key) {
		return containsKey(String.valueOf(key));
	}
	
	public boolean containsKey(final String key) {
		return map.containsKey(key);
	}
	
	@Override
	public boolean containsValue(final Object value) {
		return map.containsValue(value);
	}
	
	@Override
	public Object get(final Object key) {
		return get(String.valueOf(key));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(final String key) {
		return (T) map.get(key);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getOrDefault(final String key, final T defaultValue) {
		return (T) map.getOrDefault(key, defaultValue);
	}
	
	public <T> T getOrDefault(final String key, final Supplier<T> defaultValue) {
		return map.containsKey(key) ? get(key) : defaultValue.get();
	}
	
	@Override
	public Object put(final String key, final Object value) {
		return map.put(key, value);
	}
	
	@Override
	public Object remove(final Object key) {
		return remove(String.valueOf(key));
	}
	
	@SuppressWarnings("unchecked")
	public <T> T remove(final String key) {
		return (T) map.remove(key);
	}
	
	@Override
	public void putAll(final Map<? extends String, ?> m) {
		map.putAll(m);
	}
	
	@Override
	public void clear() {
		map.clear();
	}
	
	@Override
	public Set<String> keySet() {
		return map.keySet();
	}
	
	@Override
	public Collection<Object> values() {
		return map.values();
	}
	
	@Override
	public Set<Entry<String, Object>> entrySet() {
		return map.entrySet();
	}
	
	public Stream<Entry<String, Object>> stream() {
		return entrySet().stream();
	}
	
	@Override
	public String toString() {
		return stream()
				.map(entry -> {
					final Object value = entry.getValue();
					
					String valueString = value.toString();
					if(value.getClass().isArray()) {
						if(value instanceof Object[]) valueString = Arrays.deepToString((Object[]) value);
						else valueString = IntStream.range(0, Array.getLength(value))
								.mapToObj(i -> Array.get(value, i))
								.map(String::valueOf)
								.collect(Collectors.joining(", ", "[", "]"));
					}
					
					return entry.getKey() + "=" + valueString;
				})
				.collect(Collectors.joining(", ", "{", "}"));
	}
	
	public static PacketMap of() {
		return new PacketMap();
	}
	
	public static PacketMap of(final String key, final Object value) {
		final PacketMap map = new PacketMap();
		
		map.put(key, value);
		
		return map;
	}
	
	public static PacketMap of(final String key, final Object value, final String k1, final Object v1) {
		final PacketMap map = new PacketMap();
		
		map.put(key, value);
		map.put(k1, v1);
		
		return map;
	}
	
	public static PacketMap of(final String key, final Object value, final String k1, final Object v1, final String k2, final Object v2) {
		final PacketMap map = new PacketMap();
		
		map.put(key, value);
		map.put(k1, v1);
		map.put(k2, v2);
		
		return map;
	}
	
	public static PacketMap of(final String key, final Object value, final String k1, final Object v1, final String k2, final Object v2, final String k3, final Object v3) {
		final PacketMap map = new PacketMap();
		
		map.put(key, value);
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		
		return map;
	}
	
	public static PacketMap of(final String key, final Object value, final String k1, final Object v1, final String k2, final Object v2, final String k3, final Object v3, final String k4, final Object v4) {
		final PacketMap map = new PacketMap();
		
		map.put(key, value);
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		
		return map;
	}
	
	public static PacketMap of(final String key, final Object value, final String k1, final Object v1, final String k2, final Object v2, final String k3, final Object v3, final String k4, final Object v4, final String k5, final Object v5) {
		final PacketMap map = new PacketMap();
		
		map.put(key, value);
		map.put(k1, v1);
		map.put(k2, v2);
		map.put(k3, v3);
		map.put(k4, v4);
		map.put(k5, v5);
		
		return map;
	}
}
