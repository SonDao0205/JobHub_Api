package com.btvn.jobhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.btvn.jobhub.repository.jpa")
@EnableRedisRepositories(basePackages = "com.btvn.jobhub.repository.redis")
public class JobHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobHubApplication.class, args);
    }

}
