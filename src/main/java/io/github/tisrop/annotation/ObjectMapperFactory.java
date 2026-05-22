package io.github.tisrop.annotation;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ObjectMapperFactory {
    ObjectMapper create();
}
