package com.situ.tisrop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.situ.tisrop.annotation.ObjectMapperFactory;

public class CustomObjectMapperFactory implements ObjectMapperFactory {
    @Override
    public ObjectMapper create() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }
}
