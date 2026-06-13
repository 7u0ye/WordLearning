package com.qinyuan.wordlearning.controller;

import com.qinyuan.wordlearning.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

        @GetMapping("/api/test/hello")
        public Result hello() {
            return Result.success("Spring Boot + MyBatis-Plus 启动成功！");
        }
}
