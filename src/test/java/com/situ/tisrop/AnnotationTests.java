package com.situ.tisrop;

import com.situ.tisrop.annotation.JsonResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AnnotationTests {

    @ParameterizedTest
    @JsonResource(resources = "/import.json", type = User.class)
    void contextLoads(User user) {
        Assertions.assertEquals(user.getName(), "xiaoming");
        Assertions.assertEquals(user.getAge(), 23);
    }

    @ParameterizedTest
    @JsonResource(resources = "/import-array.json", type = User.class)
    void supportsArray(List<User> users) {
        List<String> names = users.stream().map(User::getName).collect(Collectors.toList());
        List<Integer> ages = users.stream().map(User::getAge).collect(Collectors.toList());
        Assertions.assertEquals(Stream.of("lili", "tom").collect(Collectors.toList()), names);
        Assertions.assertEquals(Stream.of(18, 20).collect(Collectors.toList()), ages);
    }
}
