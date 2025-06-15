package com.situ.tisrop;

import com.situ.tisrop.annotation.JsonResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;

class AnnotationTests {


    @ParameterizedTest
    @JsonResource(resources = "/import.json", type = User.class)
    void contextLoads(User user) {
        Assertions.assertEquals(user.getName(), "xiaoming");
        Assertions.assertEquals(user.getAge(), 23);
    }

}
