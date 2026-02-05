package com.example.voteonline;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class VoteOnlineApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // Cấu hình cho deployment dạng WAR trên Tomcat bên ngoài
        return builder.sources(VoteOnlineApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(VoteOnlineApplication.class, args);
    }
}

