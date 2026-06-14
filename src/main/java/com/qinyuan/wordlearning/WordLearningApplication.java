package com.qinyuan.wordlearning;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.qinyuan.wordlearning.mapper")
@SpringBootApplication
public class WordLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(WordLearningApplication.class, args);
    }

}
