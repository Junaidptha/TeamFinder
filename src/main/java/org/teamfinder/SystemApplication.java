package org.teamfinder;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(SystemApplication.class,args);
        //This line automatically creates an ApplicationContext —that means Spring Boot starts the IoC container for you.
    }
}
