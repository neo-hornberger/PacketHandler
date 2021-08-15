package me.neo_0815.packethandler;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import static java.lang.String.format;

public class MethodValueProvider implements ArgumentsProvider, AnnotationConsumer<MethodValueSource> {
	private String[] methodNames;
	
	@Override
	public void accept(final MethodValueSource methodValueSource) {
		methodNames = methodValueSource.value();
		
		final String prefix = methodValueSource.prefix();
		if(StringUtils.isNotBlank(prefix))
			methodNames = Arrays.stream(methodNames)
					.map(methodName -> prefix + methodName)
					.toArray(String[]::new);
	}
	
	@Override
	public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception {
		return Arrays.stream(methodNames)
				.filter(StringUtils::isNotBlank)
				.map(methodName -> getMethod(context, methodName))
				.map(MethodValue::new)
				.map(Arguments::of);
	}
	
	private Method getMethod(final ExtensionContext context, final String factoryMethodName) {
		if(factoryMethodName.contains("#")) return getMethodByFullyQualifiedName(factoryMethodName);
		
		return ReflectionUtils.getRequiredMethod(context.getRequiredTestClass(), factoryMethodName);
	}
	
	private Method getMethodByFullyQualifiedName(final String fullyQualifiedMethodName) {
		final String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		final Class<?> clazz = loadRequiredClass(methodParts[0]);
		final String methodName = methodParts[1];
		final Class<?>[] methodParams = Arrays.stream(methodParts[2].split(","))
				.map(this::loadRequiredClass)
				.toArray(Class[]::new);
		
		return ReflectionUtils.getRequiredMethod(clazz, methodName, methodParams);
	}
	
	private Class<?> loadRequiredClass(final String className) {
		return ReflectionUtils.tryToLoadClass(className)
				.getOrThrow(cause -> new JUnitException(format("Could not load class [%s]", className), cause));
	}
}
