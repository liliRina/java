package com.example.EduWebProject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication()
public class EduAppApplication {

    public EduAppApplication(Init init) {
		init.init();
    }

    public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(EduAppApplication.class, args);
	}
}
