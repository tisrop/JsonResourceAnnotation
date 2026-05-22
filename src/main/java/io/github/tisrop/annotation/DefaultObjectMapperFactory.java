package io.github.tisrop.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultObjectMapperFactory implements ObjectMapperFactory {
    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper();

    @Override
    public ObjectMapper create() {
        return DEFAULT_MAPPER;
    }
}
