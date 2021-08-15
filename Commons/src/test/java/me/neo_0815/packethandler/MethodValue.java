package me.neo_0815.packethandler;

import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.function.BiFunction;

public class MethodValue implements BiFunction<Object, Object[], Object> {
	private final Method method;
	
	MethodValue(final Method method) {
		this.method = method;
	}
	
	@Override
	public Object apply(final Object instance, final Object[] params) {
		return ReflectionUtils.invokeMethod(method, instance, params);
	}
	
	@SuppressWarnings("unchecked")
	public <R> R invoke(final Object instance, final Object... params) {
		return (R) apply(instance, params);
	}
	
	@SuppressWarnings("unchecked")
	public <R> R invokeStatic(final Object... params) {
		return (R) apply(null, params);
	}
	
	public Method getMethod() {
		return method;
	}
	
	@Override
	public String toString() {
		return ReflectionUtils.getFullyQualifiedMethodName(method.getDeclaringClass(), method);
	}
}
