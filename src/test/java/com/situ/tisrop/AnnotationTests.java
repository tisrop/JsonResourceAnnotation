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

    @ParameterizedTest
    @JsonResource(resources = "/nested.json", jsonPath = "data/user", type = User.class)
    void testJsonPath(User user) {
        Assertions.assertEquals(user.getName(), "nested_user");
        Assertions.assertEquals(user.getAge(), 30);
    }

    @ParameterizedTest
    @JsonResource(resources = "/snake_case.json", type = SnakeUser.class, mapperFactory = CustomObjectMapperFactory.class)
    void testCustomObjectMapper(SnakeUser user) {
        Assertions.assertEquals(user.getUserName(), "jack");
        Assertions.assertEquals(user.getUserAge(), 25);
    }

    public static class SnakeUser {
        private String userName;
        private int userAge;

        public SnakeUser() {
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public int getUserAge() {
            return userAge;
        }

        public void setUserAge(int userAge) {
            this.userAge = userAge;
        }
    }
}
