package com.situ.tisrop;

import com.alibaba.fastjson.JSONObject;
import com.situ.tisrop.annotation.JsonResource;
import org.junit.Assert;
import org.junit.jupiter.params.ParameterizedTest;

class AnnotationTests {


    @ParameterizedTest
    @JsonResource(resources = "/import.json")
    void contextLoads(JSONObject jsonObject) {
        Assert.assertEquals(jsonObject.toJSONString(), "{\"name\":\"xiaoming\",\"age\":\"23\"}");
    }

}
