package com.javaweb.smartnote;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.javaweb.smartnote.mapper")
public class SmartNoteApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartNoteApplication.class, args);
    }
}