package com.situ.tisrop.annotation;

import org.apiguardian.api.API;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.lang.annotation.*;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(
        status = API.Status.EXPERIMENTAL,
        since = "5.0"
)
@ArgumentsSource(JsonArgumentsProvider.class)
public @interface JsonResource {
    String[] resources();

    String encoding() default "UTF-8";
}
