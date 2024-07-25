package dev.sashacorp.statemachine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class StateMachineApplication {

  public static void main(String[] args) {
    SpringApplication.run(StateMachineApplication.class, args);
  }
}
