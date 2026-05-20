package com.situ.tisrop.annotation;

import org.apiguardian.api.API;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
        status = API.Status.EXPERIMENTAL,
        since = "1.0"
)
@ArgumentsSource(JsonArgumentsProvider.class)
public @interface JsonResource {
    String[] resources();

    String encoding() default "UTF-8";

    Class<?> type() default String.class;

    String jsonPath() default "";

    boolean fileSystem() default false;

    Class<? extends ObjectMapperFactory> mapperFactory() default DefaultObjectMapperFactory.class;
}
