package com.situ.tisrop.annotation;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public class JsonArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<JsonResource> {
    private   BiFunction<Class<?>, String, InputStream> inputStreamProvider;
    private JsonResource annotation;
    private String[] resources;
    private Charset charset;

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
        try {
            this.charset = Charset.forName(annotation.encoding());
        } catch (Exception var3) {
            throw new PreconditionViolationException("The charset supplied in " + annotation + " is invalid", var3);
        }
    }

    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
        return Arrays.stream(this.resources).map((resource) -> this.openInputStream(context, resource)).flatMap(this::toStream);
    }

    private InputStream openInputStream(ExtensionContext context, String resource) {
        Preconditions.notBlank(resource, "Classpath resource [" + resource + "] must not be null or blank");
        Class<?> testClass = context.getRequiredTestClass();
        return Preconditions.notNull(this.inputStreamProvider.apply(testClass, resource), () -> "Classpath resource [" + resource + "] does not exist");
    }

    public String readSource(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedReader.lines().forEach(e -> sb.append(e).append(System.lineSeparator()));
        return sb.toString();
    }

    private Stream<Arguments> toStream(InputStream inputStream) {
        return Stream.of(Arguments.of(JSONObject.parse(readSource(inputStream))));
    }
}
