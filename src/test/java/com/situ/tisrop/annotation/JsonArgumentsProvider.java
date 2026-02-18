package com.situ.tisrop.annotation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<JsonResource> {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final BiFunction<Class<?>, String, InputStream> inputStreamProvider;
    private JsonResource annotation;
    private String[] resources;
    private Charset charset;
    private Class<?> clazz;

    JsonArgumentsProvider() {
        this(Class::getResourceAsStream);
    }

    JsonArgumentsProvider(BiFunction<Class<?>, String, InputStream> inputStreamProvider) {
        this.inputStreamProvider = inputStreamProvider;
    }

    @Override
    public void accept(JsonResource annotation) {
        this.annotation = annotation;
        this.resources = annotation.resources();
        this.clazz = annotation.type();
        try {
            this.charset = Charset.forName(annotation.encoding());
        } catch (Exception var3) {
            throw new PreconditionViolationException("The charset supplied in " + annotation + " is invalid", var3);
        }
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Arrays.stream(this.resources).map((resource) -> this.openInputStream(context, resource)).flatMap(is -> this.toStream(context, is));
    }

    private InputStream openInputStream(ExtensionContext context, String resource) {
        Preconditions.notBlank(resource, "Classpath resource [" + resource + "] must not be null or blank");
        Class<?> testClass = context.getRequiredTestClass();
        return Preconditions.notNull(this.inputStreamProvider.apply(testClass, resource), () -> "Classpath resource [" + resource + "] does not exist");
    }

    public String readSource(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, this.charset));
        bufferedReader.lines().forEach(e -> sb.append(e).append(System.lineSeparator()));
        return sb.toString();
    }

    private Stream<Arguments> toStream(ExtensionContext context, InputStream inputStream) {
        try {
            String source = readSource(inputStream);
            JsonNode root = objectMapper.readTree(source);
            if (root.isArray()) {
                Class<?> paramType = getSingleParameterType(context);
                if (paramType != null && List.class.isAssignableFrom(paramType)) {
                    Class<?> et = getListElementType(context);
                    if (et == null) {
                        et = this.clazz;
                    }
                    List<Object> list = new ArrayList<>();
                    for (JsonNode node : root) {
                        list.add(objectMapper.convertValue(node, et));
                    }
                    return Stream.of(Arguments.of(list));
                } else {
                    return StreamSupport.stream(root.spliterator(), false)
                            .map(node -> objectMapper.convertValue(node, this.clazz))
                            .map(Arguments::of);
                }
            }
            Object obj = objectMapper.treeToValue(root, this.clazz);
            return Stream.of(Arguments.of(obj));
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON string", e);
        }
    }

    private Class<?> getSingleParameterType(ExtensionContext context) {
        Class<?>[] types = Arrays.stream(context.getRequiredTestMethod().getParameterTypes()).toArray(Class<?>[]::new);
        if (types.length == 1) {
            return types[0];
        }
        return null;
    }

    private Class<?> getListElementType(ExtensionContext context) {
        Type[] genericTypes = Arrays.stream(context.getRequiredTestMethod().getGenericParameterTypes()).toArray(Type[]::new);
        if (genericTypes.length == 1 && genericTypes[0] instanceof ParameterizedType parameterizedType) {
            Type[] args = parameterizedType.getActualTypeArguments();
            if (args.length == 1 && args[0] instanceof Class<?> c) {
                return c;
            }
        }
        return null;
    }
}
