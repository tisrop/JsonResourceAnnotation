package io.github.tisrop.annotation;

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class JsonArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<JsonResource> {
    private final BiFunction<Class<?>, String, InputStream> inputStreamProvider;
    private JsonResource annotation;
    private String[] resources;
    private Charset charset;
    private Class<?> clazz;
    private ObjectMapper objectMapper;

    public JsonArgumentsProvider() {
        this(Class::getResourceAsStream);
    }

    public JsonArgumentsProvider(BiFunction<Class<?>, String, InputStream> inputStreamProvider) {
        this.inputStreamProvider = inputStreamProvider;
    }

    @Override
    public void accept(JsonResource annotation) {
        this.annotation = annotation;
        this.resources = annotation.resources();
        this.clazz = annotation.type();
        try {
            this.charset = Charset.forName(annotation.encoding());
        } catch (Exception ex) {
            throw new PreconditionViolationException(
                    String.format("The charset '%s' supplied in %s is invalid", annotation.encoding(), annotation),
                    ex
            );
        }
        try {
            ObjectMapperFactory factory = annotation.mapperFactory().getDeclaredConstructor().newInstance();
            this.objectMapper = factory.create();
        } catch (Exception e) {
            throw new PreconditionViolationException(String.format("Failed to create ObjectMapper from factory: %s", annotation.mapperFactory().getName()), e);
        }
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Arrays.stream(this.resources)
                .map(resource -> openInputStream(context, resource))
                .flatMap(is -> toStream(context, is));
    }

    private InputStream openInputStream(ExtensionContext context, String resource) {
        Preconditions.notBlank(resource, "Resource [" + resource + "] must not be null or blank");

        if (annotation.fileSystem()) {
            try {
                return Files.newInputStream(Paths.get(resource));
            } catch (IOException e) {
                throw new PreconditionViolationException(
                        String.format("File system resource [%s] does not exist or cannot be read", resource),
                        e
                );
            }
        }

        Class<?> testClass = context.getRequiredTestClass();
        return Preconditions.notNull(
                this.inputStreamProvider.apply(testClass, resource),
                () -> String.format("Classpath resource [%s] does not exist", resource)
        );
    }

    public String readSource(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, this.charset))) {
            bufferedReader.lines().forEach(e -> sb.append(e).append(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read input stream", e);
        }
        return sb.toString();
    }

    private Stream<Arguments> toStream(ExtensionContext context, InputStream inputStream) {
        try {
            String source = readSource(inputStream);
            JsonNode root = objectMapper.readTree(source);

            if (!annotation.jsonPath().isEmpty()) {
                root = applyJsonPath(root, annotation.jsonPath());
            }

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
            String resourceNames = Arrays.toString(this.resources);
            throw new RuntimeException(
                    String.format("Failed to parse JSON string from resources: %s, target type: %s",
                            resourceNames, this.clazz.getName()),
                    e
            );
        }
    }

    private JsonNode applyJsonPath(JsonNode root, String jsonPath) {
        if (jsonPath.startsWith("/")) {
            jsonPath = jsonPath.substring(1);
        }
        String[] parts = jsonPath.split("/");
        JsonNode current = root;
        for (String part : parts) {
            if (part.isEmpty()) continue;
            if (current.isObject() && current.has(part)) {
                current = current.get(part);
            } else if (current.isArray()) {
                try {
                    int index = Integer.parseInt(part);
                    if (index >= 0 && index < current.size()) {
                        current = current.get(index);
                    } else {
                        throw new IllegalArgumentException(
                                String.format("JSONPath index out of bounds: %s", jsonPath)
                        );
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            String.format("Invalid JSONPath: %s, expecting array index", jsonPath)
                    );
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("JSONPath not found: %s at part: %s", jsonPath, part)
                );
            }
        }
        return current;
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
