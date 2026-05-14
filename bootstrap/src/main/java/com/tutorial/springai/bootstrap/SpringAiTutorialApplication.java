package com.tutorial.springai.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.tutorial.springai")
@EnableJpaRepositories(basePackages = "com.tutorial.springai")
@EntityScan(basePackages = "com.tutorial.springai")
public class SpringAiTutorialApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringAiTutorialApplication.class, args);
    }
}
